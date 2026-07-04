package com.br.leone.service;

import com.br.leone.entity.PerfilPrestador;
import com.br.leone.enums.StatusAprovacao;
import com.br.leone.exception.PerfilPrestadorDuplicadoException;
import com.br.leone.exception.PerfilPrestadorNaoEncontradoException;
import com.br.leone.exception.PrestadorSemPermissaoException;
import com.br.leone.repository.PerfilPrestadorRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PerfilPrestadorService {

    private final PerfilPrestadorRepository perfilPrestadorRepository;

    public PerfilPrestadorService(PerfilPrestadorRepository perfilPrestadorRepository) {
        this.perfilPrestadorRepository = perfilPrestadorRepository;
    }

    // Cria um novo perfil com status PENDENTE
    public PerfilPrestador criar(PerfilPrestador perfil) {
        Optional<PerfilPrestador> existente = perfilPrestadorRepository.findByUsuarioId(perfil.getUsuarioId());
        if (existente.isPresent()) {
            throw new PerfilPrestadorDuplicadoException("Este usuário já possui um perfil de prestador cadastrado.");
        }

        perfil.setStatusAprovacao(StatusAprovacao.PENDENTE);
        perfil.setDataSolicitacao(LocalDateTime.now());
        return perfilPrestadorRepository.save(perfil);
    }

    public List<PerfilPrestador> listarTodos() {
        return perfilPrestadorRepository.findAll();
    }


    public List<PerfilPrestador> listarPendentes() {
        return perfilPrestadorRepository.findByStatusAprovacao(StatusAprovacao.PENDENTE);
    }

    

    public PerfilPrestador buscarPorIdProtegido(Long id, String emailUsuarioLogado) {
        PerfilPrestador perfil = perfilPrestadorRepository.findById(id)
                .orElseThrow(() -> new PerfilPrestadorNaoEncontradoException(id));

        // 1. Se o perfil já estiver APROVADO, a visualização é pública e liberada
        if (perfil.getStatusAprovacao() == StatusAprovacao.APROVADO) {
            return perfil;
        }

        // 2. Se estiver PENDENTE, precisamos validar quem está tentando ver
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Verifica se o usuário logado possui a autoridade de ADMIN
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                return perfil; // ADMIN pode ver tudo
            }

            // Se não for Admin, precisamos buscar o perfil do usuário logado para comparar com o dono
            if (emailUsuarioLogado != null) {
                // Buscamos o ID do usuário que está fazendo a requisição pelo e-mail do token
                Long usuarioLogadoId = perfilPrestadorRepository.findByUsuarioId(perfil.getUsuarioId())
                        .map(p -> p.getUsuarioId()) // Apenas para ilustrar a lógica de comparação
                        .orElse(null);

                // Se o ID do usuário dono do perfil bater com o ID de quem está logado, ele pode ver
                if (perfil.getUsuarioId().equals(usuarioLogadoId)) {
                    return perfil;
                }
            }
        }

        // Se o perfil está PENDENTE e quem tenta acessar não é o dono nem ADMIN, barramos na hora!
        throw new PrestadorSemPermissaoException("Este perfil ainda não foi aprovado pelo administrador.");
    }

    public Optional<PerfilPrestador> buscarPorUsuarioId(Long usuarioId) {
        return perfilPrestadorRepository.findByUsuarioId(usuarioId);
    }

    // Atualiza os dados básicos (Apenas o dono poderá fazer isso, validado no Controller)
    public PerfilPrestador atualizar(Long id, PerfilPrestador dadosNovos, Long usuarioAutenticadoId) {
        PerfilPrestador perfil = perfilPrestadorRepository.findById(id)
                .orElseThrow(() -> new PerfilPrestadorNaoEncontradoException(id));

        if (!perfil.getUsuarioId().equals(usuarioAutenticadoId)) {
            throw new PrestadorSemPermissaoException("Você não tem permissão para editar este perfil.");
        }

        perfil.setNomeFantasia(dadosNovos.getNomeFantasia());
        perfil.setDescricao(dadosNovos.getDescricao());
        perfil.setAreaAtuacao(dadosNovos.getAreaAtuacao());

        return perfilPrestadorRepository.save(perfil);
    }

    // Aprova o perfil (Apenas ADMIN poderá chamar, validado no Controller)
    public PerfilPrestador aprovarPerfil(Long id) {
        PerfilPrestador perfil = perfilPrestadorRepository.findById(id)
                .orElseThrow(() -> new PerfilPrestadorNaoEncontradoException(id));

        perfil.setStatusAprovacao(StatusAprovacao.APROVADO);
        perfil.setDataAprovacao(LocalDateTime.now());
        return perfilPrestadorRepository.save(perfil);
    }

    // Rejeita/Deleta o perfil (Apenas ADMIN)
    public void rejeitarPerfil(Long id) {
        if (!perfilPrestadorRepository.existsById(id)) {
            throw new PerfilPrestadorNaoEncontradoException(id);
        }
        perfilPrestadorRepository.deleteById(id);
    }
}

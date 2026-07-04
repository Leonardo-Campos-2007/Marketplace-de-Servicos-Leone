package com.br.leone.service;

import com.br.leone.entity.PerfilPrestador;
import com.br.leone.enums.Role;
import com.br.leone.enums.StatusAprovacao;
import com.br.leone.exception.PerfilPrestadorDuplicadoException;
import com.br.leone.exception.PerfilPrestadorNaoEncontradoException;
import com.br.leone.exception.PrestadorSemPermissaoException;
import com.br.leone.repository.PerfilPrestadorRepository;
import com.br.leone.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PerfilPrestadorService {

    private final PerfilPrestadorRepository perfilPrestadorRepository;
    private final UserRepository userRepository;

    public PerfilPrestadorService(PerfilPrestadorRepository perfilPrestadorRepository, UserRepository userRepository) {
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.userRepository = userRepository;
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

    

    public PerfilPrestador buscarPorIdProtegido(Long id, Long usuarioLogadoId, boolean isAdmin) {
        PerfilPrestador perfil = perfilPrestadorRepository.findById(id)
                .orElseThrow(() -> new PerfilPrestadorNaoEncontradoException(id));

        if (perfil.getStatusAprovacao() == StatusAprovacao.APROVADO) {
            return perfil;
        }

        if (isAdmin) {
            return perfil;
        }

        if (usuarioLogadoId != null && perfil.getUsuarioId().equals(usuarioLogadoId)) {
            return perfil;
        }

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
        PerfilPrestador perfilSalvo = perfilPrestadorRepository.save(perfil);

        userRepository.findById(perfil.getUsuarioId()).ifPresent(user -> {
            user.setRole(Role.PRESTADOR);
            userRepository.save(user);
        });

        return perfilSalvo;
    }

    // Rejeita/Deleta o perfil (Apenas ADMIN)
    public void rejeitarPerfil(Long id) {
        if (!perfilPrestadorRepository.existsById(id)) {
            throw new PerfilPrestadorNaoEncontradoException(id);
        }
        perfilPrestadorRepository.deleteById(id);
    }
}

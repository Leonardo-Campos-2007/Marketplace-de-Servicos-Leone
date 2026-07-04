package com.br.leone.service;

import com.br.leone.entity.PerfilPrestador;
import com.br.leone.entity.Servico;
import com.br.leone.enums.StatusAprovacao;
import com.br.leone.enums.StatusPublicacao;
import com.br.leone.exception.CategoriaNaoEncontradaException;
import com.br.leone.exception.PrestadorSemPermissaoException;
import com.br.leone.exception.ServicoNaoEncontradoException;
import com.br.leone.repository.CategoriaServicoRepository;
import com.br.leone.repository.PerfilPrestadorRepository;
import com.br.leone.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;
    private final CategoriaServicoRepository categoriaServicoRepository;

    public ServicoService(ServicoRepository servicoRepository,
                          PerfilPrestadorRepository perfilPrestadorRepository,
                          CategoriaServicoRepository categoriaServicoRepository) {
        this.servicoRepository = servicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.categoriaServicoRepository = categoriaServicoRepository;
    }

    public Servico criar(Servico servico, Long usuarioAutenticadoId) {
        PerfilPrestador perfilPrestador = perfilPrestadorRepository.findById(servico.getPerfilPrestadorId())
                .orElseThrow(() -> new PrestadorSemPermissaoException("Perfil de prestador não encontrado."));

        if (!Objects.equals(perfilPrestador.getUsuarioId(), usuarioAutenticadoId)) {
            throw new PrestadorSemPermissaoException("Você não tem permissão para criar serviços neste perfil.");
        }

        if (perfilPrestador.getStatusAprovacao() != StatusAprovacao.APROVADO) {
            throw new PrestadorSemPermissaoException("Seu perfil de prestador ainda não foi aprovado.");
        }

        categoriaServicoRepository.findById(servico.getCategoriaServicoId())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(servico.getCategoriaServicoId()));

        servico.setStatusPublicacao(StatusPublicacao.ATIVO);
        return servicoRepository.save(servico);
    }

    public List<Servico> listarPorPrestador(Long perfilPrestadorId) {
        return servicoRepository.findByPerfilPrestadorId(perfilPrestadorId);
    }

    public List<Servico> listarAtivos() {
        return servicoRepository.findByStatusPublicacao(StatusPublicacao.ATIVO);
    }

    public List<Servico> listarPorCategoria(Long categoriaServicoId) {
        return servicoRepository.findByCategoriaServicoId(categoriaServicoId);
    }

    public Optional<Servico> buscarPorId(Long id) {
        return servicoRepository.findById(id);
    }

    public Servico atualizar(Long id, Servico dadosNovos, Long usuarioAutenticadoId) {
        Servico servicoExistente = servicoRepository.findById(id)
                .orElseThrow(() -> new ServicoNaoEncontradoException(id));

        validarDonoDoPerfil(servicoExistente.getPerfilPrestadorId(), usuarioAutenticadoId,
                "Você não tem permissão para editar este serviço.");

        servicoExistente.setNome(dadosNovos.getNome());
        servicoExistente.setDescricao(dadosNovos.getDescricao());
        servicoExistente.setPrecoBase(dadosNovos.getPrecoBase());
        servicoExistente.setTempoEstimado(dadosNovos.getTempoEstimado());
        servicoExistente.setStatusPublicacao(dadosNovos.getStatusPublicacao());

        return servicoRepository.save(servicoExistente);
    }

    public void deletar(Long id, Long usuarioAutenticadoId) {
        Servico servicoExistente = servicoRepository.findById(id)
                .orElseThrow(() -> new ServicoNaoEncontradoException(id));

        validarDonoDoPerfil(servicoExistente.getPerfilPrestadorId(), usuarioAutenticadoId,
                "Você não tem permissão para editar este serviço.");

        servicoRepository.deleteById(id);
    }

    private PerfilPrestador validarDonoDoPerfil(Long perfilPrestadorId, Long usuarioAutenticadoId, String mensagemSemPermissao) {
        PerfilPrestador perfilPrestador = perfilPrestadorRepository.findById(perfilPrestadorId)
                .orElseThrow(() -> new PrestadorSemPermissaoException("Perfil de prestador não encontrado."));

        if (!Objects.equals(perfilPrestador.getUsuarioId(), usuarioAutenticadoId)) {
            throw new PrestadorSemPermissaoException(mensagemSemPermissao);
        }

        return perfilPrestador;
    }
}

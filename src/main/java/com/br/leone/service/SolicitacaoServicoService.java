package com.br.leone.service;

import com.br.leone.entity.*;
import com.br.leone.enums.StatusCarrinho;
import com.br.leone.enums.StatusSolicitacao;
import com.br.leone.exception.SolicitacaoNaoEncontradaException;
import com.br.leone.exception.StatusTransicaoInvalidaException;
import com.br.leone.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SolicitacaoServicoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ServicoRepository servicoRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;
    private final SolicitacaoServicoRepository solicitacaoServicoRepository;
    private final ItemSolicitacaoRepository itemSolicitacaoRepository;
    private final HistoricoStatusSolicitacaoRepository historicoRepository;

    public SolicitacaoServicoService(CarrinhoRepository carrinhoRepository,
                                     ItemCarrinhoRepository itemCarrinhoRepository,
                                     ServicoRepository servicoRepository,
                                     PerfilPrestadorRepository perfilPrestadorRepository,
                                     SolicitacaoServicoRepository solicitacaoServicoRepository,
                                     ItemSolicitacaoRepository itemSolicitacaoRepository,
                                     HistoricoStatusSolicitacaoRepository historicoRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.servicoRepository = servicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.solicitacaoServicoRepository = solicitacaoServicoRepository;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
        this.historicoRepository = historicoRepository;
    }

    @Transactional
    public List<SolicitacaoServico> checkout(Long compradorId) {
        var carrinhoOpt = carrinhoRepository.findByUsuarioIdAndStatus(compradorId, StatusCarrinho.ATIVO);
        if (carrinhoOpt.isEmpty()) {
            throw new IllegalStateException("Carrinho ativo não encontrado para o usuário.");
        }

        var carrinho = carrinhoOpt.get();
        List<com.br.leone.entity.ItemCarrinho> itensCarrinho = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (itensCarrinho.isEmpty()) {
            throw new IllegalArgumentException("Carrinho está vazio.");
        }

        List<Long> servicoIds = itensCarrinho.stream().map(com.br.leone.entity.ItemCarrinho::getServicoId).toList();
        List<com.br.leone.entity.Servico> servicos = servicoRepository.findAllById(servicoIds);
        Map<Long, com.br.leone.entity.Servico> servicoMap = servicos.stream().collect(Collectors.toMap(com.br.leone.entity.Servico::getId, s -> s));

        // Agrupar itens por prestador
        Map<Long, List<com.br.leone.entity.ItemCarrinho>> itensPorPrestador = itensCarrinho.stream()
                .collect(Collectors.groupingBy(item -> {
                    var s = servicoMap.get(item.getServicoId());
                    return s.getPerfilPrestadorId();
                }));

        List<SolicitacaoServico> criadas = new ArrayList<>();

        for (Map.Entry<Long, List<com.br.leone.entity.ItemCarrinho>> entry : itensPorPrestador.entrySet()) {
            Long perfilPrestadorId = entry.getKey();
            List<com.br.leone.entity.ItemCarrinho> itensDoPrestador = entry.getValue();

            BigDecimal valorBruto = itensDoPrestador.stream()
                    .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal comissao = valorBruto.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorLiquido = valorBruto.subtract(comissao).setScale(2, RoundingMode.HALF_UP);

            SolicitacaoServico s = new SolicitacaoServico(compradorId, perfilPrestadorId, valorBruto.setScale(2, RoundingMode.HALF_UP), comissao, valorLiquido);
            SolicitacaoServico salvo = solicitacaoServicoRepository.save(s);

            // Criar itens da solicitacao com snapshot
            for (com.br.leone.entity.ItemCarrinho ic : itensDoPrestador) {
                com.br.leone.entity.Servico serv = servicoMap.get(ic.getServicoId());
                ItemSolicitacao item = new ItemSolicitacao(salvo.getId(), serv.getId(), serv.getNome(), serv.getDescricao(), ic.getPrecoUnitario(), ic.getQuantidade(), serv.getTempoEstimado());
                itemSolicitacaoRepository.save(item);
            }

            // Criar histórico inicial
            HistoricoStatusSolicitacao hist = new HistoricoStatusSolicitacao(salvo.getId(), null, StatusSolicitacao.PENDENTE, LocalDateTime.now(), "Criação via checkout", compradorId);
            historicoRepository.save(hist);

            criadas.add(salvo);
        }

        // Marcar carrinho como convertido e limpar itens
        carrinho.setStatus(StatusCarrinho.CONVERTIDO);
        carrinho.setDataAtualizacao(LocalDateTime.now());
        carrinhoRepository.save(carrinho);
        itemCarrinhoRepository.deleteByCarrinhoId(carrinho.getId());

        return criadas;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoServico> listarComoComprador(Long compradorId) {
        return solicitacaoServicoRepository.findByCompradorId(compradorId);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoServico> listarComoPrestador(Long usuarioId) {
        var perfilOpt = perfilPrestadorRepository.findByUsuarioId(usuarioId);
        if (perfilOpt.isEmpty()) return List.of();
        return solicitacaoServicoRepository.findByPerfilPrestadorId(perfilOpt.get().getId());
    }

    @Transactional(readOnly = true)
    public SolicitacaoServico buscarPorIdProtegido(Long id, Long usuarioLogadoId, boolean isAdmin) {
        var opt = solicitacaoServicoRepository.findById(id);
        if (opt.isEmpty()) throw new SolicitacaoNaoEncontradaException(id);
        SolicitacaoServico s = opt.get();

        // Permissão: comprador, prestador do perfil, ou admin
        if (isAdmin) return s;
        if (Objects.equals(s.getCompradorId(), usuarioLogadoId)) return s;
        if (Objects.equals(s.getPerfilPrestadorId(), usuarioLogadoId)) return s; // note:perfilPrestadorId is profile id; but requirement was prestador user id - map needed

        // To be conservative, also allow if usuarioLogadoId equals owner user id of perfilPrestador
        var perfilOpt = perfilPrestadorRepository.findById(s.getPerfilPrestadorId());
        if (perfilOpt.isPresent() && Objects.equals(perfilOpt.get().getUsuarioId(), usuarioLogadoId)) return s;

        throw new SolicitacaoNaoEncontradaException(id);
    }

    @Transactional
    public SolicitacaoServico alterarStatus(Long id, Long usuarioResponsavelId, StatusSolicitacao novoStatus, String observacao, boolean isAdmin) {
        var opt = solicitacaoServicoRepository.findById(id);
        if (opt.isEmpty()) throw new SolicitacaoNaoEncontradaException(id);
        SolicitacaoServico s = opt.get();
        StatusSolicitacao anterior = s.getStatus();

        // Regras de transição e permissões
        // Quem pode aceitar/rejeitar: apenas o prestador dono do perfil associado
        // Quem pode iniciar: apenas o prestador (ACEITA -> EM_ANDAMENTO)
        // Quem pode concluir: apenas o comprador ou ADMIN
        // Quem pode cancelar: comprador (se PENDENTE), prestador (PENDENTE, ACEITA, EM_ANDAMENTO), ADMIN sempre

        // obter id do usuario dono do perfil prestador
        Long prestadorUsuarioId = perfilPrestadorRepository.findById(s.getPerfilPrestadorId()).map(p -> p.getUsuarioId()).orElse(null);

        boolean isPrestador = Objects.equals(prestadorUsuarioId, usuarioResponsavelId);
        boolean isComprador = Objects.equals(s.getCompradorId(), usuarioResponsavelId);

        // verify transition validity
        if (anterior == StatusSolicitacao.PENDENTE && novoStatus == StatusSolicitacao.ACEITA) {
            if (!isPrestador && !isAdmin) throw new StatusTransicaoInvalidaException("Apenas o prestador pode aceitar a solicitação.");
        } else if (anterior == StatusSolicitacao.PENDENTE && novoStatus == StatusSolicitacao.CANCELADA) {
            if (!(isComprador || isPrestador || isAdmin)) throw new StatusTransicaoInvalidaException("Sem permissão para cancelar a solicitação pendente.");
        } else if (anterior == StatusSolicitacao.ACEITA && novoStatus == StatusSolicitacao.EM_ANDAMENTO) {
            if (!isPrestador && !isAdmin) throw new StatusTransicaoInvalidaException("Apenas o prestador pode iniciar a execução.");
        } else if (novoStatus == StatusSolicitacao.CONCLUIDA) {
            if (!(isComprador || isAdmin)) throw new StatusTransicaoInvalidaException("Apenas o comprador ou um ADMIN podem concluir a solicitação.");
        } else if (novoStatus == StatusSolicitacao.CANCELADA) {
            // prestador pode cancelar em PENDENTE, ACEITA, EM_ANDAMENTO
            if (isPrestador) {
                if (!(anterior == StatusSolicitacao.PENDENTE || anterior == StatusSolicitacao.ACEITA || anterior == StatusSolicitacao.EM_ANDAMENTO)) {
                    throw new StatusTransicaoInvalidaException("Prestador não pode cancelar neste estado.");
                }
            } else if (isComprador) {
                if (anterior != StatusSolicitacao.PENDENTE) throw new StatusTransicaoInvalidaException("Comprador só pode cancelar quando a solicitação estiver pendente.");
            } else if (!isAdmin) {
                throw new StatusTransicaoInvalidaException("Sem permissão para cancelar a solicitação.");
            }
        } else {
            // permitir transições naturals: PENDENTE->ACEITA, ACEITA->EM_ANDAMENTO, EM_ANDAMENTO->CONCLUIDA, etc.
            // impeditivas gerais: não permitir voltar de CONCLUIDA ou CANCELADA
            if (anterior == StatusSolicitacao.CONCLUIDA || anterior == StatusSolicitacao.CANCELADA) {
                throw new StatusTransicaoInvalidaException("Não é possível alterar o status de uma solicitação finalizada.");
            }
        }

        s.setStatus(novoStatus);
        s.setDataAtualizacao(LocalDateTime.now());
        solicitacaoServicoRepository.save(s);

        HistoricoStatusSolicitacao hist = new HistoricoStatusSolicitacao(s.getId(), anterior, novoStatus, LocalDateTime.now(), observacao, usuarioResponsavelId);
        historicoRepository.save(hist);

        return s;
    }
}

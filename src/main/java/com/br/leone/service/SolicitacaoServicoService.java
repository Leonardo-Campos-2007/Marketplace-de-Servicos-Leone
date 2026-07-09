package com.br.leone.service;

import com.br.leone.entity.*;
import com.br.leone.enums.StatusCarrinho;
import com.br.leone.enums.StatusSolicitacao;
import com.br.leone.enums.TipoNotificacao;
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
    private final ChatService chatService;
    private final NotificacaoService notificacaoService;

    public SolicitacaoServicoService(CarrinhoRepository carrinhoRepository,
                                     ItemCarrinhoRepository itemCarrinhoRepository,
                                     ServicoRepository servicoRepository,
                                     PerfilPrestadorRepository perfilPrestadorRepository,
                                     SolicitacaoServicoRepository solicitacaoServicoRepository,
                                     ItemSolicitacaoRepository itemSolicitacaoRepository,
                                     HistoricoStatusSolicitacaoRepository historicoRepository,
                                     ChatService chatService,
                                     NotificacaoService notificacaoService) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.servicoRepository = servicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.solicitacaoServicoRepository = solicitacaoServicoRepository;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
        this.historicoRepository = historicoRepository;
        this.chatService = chatService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public List<SolicitacaoServico> checkout(Long compradorId) {
        var carrinhoOpt = carrinhoRepository.findByUsuarioIdAndStatus(compradorId, StatusCarrinho.ATIVO);
        if (carrinhoOpt.isEmpty()) {
            throw new IllegalStateException("Carrinho ativo não encontrado para o usuário.");
        }

        var carrinho = carrinhoOpt.get();
        List<ItemCarrinho> itensCarrinho = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (itensCarrinho.isEmpty()) {
            throw new IllegalArgumentException("Carrinho está vazio.");
        }

        List<Long> servicoIds = itensCarrinho.stream().map(ItemCarrinho::getServicoId).toList();
        List<Servico> servicos = servicoRepository.findAllById(servicoIds);
        Map<Long, Servico> servicoMap = servicos.stream().collect(Collectors.toMap(Servico::getId, s -> s));

        Map<Long, List<ItemCarrinho>> itensPorPrestador = itensCarrinho.stream()
                .collect(Collectors.groupingBy(item -> {
                    var s = servicoMap.get(item.getServicoId());
                    return s.getPerfilPrestadorId();
                }));

        List<SolicitacaoServico> criadas = new ArrayList<>();

        for (Map.Entry<Long, List<ItemCarrinho>> entry : itensPorPrestador.entrySet()) {
            Long perfilPrestadorId = entry.getKey();
            List<ItemCarrinho> itensDoPrestador = entry.getValue();

            BigDecimal valorBruto = itensDoPrestador.stream()
                    .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal comissao = valorBruto.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorLiquido = valorBruto.subtract(comissao).setScale(2, RoundingMode.HALF_UP);

            SolicitacaoServico s = new SolicitacaoServico(compradorId, perfilPrestadorId, valorBruto.setScale(2, RoundingMode.HALF_UP), comissao, valorLiquido);
            SolicitacaoServico salvo = solicitacaoServicoRepository.save(s);
            chatService.criarChatParaSolicitacao(salvo.getId());

            for (ItemCarrinho ic : itensDoPrestador) {
                Servico serv = servicoMap.get(ic.getServicoId());
                ItemSolicitacao item = new ItemSolicitacao(salvo.getId(), serv.getId(), serv.getNome(), serv.getDescricao(), ic.getPrecoUnitario(), ic.getQuantidade(), serv.getTempoEstimado());
                itemSolicitacaoRepository.save(item);
            }

            HistoricoStatusSolicitacao hist = new HistoricoStatusSolicitacao(salvo.getId(), null, StatusSolicitacao.PENDENTE, LocalDateTime.now(), "Criação via checkout", compradorId);
            historicoRepository.save(hist);

            criadas.add(salvo);
        }

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

        if (isAdmin) return s;
        if (Objects.equals(s.getCompradorId(), usuarioLogadoId)) return s;
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

        Long prestadorUsuarioId = perfilPrestadorRepository.findById(s.getPerfilPrestadorId()).map(p -> p.getUsuarioId()).orElse(null);
        boolean isPrestador = Objects.equals(prestadorUsuarioId, usuarioResponsavelId);
        boolean isComprador = Objects.equals(s.getCompradorId(), usuarioResponsavelId);

        if (!isAdmin && !isPrestador && !isComprador) {
            throw new StatusTransicaoInvalidaException("Você não tem permissão sobre esta solicitação.");
        }

        boolean transicaoValida = switch (anterior) {
            case PENDENTE -> novoStatus == StatusSolicitacao.ACEITA || novoStatus == StatusSolicitacao.CANCELADA;
            case ACEITA -> novoStatus == StatusSolicitacao.EM_ANDAMENTO || novoStatus == StatusSolicitacao.CANCELADA;
            case EM_ANDAMENTO -> novoStatus == StatusSolicitacao.CONCLUIDA || novoStatus == StatusSolicitacao.CANCELADA;
            case CONCLUIDA, CANCELADA -> false;
        };

        if (!transicaoValida) {
            throw new StatusTransicaoInvalidaException(
                    "Transição de " + anterior + " para " + novoStatus + " não é permitida.");
        }

        if (novoStatus == StatusSolicitacao.ACEITA && !isPrestador && !isAdmin) {
            throw new StatusTransicaoInvalidaException("Apenas o prestador pode aceitar a solicitação.");
        }

        if (novoStatus == StatusSolicitacao.EM_ANDAMENTO && !isPrestador && !isAdmin) {
            throw new StatusTransicaoInvalidaException("Apenas o prestador pode iniciar a execução.");
        }

        if (novoStatus == StatusSolicitacao.CONCLUIDA && !isComprador && !isAdmin) {
            throw new StatusTransicaoInvalidaException("Apenas o comprador ou um ADMIN podem concluir a solicitação.");
        }

        if (novoStatus == StatusSolicitacao.CANCELADA) {
            if (isPrestador && !(anterior == StatusSolicitacao.PENDENTE || anterior == StatusSolicitacao.ACEITA || anterior == StatusSolicitacao.EM_ANDAMENTO)) {
                throw new StatusTransicaoInvalidaException("Prestador não pode cancelar neste estado.");
            }
            if (isComprador && anterior != StatusSolicitacao.PENDENTE) {
                throw new StatusTransicaoInvalidaException("Comprador só pode cancelar quando a solicitação estiver pendente.");
            }
            if (!isPrestador && !isComprador && !isAdmin) {
                throw new StatusTransicaoInvalidaException("Sem permissão para cancelar a solicitação.");
            }
        }

        s.setStatus(novoStatus);
        s.setDataAtualizacao(LocalDateTime.now());
        solicitacaoServicoRepository.save(s);

        if (novoStatus == StatusSolicitacao.CONCLUIDA || novoStatus == StatusSolicitacao.CANCELADA) {
            chatService.encerrarChat(s.getId());
        }

        List<Long> destinatarios = determinarDestinatariosNotificacao(s, usuarioResponsavelId, prestadorUsuarioId, isAdmin);
        TipoNotificacao tipoNotificacao = mapearTipoNotificacao(novoStatus);
        if (tipoNotificacao != null) {
            for (Long destinatarioId : destinatarios) {
                notificacaoService.notificar(destinatarioId, tipoNotificacao, s.getId());
            }
        }

        HistoricoStatusSolicitacao hist = new HistoricoStatusSolicitacao(s.getId(), anterior, novoStatus, LocalDateTime.now(), observacao, usuarioResponsavelId);
        historicoRepository.save(hist);

        return s;
    }

    private List<Long> determinarDestinatariosNotificacao(SolicitacaoServico s, Long usuarioResponsavelId,
                                                          Long prestadorUsuarioId, boolean isAdmin) {
        if (isAdmin) {
            return List.of(s.getCompradorId(), prestadorUsuarioId);
        }
        boolean quemAgiuFoiComprador = Objects.equals(s.getCompradorId(), usuarioResponsavelId);
        Long destinatario = quemAgiuFoiComprador ? prestadorUsuarioId : s.getCompradorId();
        return destinatario != null ? List.of(destinatario) : List.of();
    }

    private TipoNotificacao mapearTipoNotificacao(StatusSolicitacao novoStatus) {
        return switch (novoStatus) {
            case ACEITA -> TipoNotificacao.SOLICITACAO_ACEITA;
            case EM_ANDAMENTO -> TipoNotificacao.SOLICITACAO_INICIADA;
            case CONCLUIDA -> TipoNotificacao.SOLICITACAO_CONCLUIDA;
            case CANCELADA -> TipoNotificacao.SOLICITACAO_CANCELADA;
            default -> null;
        };
    }
}
package com.br.leone.service;

import com.br.leone.entity.Chat;
import com.br.leone.entity.Mensagem;
import com.br.leone.entity.PerfilPrestador;
import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.enums.TipoNotificacao;
import com.br.leone.exception.AcessoChatNegadoException;
import com.br.leone.exception.ChatEncerradoException;
import com.br.leone.exception.ChatNaoEncontradoException;
import com.br.leone.exception.SolicitacaoNaoEncontradaException;
import com.br.leone.repository.ChatRepository;
import com.br.leone.repository.MensagemRepository;
import com.br.leone.repository.PerfilPrestadorRepository;
import com.br.leone.repository.SolicitacaoServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MensagemRepository mensagemRepository;
    private final SolicitacaoServicoRepository solicitacaoServicoRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;
    private final NotificacaoService notificacaoService;

    public ChatService(ChatRepository chatRepository,
                       MensagemRepository mensagemRepository,
                       SolicitacaoServicoRepository solicitacaoServicoRepository,
                       PerfilPrestadorRepository perfilPrestadorRepository,
                       NotificacaoService notificacaoService) {
        this.chatRepository = chatRepository;
        this.mensagemRepository = mensagemRepository;
        this.solicitacaoServicoRepository = solicitacaoServicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Chat criarChatParaSolicitacao(Long solicitacaoId) {
        if (chatRepository.existsBySolicitacaoId(solicitacaoId)) {
            return chatRepository.findBySolicitacaoId(solicitacaoId).orElseThrow();
        }
        Chat chat = new Chat(solicitacaoId);
        return chatRepository.save(chat);
    }

    @Transactional
    public void encerrarChat(Long solicitacaoId) {
        chatRepository.findBySolicitacaoId(solicitacaoId).ifPresent(chat -> {
            chat.setAtivo(false);
            chatRepository.save(chat);
        });
    }

    @Transactional
    public Page<Mensagem> buscarMensagens(Long solicitacaoId, Long usuarioId, boolean isAdmin, Pageable pageable) {
        Chat chat = buscarChatOuLancar(solicitacaoId);
        validarAcessoLeitura(chat, usuarioId, isAdmin);

        Pageable comOrdem = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by("dataEnvio").ascending());

        Page<Mensagem> pagina = mensagemRepository.findByChatId(chat.getId(), comOrdem);

        mensagemRepository.marcarComoLidas(chat.getId(), usuarioId);

        return pagina;
    }

    @Transactional
    public Mensagem enviarMensagem(Long solicitacaoId, Long remetenteId, boolean isAdmin, String conteudo) {
        Chat chat = buscarChatOuLancar(solicitacaoId);

        if (isAdmin) {
            throw new AcessoChatNegadoException("Administradores podem visualizar o chat para moderação, mas não podem enviar mensagens.");
        }

        validarParticipante(chat, remetenteId);

        if (!chat.getAtivo()) {
            throw new ChatEncerradoException();
        }

        Mensagem mensagem = new Mensagem(chat.getId(), remetenteId, conteudo);
        Mensagem salva = mensagemRepository.save(mensagem);

        Long destinatarioId = determinarDestinatarioMensagem(chat, remetenteId);
        if (destinatarioId != null) {
            notificacaoService.notificar(destinatarioId, TipoNotificacao.MENSAGEM_NOVA, chat.getSolicitacaoId());
        }

        return salva;
    }

    private Chat buscarChatOuLancar(Long solicitacaoId) {
        return chatRepository.findBySolicitacaoId(solicitacaoId)
                .orElseThrow(() -> new ChatNaoEncontradoException(solicitacaoId));
    }

    private void validarAcessoLeitura(Chat chat, Long usuarioId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        validarParticipante(chat, usuarioId);
    }

    private void validarParticipante(Chat chat, Long usuarioId) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(chat.getSolicitacaoId())
                .orElseThrow(() -> new SolicitacaoNaoEncontradaException(chat.getSolicitacaoId()));

        if (Objects.equals(solicitacao.getCompradorId(), usuarioId)) {
            return;
        }

        Long prestadorUsuarioId = perfilPrestadorRepository.findById(solicitacao.getPerfilPrestadorId())
                .map(PerfilPrestador::getUsuarioId)
                .orElse(null);

        if (Objects.equals(prestadorUsuarioId, usuarioId)) {
            return;
        }

        throw new AcessoChatNegadoException();
    }

    private Long determinarDestinatarioMensagem(Chat chat, Long remetenteId) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(chat.getSolicitacaoId()).orElse(null);
        if (solicitacao == null) {
            return null;
        }

        boolean remetenteEhComprador = Objects.equals(solicitacao.getCompradorId(), remetenteId);
        if (remetenteEhComprador) {
            return perfilPrestadorRepository.findById(solicitacao.getPerfilPrestadorId())
                    .map(PerfilPrestador::getUsuarioId)
                    .orElse(null);
        }
        return solicitacao.getCompradorId();
    }
}
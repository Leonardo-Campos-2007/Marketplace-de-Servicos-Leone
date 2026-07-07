package com.br.leone.service;

import com.br.leone.entity.Chat;
import com.br.leone.entity.Mensagem;
import com.br.leone.entity.PerfilPrestador;
import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.exception.AcessoChatNegadoException;
import com.br.leone.exception.ChatEncerradoException;
import com.br.leone.exception.ChatNaoEncontradoException;
import com.br.leone.exception.SolicitacaoNaoEncontradaException;
import com.br.leone.repository.ChatRepository;
import com.br.leone.repository.MensagemRepository;
import com.br.leone.repository.PerfilPrestadorRepository;
import com.br.leone.repository.SolicitacaoServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MensagemRepository mensagemRepository;
    private final SolicitacaoServicoRepository solicitacaoServicoRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;

    public ChatService(ChatRepository chatRepository,
                       MensagemRepository mensagemRepository,
                       SolicitacaoServicoRepository solicitacaoServicoRepository,
                       PerfilPrestadorRepository perfilPrestadorRepository) {
        this.chatRepository = chatRepository;
        this.mensagemRepository = mensagemRepository;
        this.solicitacaoServicoRepository = solicitacaoServicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
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
        validarAcesso(chat, usuarioId, isAdmin);

        Pageable comOrdem = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by("dataEnvio").ascending());

        Page<Mensagem> pagina = mensagemRepository.findByChatId(chat.getId(), comOrdem);

        List<Mensagem> naoLidas = mensagemRepository
                .findByChatIdAndLidaFalseAndRemetenteIdNot(chat.getId(), usuarioId);
        naoLidas.forEach(m -> {
            m.setLida(true);
            mensagemRepository.save(m);
        });

        return pagina;
    }
    @Transactional
    public Mensagem enviarMensagem(Long solicitacaoId, Long remetenteId, String conteudo) {
        Chat chat = buscarChatOuLancar(solicitacaoId);
        validarAcesso(chat, remetenteId, false);

        if (!chat.getAtivo()) {
            throw new ChatEncerradoException();
        }

        Mensagem mensagem = new Mensagem(chat.getId(), remetenteId, conteudo);
        return mensagemRepository.save(mensagem);
    }

    private Chat buscarChatOuLancar(Long solicitacaoId) {
        return chatRepository.findBySolicitacaoId(solicitacaoId)
                .orElseThrow(() -> new ChatNaoEncontradoException(solicitacaoId));
    }

    private void validarAcesso(Chat chat, Long usuarioId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }

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
}
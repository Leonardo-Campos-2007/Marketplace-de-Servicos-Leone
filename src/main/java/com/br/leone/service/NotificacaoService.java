package com.br.leone.service;

import com.br.leone.dto.NotificacaoResponseDTO;
import com.br.leone.dto.UserResponseDTO;
import com.br.leone.entity.Notificacao;
import com.br.leone.enums.TipoNotificacao;
import com.br.leone.exception.NotificacaoNaoEncontradaException;
import com.br.leone.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoPushService notificacaoPushService;
    private final UserService userService;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, NotificacaoPushService notificacaoPushService, UserService userService) {
        this.notificacaoRepository = notificacaoRepository;
        this.notificacaoPushService = notificacaoPushService;
        this.userService = userService;
    }

    /**
     * Best-effort: falha na criação de notificação nunca deve reverter a transação
     * da operação que a originou (ex.: enviar mensagem, alterar status de solicitação).
     * Por isso captura qualquer exceção e apenas loga, ao invés de propagar.
     */


    @Transactional(readOnly = true)
    public Page<Notificacao> listar(Long usuarioId, boolean apenasNaoLidas, Pageable pageable) {
        if (apenasNaoLidas) {
            return notificacaoRepository.findByUsuarioIdAndVisualizadaFalse(usuarioId, pageable);
        }
        return notificacaoRepository.findByUsuarioId(usuarioId, pageable);
    }

    @Transactional
    public void marcarComoLida(Long id, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new NotificacaoNaoEncontradaException(id));

        if (!Objects.equals(notificacao.getUsuarioId(), usuarioId)) {
            throw new NotificacaoNaoEncontradaException(id);
        }

        if (Boolean.TRUE.equals(notificacao.getVisualizada())) {
            return; // idempotente: já lida, não é erro
        }

        notificacao.setVisualizada(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }

    public void notificar(Long usuarioId, TipoNotificacao tipo, Long referenciaId) {
        try {
            Notificacao notificacao = new Notificacao(usuarioId, tipo, referenciaId);
            Notificacao salva = notificacaoRepository.save(notificacao);

            UserResponseDTO usuario = userService.buscarPorId(usuarioId);
            notificacaoPushService.enviarNotificacaoParaUsuario(
                    usuario.email(), new NotificacaoResponseDTO(salva));
        } catch (Exception ex) {
            log.error("Falha ao criar notificação. usuarioId={}, tipo={}, referenciaId={}",
                    usuarioId, tipo, referenciaId, ex);
        }
    }
}
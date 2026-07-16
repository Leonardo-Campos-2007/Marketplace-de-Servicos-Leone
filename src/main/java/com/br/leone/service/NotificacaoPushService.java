package com.br.leone.service;

import com.br.leone.dto.MensagemResponseDTO;
import com.br.leone.dto.NotificacaoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoPushService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoPushService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificacaoPushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Best-effort: falha no push nunca deve reverter a operação principal
     * (mensagem já foi salva no banco antes deste método ser chamado).
     */
    public void enviarMensagemParaChat(Long solicitacaoId, MensagemResponseDTO mensagem) {
        try {
            messagingTemplate.convertAndSend("/topic/chat/" + solicitacaoId, mensagem);
        } catch (Exception e) {
            log.error("Falha ao empurrar mensagem via WebSocket. solicitacaoId={}, mensagemId={}",
                    solicitacaoId, mensagem.id(), e);
        }
    }

    public void enviarNotificacaoParaUsuario(String emailUsuario, NotificacaoResponseDTO notificacao) {
        try {
            messagingTemplate.convertAndSendToUser(emailUsuario, "/queue/notificacoes", notificacao);
        } catch (Exception e) {
            log.error("Falha ao empurrar notificação via WebSocket. email={}, notificacaoId={}",
                    emailUsuario, notificacao.id(), e);
        }
    }
}
package com.br.leone.dto;

import com.br.leone.entity.Notificacao;
import com.br.leone.enums.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        TipoNotificacao tipo,
        Long referenciaId,
        boolean visualizada,
        LocalDateTime dataCriacao
) {
    public NotificacaoResponseDTO(Notificacao n) {
        this(n.getId(), n.getTipo(), n.getReferenciaId(), n.getVisualizada(), n.getDataCriacao());
    }
}
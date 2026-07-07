package com.br.leone.dto;

import com.br.leone.entity.Mensagem;

import java.time.LocalDateTime;

public record MensagemResponseDTO(
        Long id,
        Long chatId,
        Long remetenteId,
        String conteudo,
        LocalDateTime dataEnvio,
        boolean lida
) {
    public MensagemResponseDTO(Mensagem m) {
        this(m.getId(), m.getChatId(), m.getRemetenteId(), m.getConteudo(), m.getDataEnvio(), m.getLida());
    }
}
package com.br.leone.dto;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponseDTO(
        Long id,
        Long solicitacaoId,
        boolean ativo,
        LocalDateTime dataCriacao,
        List<MensagemResponseDTO> mensagens,
        int paginaAtual,
        int totalPaginas,
        long totalMensagens
) {
    public ChatResponseDTO(com.br.leone.entity.Chat chat, Page<MensagemResponseDTO> pagina) {
        this(chat.getId(), chat.getSolicitacaoId(), chat.getAtivo(), chat.getDataCriacao(),
                pagina.getContent(), pagina.getNumber(), pagina.getTotalPages(), pagina.getTotalElements());
    }
}
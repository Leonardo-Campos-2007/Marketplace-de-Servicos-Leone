package com.br.leone.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record NotificacaoPageResponseDTO(
        List<NotificacaoResponseDTO> notificacoes,
        int paginaAtual,
        int totalPaginas,
        long totalElementos
) {
    public NotificacaoPageResponseDTO(Page<NotificacaoResponseDTO> pagina) {
        this(pagina.getContent(), pagina.getNumber(), pagina.getTotalPages(), pagina.getTotalElements());
    }
}
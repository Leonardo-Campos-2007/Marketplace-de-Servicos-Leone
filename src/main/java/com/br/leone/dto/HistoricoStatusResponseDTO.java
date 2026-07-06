package com.br.leone.dto;

import com.br.leone.entity.HistoricoStatusSolicitacao;
import com.br.leone.enums.StatusSolicitacao;

import java.time.LocalDateTime;

public record HistoricoStatusResponseDTO(
        Long id,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        LocalDateTime dataAlteracao,
        String observacao,
        Long usuarioResponsavelId
) {
    public HistoricoStatusResponseDTO(HistoricoStatusSolicitacao h) {
        this(h.getId(), h.getStatusAnterior(), h.getStatusNovo(), h.getDataAlteracao(), h.getObservacao(), h.getUsuarioResponsavelId());
    }
}

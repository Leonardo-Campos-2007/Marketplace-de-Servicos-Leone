package com.br.leone.dto;

import com.br.leone.entity.ItemSolicitacao;

import java.math.BigDecimal;

public record ItemSolicitacaoResponseDTO(
        Long id,
        Long servicoId,
        String nomeServico,
        String descricaoServico,
        BigDecimal precoUnitario,
        Integer quantidade,
        Integer tempoEstimado
) {
    public ItemSolicitacaoResponseDTO(ItemSolicitacao item) {
        this(item.getId(), item.getServicoId(), item.getNomeServicoSnapshot(), item.getDescricaoServicoSnapshot(), item.getPrecoUnitario(), item.getQuantidade(), item.getTempoEstimadoSnapshot());
    }
}

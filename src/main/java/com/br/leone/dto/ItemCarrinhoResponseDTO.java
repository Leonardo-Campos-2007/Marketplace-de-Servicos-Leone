package com.br.leone.dto;

import com.br.leone.entity.ItemCarrinho;
import com.br.leone.entity.Servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemCarrinhoResponseDTO(
        Long id,
        Long servicoId,
        String nomeServico,
        BigDecimal precoBaseServicoAtual,
        BigDecimal precoUnitarioNoCarrinho,
        Integer quantidade,
        LocalDateTime dataAdicionado,
        boolean precoAlterado
) {
    public ItemCarrinhoResponseDTO(ItemCarrinho item, Servico servico) {
        this(
                item.getId(),
                item.getServicoId(),
                servico != null ? servico.getNome() : "Serviço Inexistente",
                servico != null ? servico.getPrecoBase() : BigDecimal.ZERO,
                item.getPrecoUnitario(),
                item.getQuantidade(),
                item.getDataAdicionado(),
                servico != null && item.getPrecoUnitario().compareTo(servico.getPrecoBase()) != 0
        );
    }
}

package com.br.leone.dto;

import com.br.leone.entity.Carrinho;
import com.br.leone.enums.StatusCarrinho;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarrinhoResponseDTO(
        Long id,
        Long usuarioId,
        StatusCarrinho status,
        List<ItemCarrinhoResponseDTO> itens,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    public CarrinhoResponseDTO(Carrinho carrinho, List<ItemCarrinhoResponseDTO> itens) {
        this(
                carrinho.getId(),
                carrinho.getUsuarioId(),
                carrinho.getStatus(),
                itens,
                itens.stream()
                        .map(item -> item.precoUnitarioNoCarrinho().multiply(BigDecimal.valueOf(item.quantidade())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                carrinho.getDataCriacao(),
                carrinho.getDataAtualizacao()
        );
    }
}

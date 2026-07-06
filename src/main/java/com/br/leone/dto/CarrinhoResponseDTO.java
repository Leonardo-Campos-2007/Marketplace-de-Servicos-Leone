package com.br.leone.dto;

import com.br.leone.entity.Carrinho;
import com.br.leone.enums.StatusCarrinho;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CarrinhoResponseDTO {

    private Long id;
    private Long usuarioId;
    private StatusCarrinho status;
    private List<ItemCarrinhoResponseDTO> itens;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public CarrinhoResponseDTO(Carrinho carrinho, List<ItemCarrinhoResponseDTO> itens) {
        this.id = carrinho.getId();
        this.usuarioId = carrinho.getUsuarioId();
        this.status = carrinho.getStatus();
        this.itens = itens;
        this.dataCriacao = carrinho.getDataCriacao();
        this.dataAtualizacao = carrinho.getDataAtualizacao();

        // Calcula o valor total com base nos preços e quantidades do carrinho
        this.valorTotal = itens.stream()
                .map(item -> item.getPrecoUnitarioNoCarrinho().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public StatusCarrinho getStatus() { return status; }
    public List<ItemCarrinhoResponseDTO> getItens() { return itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}

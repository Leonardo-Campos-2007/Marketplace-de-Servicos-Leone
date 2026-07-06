package com.br.leone.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("item_carrinho")
public class ItemCarrinho {

    @Id
    private Long id;

    @Column("carrinho_id")
    private Long carrinhoId;

    @Column("servico_id")
    private Long servicoId;

    @Column("quantidade")
    private Integer quantidade = 1;

    @Column("preco_unitario")
    private BigDecimal precoUnitario;

    @Column("data_adicionado")
    private LocalDateTime dataAdicionado = LocalDateTime.now();

    protected ItemCarrinho() {}

    public ItemCarrinho(Long id, Long carrinhoId, Long servicoId, Integer quantidade, BigDecimal precoUnitario, LocalDateTime dataAdicionado) {
        this.id = id;
        this.carrinhoId = carrinhoId;
        this.servicoId = servicoId;
        this.quantidade = quantidade != null ? quantidade : 1;
        this.precoUnitario = precoUnitario;
        this.dataAdicionado = dataAdicionado != null ? dataAdicionado : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCarrinhoId() { return carrinhoId; }
    public void setCarrinhoId(Long carrinhoId) { this.carrinhoId = carrinhoId; }

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public LocalDateTime getDataAdicionado() { return dataAdicionado; }
    public void setDataAdicionado(LocalDateTime dataAdicionado) { this.dataAdicionado = dataAdicionado; }
}

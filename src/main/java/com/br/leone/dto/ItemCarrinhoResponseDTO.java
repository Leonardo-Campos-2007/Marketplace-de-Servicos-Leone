package com.br.leone.dto;

import com.br.leone.entity.ItemCarrinho;
import com.br.leone.entity.Servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemCarrinhoResponseDTO {

    private Long id;
    private Long servicoId;
    private String nomeServico;
    private BigDecimal precoBaseServicoAtual;
    private BigDecimal precoUnitarioNoCarrinho;
    private Integer quantidade;
    private LocalDateTime dataAdicionado;
    private boolean precoAlterado;

    public ItemCarrinhoResponseDTO(ItemCarrinho item, Servico servico) {
        this.id = item.getId();
        this.servicoId = item.getServicoId();
        this.quantidade = item.getQuantidade();
        this.precoUnitarioNoCarrinho = item.getPrecoUnitario();
        this.dataAdicionado = item.getDataAdicionado();

        if (servico != null) {
            this.nomeServico = servico.getNome();
            this.precoBaseServicoAtual = servico.getPrecoBase();
            this.precoAlterado = item.getPrecoUnitario().compareTo(servico.getPrecoBase()) != 0;
        } else {
            this.nomeServico = "Serviço Inexistente";
            this.precoBaseServicoAtual = BigDecimal.ZERO;
            this.precoAlterado = false;
        }
    }

    public Long getId() { return id; }
    public Long getServicoId() { return servicoId; }
    public String getNomeServico() { return nomeServico; }
    public BigDecimal getPrecoBaseServicoAtual() { return precoBaseServicoAtual; }
    public BigDecimal getPrecoUnitarioNoCarrinho() { return precoUnitarioNoCarrinho; }
    public Integer getQuantidade() { return quantidade; }
    public LocalDateTime getDataAdicionado() { return dataAdicionado; }
    public boolean isPrecoAlterado() { return precoAlterado; }
}

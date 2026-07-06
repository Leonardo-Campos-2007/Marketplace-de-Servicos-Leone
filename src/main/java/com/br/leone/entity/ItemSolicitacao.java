package com.br.leone.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("item_solicitacao")
public class ItemSolicitacao {

    @Id
    private Long id;

    @Column("solicitacao_id")
    private Long solicitacaoId;

    @Column("servico_id")
    private Long servicoId;

    @Column("nome_servico_snapshot")
    private String nomeServicoSnapshot;

    @Column("descricao_servico_snapshot")
    private String descricaoServicoSnapshot;

    @Column("preco_unitario")
    private BigDecimal precoUnitario;

    @Column("quantidade")
    private Integer quantidade = 1;

    @Column("tempo_estimado_snapshot")
    private Integer tempoEstimadoSnapshot;

    protected ItemSolicitacao() {}

    public ItemSolicitacao(Long id, Long solicitacaoId, Long servicoId, String nomeServicoSnapshot,
                           String descricaoServicoSnapshot, BigDecimal precoUnitario, Integer quantidade,
                           Integer tempoEstimadoSnapshot) {
        this.id = id;
        this.solicitacaoId = solicitacaoId;
        this.servicoId = servicoId;
        this.nomeServicoSnapshot = nomeServicoSnapshot;
        this.descricaoServicoSnapshot = descricaoServicoSnapshot;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade != null ? quantidade : 1;
        this.tempoEstimadoSnapshot = tempoEstimadoSnapshot;
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSolicitacaoId() { return solicitacaoId; }
    public void setSolicitacaoId(Long solicitacaoId) { this.solicitacaoId = solicitacaoId; }

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }

    public String getNomeServicoSnapshot() { return nomeServicoSnapshot; }
    public void setNomeServicoSnapshot(String nomeServicoSnapshot) { this.nomeServicoSnapshot = nomeServicoSnapshot; }

    public String getDescricaoServicoSnapshot() { return descricaoServicoSnapshot; }
    public void setDescricaoServicoSnapshot(String descricaoServicoSnapshot) { this.descricaoServicoSnapshot = descricaoServicoSnapshot; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Integer getTempoEstimadoSnapshot() { return tempoEstimadoSnapshot; }
    public void setTempoEstimadoSnapshot(Integer tempoEstimadoSnapshot) { this.tempoEstimadoSnapshot = tempoEstimadoSnapshot; }
}

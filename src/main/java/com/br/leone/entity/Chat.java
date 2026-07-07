package com.br.leone.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("chat")
public class Chat {

    @Id
    private Long id;

    @NotNull(message = "Solicitação é obrigatória")
    @Column("solicitacao_id")
    private Long solicitacaoId;

    @Column("ativo")
    private Boolean ativo = true;

    @Column("data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    protected Chat() {}

    public Chat(Long solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSolicitacaoId() { return solicitacaoId; }
    public void setSolicitacaoId(Long solicitacaoId) { this.solicitacaoId = solicitacaoId; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
package com.br.leone.entity;

import com.br.leone.enums.StatusCarrinho;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("carrinho")
public class Carrinho {

    @Id
    private Long id;

    @Column("usuario_id")
    private Long usuarioId;

    @Column("status")
    private StatusCarrinho status = StatusCarrinho.ATIVO;

    @Column("data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    protected Carrinho() {}

    public Carrinho(Long id, Long usuarioId, StatusCarrinho status, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.status = status != null ? status : StatusCarrinho.ATIVO;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
        this.dataAtualizacao = dataAtualizacao != null ? dataAtualizacao : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public StatusCarrinho getStatus() { return status; }
    public void setStatus(StatusCarrinho status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}

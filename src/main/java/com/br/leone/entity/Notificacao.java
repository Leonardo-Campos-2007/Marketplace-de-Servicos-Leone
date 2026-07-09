package com.br.leone.entity;

import com.br.leone.enums.TipoNotificacao;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("notificacao")
public class Notificacao {

    @Id
    private Long id;

    @NotNull(message = "Usuário destinatário é obrigatório")
    @Column("usuario_id")
    private Long usuarioId;

    @NotNull(message = "Tipo da notificação é obrigatório")
    @Column("tipo")
    private TipoNotificacao tipo;

    @NotNull(message = "Referência é obrigatória")
    @Column("referencia_id")
    private Long referenciaId;

    @Column("visualizada")
    private Boolean visualizada = false;

    @Column("data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    protected Notificacao() {}

    public Notificacao(Long usuarioId, TipoNotificacao tipo, Long referenciaId) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.visualizada = false;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public Boolean getVisualizada() { return visualizada; }
    public void setVisualizada(Boolean visualizada) { this.visualizada = visualizada; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
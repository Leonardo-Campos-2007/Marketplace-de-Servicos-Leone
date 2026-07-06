package com.br.leone.entity;

import com.br.leone.enums.StatusSolicitacao;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("historico_status_solicitacao")
public class HistoricoStatusSolicitacao {

    @Id
    private Long id;

    @Column("solicitacao_id")
    private StatusSolicitacao solicitacaoId;

    @Column("status_anterior")
    private StatusSolicitacao statusAnterior;

    @Column("status_novo")
    private StatusSolicitacao statusNovo;

    @Column("data_alteracao")
    private LocalDateTime dataAlteracao = LocalDateTime.now();

    @Column("observacao")
    private String observacao;

    @Column("usuario_responsavel_id")
    private Long usuarioResponsavelId;

    protected HistoricoStatusSolicitacao() {}

    public HistoricoStatusSolicitacao(Long id, StatusSolicitacao solicitacaoId, StatusSolicitacao statusAnterior,
                                      LocalDateTime dataAlteracao,
                                      String observacao, Long usuarioResponsavelId) {
        this.id = id;
        this.solicitacaoId = solicitacaoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataAlteracao = dataAlteracao != null ? dataAlteracao : LocalDateTime.now();
        this.observacao = observacao;
        this.usuarioResponsavelId = usuarioResponsavelId;
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusSolicitacao getSolicitacaoId() { return solicitacaoId; }
    public void setSolicitacaoId(StatusSolicitacao solicitacaoId) { this.solicitacaoId = solicitacaoId; }

    public StatusSolicitacao getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusSolicitacao statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusSolicitacao getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusSolicitacao statusNovo) { this.statusNovo = statusNovo; }

    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(LocalDateTime dataAlteracao) { this.dataAlteracao = dataAlteracao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Long getUsuarioResponsavelId() { return usuarioResponsavelId; }
    public void setUsuarioResponsavelId(Long usuarioResponsavelId) { this.usuarioResponsavelId = usuarioResponsavelId; }
}

package com.br.leone.entity;

import com.br.leone.enums.StatusSolicitacao;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("solicitacao_servico")
public class SolicitacaoServico {

    @Id
    private Long id;

    @Column("comprador_id")
    private Long compradorId;

    @Column("perfil_prestador_id")
    private Long perfilPrestadorId;

    @Column("status")
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column("valor_bruto")
    private BigDecimal valorBruto;

    @Column("comissao_plataforma")
    private BigDecimal comissaoPlataforma;

    @Column("valor_liquido_prestador")
    private BigDecimal valorLiquidoPrestador;

    @Column("data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    protected SolicitacaoServico() {}

    public SolicitacaoServico(Long id, Long compradorId,
                              BigDecimal valorBruto, BigDecimal comissaoPlataforma, BigDecimal valorLiquidoPrestador) {
        this.id = id;
        this.compradorId = compradorId;
        this.perfilPrestadorId = perfilPrestadorId;
        this.status = status != null ? status : StatusSolicitacao.PENDENTE;
        this.valorBruto = valorBruto;
        this.comissaoPlataforma = comissaoPlataforma;
        this.valorLiquidoPrestador = valorLiquidoPrestador;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
        this.dataAtualizacao = dataAtualizacao != null ? dataAtualizacao : LocalDateTime.now();
    }




    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }

    public Long getPerfilPrestadorId() { return perfilPrestadorId; }
    public void setPerfilPrestadorId(Long perfilPrestadorId) { this.perfilPrestadorId = perfilPrestadorId; }

    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }

    public BigDecimal getValorBruto() { return valorBruto; }
    public void setValorBruto(BigDecimal valorBruto) { this.valorBruto = valorBruto; }

    public BigDecimal getComissaoPlataforma() { return comissaoPlataforma; }
    public void setComissaoPlataforma(BigDecimal comissaoPlataforma) { this.comissaoPlataforma = comissaoPlataforma; }

    public BigDecimal getValorLiquidoPrestador() { return valorLiquidoPrestador; }
    public void setValorLiquidoPrestador(BigDecimal valorLiquidoPrestador) { this.valorLiquidoPrestador = valorLiquidoPrestador; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}

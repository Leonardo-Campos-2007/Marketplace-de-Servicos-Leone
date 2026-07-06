package com.br.leone.dto;

import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.enums.StatusSolicitacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitacaoResponseDTO {

    private Long id;
    private Long compradorId;
    private Long perfilPrestadorId;
    private StatusSolicitacao status;
    private BigDecimal valorBruto;
    private BigDecimal comissaoPlataforma;
    private BigDecimal valorLiquidoPrestador;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private List<ItemSolicitacaoResponseDTO> itens;
    private List<HistoricoStatusResponseDTO> historico;

    public SolicitacaoResponseDTO(SolicitacaoServico s, List<ItemSolicitacaoResponseDTO> itens, List<HistoricoStatusResponseDTO> historico) {
        this.id = s.getId();
        this.compradorId = s.getCompradorId();
        this.perfilPrestadorId = s.getPerfilPrestadorId();
        this.status = s.getStatus();
        this.valorBruto = s.getValorBruto();
        this.comissaoPlataforma = s.getComissaoPlataforma();
        this.valorLiquidoPrestador = s.getValorLiquidoPrestador();
        this.dataCriacao = s.getDataCriacao();
        this.dataAtualizacao = s.getDataAtualizacao();
        this.itens = itens;
        this.historico = historico;
    }

    // Getters
    public Long getId() { return id; }
    public Long getCompradorId() { return compradorId; }
    public Long getPerfilPrestadorId() { return perfilPrestadorId; }
    public StatusSolicitacao getStatus() { return status; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getComissaoPlataforma() { return comissaoPlataforma; }
    public BigDecimal getValorLiquidoPrestador() { return valorLiquidoPrestador; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public List<ItemSolicitacaoResponseDTO> getItens() { return itens; }
    public List<HistoricoStatusResponseDTO> getHistorico() { return historico; }
}

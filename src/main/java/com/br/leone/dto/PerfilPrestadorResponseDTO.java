package com.br.leone.dto;

import com.br.leone.entity.PerfilPrestador;
import com.br.leone.enums.StatusAprovacao;
import java.time.LocalDateTime;

public class PerfilPrestadorResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeFantasia;
    private String descricao;
    private String areaAtuacao;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private StatusAprovacao statusAprovacao;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAprovacao;

    // Construtor que transforma a Entidade no DTO de Saída
    public PerfilPrestadorResponseDTO(PerfilPrestador perfil) {
        this.id = perfil.getId();
        this.usuarioId = perfil.getUsuarioId();
        this.nomeFantasia = perfil.getNomeFantasia();
        this.descricao = perfil.getDescricao();
        this.areaAtuacao = perfil.getAreaAtuacao();
        this.avaliacaoMedia = perfil.getAvaliacaoMedia();
        this.totalAvaliacoes = perfil.getTotalAvaliacoes();
        this.statusAprovacao = perfil.getStatusAprovacao();
        this.dataSolicitacao = perfil.getDataSolicitacao();
        this.dataAprovacao = perfil.getDataAprovacao();
    }

    // Apenas Getters são necessários para o Jackson serializar em JSON
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNomeFantasia() { return nomeFantasia; }
    public String getDescricao() { return descricao; }
    public String getAreaAtuacao() { return areaAtuacao; }
    public Double getAvaliacaoMedia() { return avaliacaoMedia; }
    public Integer getTotalAvaliacoes() { return totalAvaliacoes; }
    public StatusAprovacao getStatusAprovacao() { return statusAprovacao; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
}

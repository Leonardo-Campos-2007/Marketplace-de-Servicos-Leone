package com.br.leone.dto;

import com.br.leone.entity.Servico;
import com.br.leone.enums.StatusPublicacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServicoResponseDTO {

    private Long id;
    private Long perfilPrestadorId;
    private Long categoriaServicoId;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Integer tempoEstimado; // Sincronizado para Integer
    private StatusPublicacao statusPublicacao;
    private LocalDateTime dataCriacao;

    public ServicoResponseDTO(Servico servico) {
        this.id = servico.getId();
        this.perfilPrestadorId = servico.getPerfilPrestadorId();
        this.categoriaServicoId = servico.getCategoriaServicoId();
        this.nome = servico.getNome();
        this.descricao = servico.getDescricao();
        this.precoBase = servico.getPrecoBase();
        this.tempoEstimado = servico.getTempoEstimado();
        this.statusPublicacao = servico.getStatusPublicacao();
        this.dataCriacao = servico.getDataCriacao();
    }

    // Getters
    public Long getId() { return id; }
    public Long getPerfilPrestadorId() { return perfilPrestadorId; }
    public Long getCategoriaServicoId() { return categoriaServicoId; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public Integer getTempoEstimado() { return tempoEstimado; }
    public StatusPublicacao getStatusPublicacao() { return statusPublicacao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}

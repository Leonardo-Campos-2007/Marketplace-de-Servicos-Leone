package com.br.leone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServicoRequestDTO {

    @NotBlank(message = "Nome do serviço é obrigatório")
    private String nome;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Preço deve ser maior que zero")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal precoBase;

    @NotNull(message = "Tempo estimado deve ser maior que zero")
    @Min(value = 1, message = "Tempo estimado deve ser maior que zero")
    private Integer tempoEstimado; // Alterado para Integer conforme a Entity

    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaServicoId;

    @NotNull(message = "Perfil do prestador é obrigatório")
    private Long perfilPrestadorId;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }

    public Integer getTempoEstimado() { return tempoEstimado; }
    public void setTempoEstimado(Integer tempoEstimado) { this.tempoEstimado = tempoEstimado; }

    public Long getCategoriaServicoId() { return categoriaServicoId; }
    public void setCategoriaServicoId(Long categoriaServicoId) { this.categoriaServicoId = categoriaServicoId; }

    public Long getPerfilPrestadorId() { return perfilPrestadorId; }
    public void setPerfilPrestadorId(Long perfilPrestadorId) { this.perfilPrestadorId = perfilPrestadorId; }
}
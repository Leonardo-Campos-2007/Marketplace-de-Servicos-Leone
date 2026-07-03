package com.br.leone.entity;

import com.br.leone.enums.StatusPublicacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "servico")
public class Servico {

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "Perfil do prestador é obrigatório")
    @Column("perfil_prestador_id")
    private Long perfilPrestadorId;

    @NotNull(message = "Categoria é obrigatória")
    @Column("categoria_servico_id")
    private Long categoriaServicoId;

    @NotBlank(message = "Nome do serviço é obrigatório")
    @Column("nome")
    private String nome;

    @NotBlank(message = "Descrição é obrigatória")
    @Column("descricao")
    private String descricao;

    @NotNull(message = "Preço deve ser maior que zero")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column("preco_base")
    private BigDecimal precoBase;

    @NotNull(message = "Tempo estimado deve ser maior que zero")
    @Min(value = 1, message = "Tempo estimado deve ser maior que zero")
    @Column("tempo_estimado")
    private Integer tempoEstimado;

    @Column("status_publicacao")
    private StatusPublicacao statusPublicacao = StatusPublicacao.ATIVO;

    @Column("data_criacao")
    private LocalDateTime dataCriacao;

    protected Servico() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Servico(Long perfilPrestadorId, Long categoriaServicoId, String nome, String descricao,
                   BigDecimal precoBase, Integer tempoEstimado, StatusPublicacao statusPublicacao) {
        this.perfilPrestadorId = perfilPrestadorId;
        this.categoriaServicoId = categoriaServicoId;
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.tempoEstimado = tempoEstimado;
        this.statusPublicacao = statusPublicacao != null ? statusPublicacao : StatusPublicacao.ATIVO;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPerfilPrestadorId() { return perfilPrestadorId; }
    public void setPerfilPrestadorId(Long perfilPrestadorId) { this.perfilPrestadorId = perfilPrestadorId; }

    public Long getCategoriaServicoId() { return categoriaServicoId; }
    public void setCategoriaServicoId(Long categoriaServicoId) { this.categoriaServicoId = categoriaServicoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }

    public Integer getTempoEstimado() { return tempoEstimado; }
    public void setTempoEstimado(Integer tempoEstimado) { this.tempoEstimado = tempoEstimado; }

    public StatusPublicacao getStatusPublicacao() { return statusPublicacao; }
    public void setStatusPublicacao(StatusPublicacao statusPublicacao) { this.statusPublicacao = statusPublicacao; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }


}

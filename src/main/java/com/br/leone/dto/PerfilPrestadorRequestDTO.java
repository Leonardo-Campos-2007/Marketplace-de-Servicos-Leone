package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;

public class PerfilPrestadorRequestDTO {

    @NotBlank(message = "Nome fantasia é obrigatório")
    private String nomeFantasia;

    private String descricao;

    @NotBlank(message = "Área de atuação é obrigatória")
    private String areaAtuacao;

    // Construtor padrão obrigatório para o Jackson/Spring
    public PerfilPrestadorRequestDTO() {}

    public PerfilPrestadorRequestDTO(String nomeFantasia, String descricao, String areaAtuacao) {
        this.nomeFantasia = nomeFantasia;
        this.descricao = descricao;
        this.areaAtuacao = areaAtuacao;
    }

    // Getters e Setters
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getAreaAtuacao() { return areaAtuacao; }
    public void setAreaAtuacao(String areaAtuacao) { this.areaAtuacao = areaAtuacao; }
}

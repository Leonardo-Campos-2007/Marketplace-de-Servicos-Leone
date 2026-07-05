package com.br.leone.dto;

import com.br.leone.entity.CategoriaServico;

public record CategoriaResponseDTO(
        Long id,
        String nome,
        String descricao,
        Long categoriaPaiId,
        String statusAprovacao
) {

    public CategoriaResponseDTO(CategoriaServico categoria) {
        this(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getCategoriaPaiId(),
                categoria.getStatusAprovacao().name()
        );
    }
}

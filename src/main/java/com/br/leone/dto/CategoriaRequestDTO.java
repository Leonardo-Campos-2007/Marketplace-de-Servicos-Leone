package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(

        @NotBlank(message = "O nome da categoria é obrigatório.")
        @Size(min = 3, max = 50, message = "O nome da categoria deve ter entre 3 e 50 caracteres.")
        String nome,

        @Size(max = 255, message = "A descrição não pode passar de 255 caracteres.")
        String descricao,

        Long categoriaPaiId
) {
}

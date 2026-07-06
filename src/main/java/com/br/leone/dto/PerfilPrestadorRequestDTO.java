package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;

public record PerfilPrestadorRequestDTO(

        @NotBlank(message = "Nome fantasia é obrigatório")
        String nomeFantasia,

        String descricao,

        @NotBlank(message = "Área de atuação é obrigatória")
        String areaAtuacao

) {}

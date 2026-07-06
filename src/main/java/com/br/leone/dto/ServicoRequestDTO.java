package com.br.leone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServicoRequestDTO(

        @NotBlank(message = "Nome do serviço é obrigatório")
        String nome,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Preço deve ser maior que zero")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal precoBase,

        @NotNull(message = "Tempo estimado deve ser maior que zero")
        @Min(value = 1, message = "Tempo estimado deve ser maior que zero")
        Integer tempoEstimado,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaServicoId,

        @NotNull(message = "Perfil do prestador é obrigatório")
        Long perfilPrestadorId

) {}
package com.br.leone.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarQuantidadeRequestDTO(
        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade mínima é 1")
        @Max(value = 99, message = "Quantidade máxima é 99")
        Integer quantidade
) {}

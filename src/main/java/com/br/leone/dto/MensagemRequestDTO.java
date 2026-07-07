package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;

public record MensagemRequestDTO(

        @NotBlank(message = "Mensagem não pode ser vazia")
        String conteudo

) {}
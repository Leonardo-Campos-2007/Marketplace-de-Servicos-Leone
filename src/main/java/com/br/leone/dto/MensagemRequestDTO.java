package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemRequestDTO(

        @NotBlank(message = "Mensagem não pode ser vazia")
        @Size(max = 2000, message = "Mensagem não pode ultrapassar 2000 caracteres")
        String conteudo

) {}
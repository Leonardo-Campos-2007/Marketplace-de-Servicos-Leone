package com.br.leone.dto;

import com.br.leone.enums.TipoConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 200, message = "O nome deve ter entre 2 e 200 caracteres")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial"
        )
        String senha,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos (apenas números com DDD)")
        String telefone,

        @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 dígitos (apenas números)")
        String cpf,

        @Size(min = 14, max = 14, message = "CNPJ deve ter exatamente 14 dígitos (apenas números)")
        String cnpj,

        @NotNull(message = "Tipo de conta é obrigatório")
        TipoConta tipoConta
) {
}

package com.br.leone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 200, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos (apenas números com DDD)")
        String telefone,

        // Sem @NotBlank aqui. O Regex aceita o campo vazio (caso não queira mudar a senha)
        // ou valida a força da senha caso ela seja digitada.
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^$|^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial"
        )
        String senha
) {
}

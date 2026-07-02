package com.br.leone.dto;

import com.br.leone.enums.Role;
import com.br.leone.enums.TipoConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @Email(message = "E-mail inválido")
        @NotBlank(message = "E-mail é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos")
        String telefone,

        @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
        String cpf,

        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 dígitos")
        String cnpj,

        @NotNull(message = "Tipo de conta é obrigatório")
        TipoConta tipoConta



) {

}

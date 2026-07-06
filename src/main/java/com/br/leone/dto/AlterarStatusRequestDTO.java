package com.br.leone.dto;

import com.br.leone.enums.StatusSolicitacao;

public record AlterarStatusRequestDTO(
        StatusSolicitacao status,
        String observacao
) {}

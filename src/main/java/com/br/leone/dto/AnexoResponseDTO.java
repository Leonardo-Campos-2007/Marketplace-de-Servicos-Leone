package com.br.leone.dto;

import com.br.leone.entity.Anexo;

import java.time.LocalDateTime;

public record AnexoResponseDTO(
        Long id,
        Long mensagemId,
        String nomeArquivoOriginal,
        String tipoConteudo,
        Long tamanhoBytes,
        LocalDateTime dataUpload
) {
    public AnexoResponseDTO(Anexo anexo) {
        this(anexo.getId(), anexo.getMensagemId(), anexo.getNomeArquivoOriginal(),
                anexo.getTipoConteudo(), anexo.getTamanhoBytes(), anexo.getDataUpload());
    }
}
package com.br.leone.service;

import org.springframework.web.multipart.MultipartFile;

public interface ArmazenamentoService {

    /**
     * Salva o arquivo e retorna a URL/path para acessá-lo depois.
     * @param arquivo o conteúdo enviado pelo cliente
     * @param nomeArmazenado identificador interno já sanitizado (UUID + extensão), nunca o nome original do cliente
     */
    String salvar(MultipartFile arquivo, String nomeArmazenado);

    /**
     * Remove o arquivo do armazenamento. Usado tanto em rotinas de limpeza
     * quanto para desfazer um upload parcial quando a gravação de metadados falha.
     */
    void deletar(String urlArquivo);
}
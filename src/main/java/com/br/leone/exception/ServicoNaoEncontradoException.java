package com.br.leone.exception;

public class ServicoNaoEncontradoException extends RuntimeException {
    public ServicoNaoEncontradoException(Long id) {
        super("Serviço não encontrado com id: " + id);
    }
}

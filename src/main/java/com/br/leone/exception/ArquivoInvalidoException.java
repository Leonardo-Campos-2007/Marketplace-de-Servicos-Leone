package com.br.leone.exception;

public class ArquivoInvalidoException extends RuntimeException {
    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
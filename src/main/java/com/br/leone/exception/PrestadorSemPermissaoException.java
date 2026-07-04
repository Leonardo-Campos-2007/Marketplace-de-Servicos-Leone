package com.br.leone.exception;

public class PrestadorSemPermissaoException extends RuntimeException {
    public PrestadorSemPermissaoException(String mensagem) {
        super(mensagem);
    }
}

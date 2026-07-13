package com.br.leone.exception;

public class AnexoJaExisteException extends RuntimeException {
    public AnexoJaExisteException() {
        super("Esta mensagem já possui um anexo.");
    }
}
package com.br.leone.exception;

public class AcessoChatNegadoException extends RuntimeException {
    public AcessoChatNegadoException() {
        super("Você não tem permissão para acessar este chat.");
    }

    public AcessoChatNegadoException(String mensagem) {
        super(mensagem);
    }
}
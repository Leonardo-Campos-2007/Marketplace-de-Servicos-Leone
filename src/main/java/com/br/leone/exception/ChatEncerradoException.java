package com.br.leone.exception;

public class ChatEncerradoException extends RuntimeException {
    public ChatEncerradoException() {
        super("Este chat está encerrado e não aceita novas mensagens.");
    }
}
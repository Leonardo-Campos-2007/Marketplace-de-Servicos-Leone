package com.br.leone.exception;

public class NotificacaoNaoEncontradaException extends RuntimeException {
    public NotificacaoNaoEncontradaException(Long id) {
        super("Notificação não encontrada com id: " + id);
    }
}
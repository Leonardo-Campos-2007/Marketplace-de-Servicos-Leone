package com.br.leone.exception;

public class ChatNaoEncontradoException extends RuntimeException {
    public ChatNaoEncontradoException(Long solicitacaoId) {
        super("Chat não encontrado para a solicitação com id: " + solicitacaoId);
    }
}
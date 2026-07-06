package com.br.leone.exception;

public class SolicitacaoNaoEncontradaException extends RuntimeException {
    public SolicitacaoNaoEncontradaException(Long id) {
        super("Solicitação com ID " + id + " não encontrada.");
    }
}

package com.br.leone.exception;

public class AnexoNaoEncontradoException extends RuntimeException {
    public AnexoNaoEncontradoException() {
        super("Anexo não encontrado para esta mensagem.");
    }
}
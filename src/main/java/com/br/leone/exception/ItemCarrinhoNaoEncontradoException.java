package com.br.leone.exception;

public class ItemCarrinhoNaoEncontradoException extends RuntimeException {
    public ItemCarrinhoNaoEncontradoException(Long id) {
        super("Item de carrinho com ID " + id + " não encontrado.");
    }
}

package com.br.leone.exception;

public class PerfilPrestadorNaoEncontradoException extends RuntimeException {

    public PerfilPrestadorNaoEncontradoException(Long id) {
        super("Perfil de prestador com o ID " + id + " não foi encontrado.");
    }
}

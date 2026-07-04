package com.br.leone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(DadosJaCadastradosException.class)
    public ResponseEntity<Map<String, String>> handleDadosDuplicados(DadosJaCadastradosException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());

        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erros);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CategoriaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleCategoriaNaoEncontrada(CategoriaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CategoriaJaCadastradaException.class)
    public ResponseEntity<Map<String, String>> handleCategoriaJaCadastrada(CategoriaJaCadastradaException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(ServicoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleServicoNaoEncontrado(ServicoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(PrestadorSemPermissaoException.class)
    public ResponseEntity<Map<String, String>> handlePrestadorSemPermissao(PrestadorSemPermissaoException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

}


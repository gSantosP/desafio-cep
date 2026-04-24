package com.example.cep.infrastructure.web;

import com.example.cep.domain.exception.CepInvalidoException;
import com.example.cep.domain.exception.CepNaoEncontradoException;
import com.example.cep.domain.exception.ProvedorCepException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Traduz excecoes de dominio em respostas HTTP padronizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleCepInvalido(CepInvalidoException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCepNaoEncontrado(CepNaoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProvedorCepException.class)
    public ResponseEntity<Map<String, Object>> handleProvedor(ProvedorCepException ex) {
        return build(HttpStatus.BAD_GATEWAY, "Falha ao consultar o provedor de CEP");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String mensagem) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensagem
        );
        return ResponseEntity.status(status).body(body);
    }
}

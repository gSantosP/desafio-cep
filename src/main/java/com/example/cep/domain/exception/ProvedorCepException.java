package com.example.cep.domain.exception;

/**
 * Lancada quando ocorre uma falha tecnica ao chamar o provedor de CEP
 * (indisponibilidade, timeout, resposta invalida etc).
 */
public class ProvedorCepException extends RuntimeException {
    public ProvedorCepException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

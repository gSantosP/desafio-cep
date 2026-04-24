package com.example.cep.domain.exception;

/**
 * Lancada quando o CEP informado nao respeita o formato esperado (8 digitos).
 */
public class CepInvalidoException extends RuntimeException {
    public CepInvalidoException(String cep) {
        super("CEP invalido: '" + cep + "'. Esperado 8 digitos numericos.");
    }
}

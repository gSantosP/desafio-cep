package com.example.cep.domain.exception;

/**
 * Lancada quando o provedor nao encontra o CEP informado.
 */
public class CepNaoEncontradoException extends RuntimeException {
    public CepNaoEncontradoException(String cep) {
        super("CEP nao encontrado: " + cep);
    }
}

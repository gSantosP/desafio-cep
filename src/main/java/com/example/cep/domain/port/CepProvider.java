package com.example.cep.domain.port;

import com.example.cep.domain.model.Endereco;

/**
 * Porta de SAIDA (output port) do dominio para consulta de CEP em provedor externo.
 *
 * O dominio DECLARA esta interface. Qualquer adaptador (WireMock, ViaCEP real,
 * cache Redis, fallback encadeado) vive em {@code infrastructure} e implementa
 * esta porta.
 *
 * Aplica o principio D do SOLID (Dependency Inversion): modulos de alto nivel
 * (o caso de uso {@code ConsultarCepUseCase}) dependem desta abstracao, nao de
 * uma implementacao concreta.
 */
public interface CepProvider {

    /**
     * Consulta um CEP no provedor externo.
     *
     * @param cep CEP ja sanitizado (apenas digitos, 8 caracteres).
     * @return o endereco correspondente.
     * @throws com.example.cep.domain.exception.CepNaoEncontradoException quando o CEP nao existe
     * @throws com.example.cep.domain.exception.ProvedorCepException quando ha falha tecnica
     */
    Endereco buscar(String cep);
}

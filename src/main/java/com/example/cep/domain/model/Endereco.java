package com.example.cep.domain.model;

/**
 * Modelo de dominio que representa um endereco retornado pelo provedor de CEP.
 *
 * E um record puro - zero dependencia de framework. Faz parte do NUCLEO da
 * arquitetura hexagonal (a "hexagona" propriamente dita), portanto nao conhece
 * HTTP, JSON, Spring ou JPA.
 *
 * A estrutura dos campos segue o padrao ViaCEP para facilitar a substituicao
 * do WireMock pelo servico real.
 */
public record Endereco(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String ibge,
        String gia,
        String ddd,
        String siafi
) { }

package com.example.cep.infrastructure.web.dto;

import com.example.cep.domain.model.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta HTTP para consulta de CEP. Isola o contrato externo
 * (API REST) do modelo de dominio - mudancas no dominio nao quebram
 * consumidores e vice-versa.
 */
@Schema(description = "Endereco retornado pela consulta de CEP")
public record EnderecoResponse(
        @Schema(example = "01310-100") String cep,
        @Schema(example = "Avenida Paulista") String logradouro,
        @Schema(example = "de 612 a 1510 - lado par") String complemento,
        @Schema(example = "Bela Vista") String bairro,
        @Schema(example = "Sao Paulo") String localidade,
        @Schema(example = "SP") String uf,
        @Schema(example = "3550308") String ibge,
        @Schema(example = "1004") String gia,
        @Schema(example = "11") String ddd,
        @Schema(example = "7107") String siafi
) {
    public static EnderecoResponse from(Endereco e) {
        return new EnderecoResponse(
                e.cep(), e.logradouro(), e.complemento(), e.bairro(),
                e.localidade(), e.uf(), e.ibge(), e.gia(), e.ddd(), e.siafi()
        );
    }
}

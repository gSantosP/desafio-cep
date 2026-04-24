package com.example.cep.domain.model;

import java.time.LocalDateTime;

/**
 * Log de uma consulta de CEP - entidade de dominio pura (sem anotacoes JPA).
 *
 * A representacao persistente vive em {@code infrastructure.persistence} como
 * {@code ConsultaLogEntity}. Essa separacao garante que o dominio nao conheca
 * detalhes de persistencia - trocar de JPA para MongoDB, por exemplo, nao
 * impactaria esta classe.
 *
 * O identificador e opcional (null ate ser persistido).
 */
public record ConsultaLog(
        Long id,
        String cep,
        LocalDateTime dataHoraConsulta,
        StatusConsulta status,
        String respostaApi
) {
    public static ConsultaLog novo(String cep, StatusConsulta status, String respostaApi) {
        return new ConsultaLog(null, cep, LocalDateTime.now(), status, respostaApi);
    }
}

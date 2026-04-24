package com.example.cep.infrastructure.web.dto;

import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.model.StatusConsulta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Log de uma consulta de CEP")
public record ConsultaLogResponse(
        Long id,
        String cep,
        LocalDateTime dataHoraConsulta,
        StatusConsulta status,
        String respostaApi
) {
    public static ConsultaLogResponse from(ConsultaLog log) {
        return new ConsultaLogResponse(
                log.id(), log.cep(), log.dataHoraConsulta(),
                log.status(), log.respostaApi()
        );
    }
}

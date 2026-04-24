package com.example.cep.infrastructure.persistence;

import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.model.StatusConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representacao JPA de um log de consulta.
 *
 * Fica em {@code infrastructure.persistence}, nao em {@code domain}: isso
 * isola os detalhes de persistencia do nucleo. Metodos {@link #toDomain()} e
 * {@link #fromDomain(ConsultaLog)} fazem a traducao entre as duas camadas.
 */
@Entity
@Table(name = "consulta_log")
public class ConsultaLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(name = "data_hora_consulta", nullable = false)
    private LocalDateTime dataHoraConsulta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;

    @Column(name = "resposta_api", columnDefinition = "TEXT")
    private String respostaApi;

    protected ConsultaLogEntity() { }

    public ConsultaLogEntity(Long id, String cep, LocalDateTime dataHoraConsulta,
                             StatusConsulta status, String respostaApi) {
        this.id = id;
        this.cep = cep;
        this.dataHoraConsulta = dataHoraConsulta;
        this.status = status;
        this.respostaApi = respostaApi;
    }

    public ConsultaLog toDomain() {
        return new ConsultaLog(id, cep, dataHoraConsulta, status, respostaApi);
    }

    public static ConsultaLogEntity fromDomain(ConsultaLog log) {
        return new ConsultaLogEntity(
                log.id(),
                log.cep(),
                log.dataHoraConsulta(),
                log.status(),
                log.respostaApi()
        );
    }

    public Long getId() { return id; }
    public String getCep() { return cep; }
    public LocalDateTime getDataHoraConsulta() { return dataHoraConsulta; }
    public StatusConsulta getStatus() { return status; }
    public String getRespostaApi() { return respostaApi; }
}

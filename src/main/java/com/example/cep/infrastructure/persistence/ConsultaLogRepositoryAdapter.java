package com.example.cep.infrastructure.persistence;

import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.port.ConsultaLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de saida: implementa a porta de dominio
 * {@link ConsultaLogRepository} delegando a persistencia ao Spring Data JPA.
 *
 * E a ponte entre o nucleo (que fala em {@link ConsultaLog}) e a infraestrutura
 * (que fala em {@link ConsultaLogEntity}).
 */
@Component
public class ConsultaLogRepositoryAdapter implements ConsultaLogRepository {

    private final JpaConsultaLogRepository jpaRepository;

    public ConsultaLogRepositoryAdapter(JpaConsultaLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ConsultaLog salvar(ConsultaLog log) {
        ConsultaLogEntity salva = jpaRepository.save(ConsultaLogEntity.fromDomain(log));
        return salva.toDomain();
    }

    @Override
    public List<ConsultaLog> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(ConsultaLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<ConsultaLog> buscarPorCep(String cep) {
        return jpaRepository.findByCepOrderByDataHoraConsultaDesc(cep).stream()
                .map(ConsultaLogEntity::toDomain)
                .toList();
    }
}

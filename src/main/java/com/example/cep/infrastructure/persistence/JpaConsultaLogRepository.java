package com.example.cep.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository usado internamente pelo adaptador
 * {@link ConsultaLogRepositoryAdapter}. Nao e exposto ao dominio.
 */
interface JpaConsultaLogRepository extends JpaRepository<ConsultaLogEntity, Long> {
    List<ConsultaLogEntity> findByCepOrderByDataHoraConsultaDesc(String cep);
}

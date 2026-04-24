package com.example.cep.domain.port;

import com.example.cep.domain.model.ConsultaLog;

import java.util.List;

/**
 * Porta de SAIDA (output port) para persistencia de logs de consulta.
 *
 * O dominio declara este contrato - o adaptador concreto vive em
 * {@code infrastructure.persistence} (implementacao JPA). Trocar de JPA para
 * Mongo, JDBC puro ou ate um CSV local seria uma troca de implementacao desta
 * porta sem impactar o dominio.
 */
public interface ConsultaLogRepository {

    ConsultaLog salvar(ConsultaLog log);

    List<ConsultaLog> listarTodos();

    List<ConsultaLog> buscarPorCep(String cep);
}

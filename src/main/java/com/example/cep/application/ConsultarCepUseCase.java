package com.example.cep.application;

import com.example.cep.domain.exception.CepInvalidoException;
import com.example.cep.domain.exception.CepNaoEncontradoException;
import com.example.cep.domain.exception.ProvedorCepException;
import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.model.Endereco;
import com.example.cep.domain.model.StatusConsulta;
import com.example.cep.domain.port.CepProvider;
import com.example.cep.domain.port.ConsultaLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Caso de uso: "Consultar CEP".
 *
 * Orquestra o fluxo principal da aplicacao:
 *  1. Valida e sanitiza o CEP informado.
 *  2. Delega a busca a porta {@link CepProvider}.
 *  3. Registra o log da consulta via {@link ConsultaLogRepository},
 *     independentemente do resultado (sucesso, nao encontrado ou erro).
 *
 * Esta classe pertence a camada de APLICACAO da arquitetura hexagonal.
 * Depende exclusivamente de portas (interfaces de dominio), jamais de
 * adaptadores concretos.
 *
 * A anotacao {@code @Service} e o unico "toque" de Spring nesta camada, e
 * serve apenas para que o container realize o wiring automatico dos adaptadores
 * concretos injetados via construtor.
 */
@Service
public class ConsultarCepUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCepUseCase.class);
    private static final Pattern SOMENTE_DIGITOS = Pattern.compile("\\d{8}");

    private final CepProvider cepProvider;
    private final ConsultaLogRepository logRepository;
    private final ObjectMapper objectMapper;

    public ConsultarCepUseCase(CepProvider cepProvider,
                               ConsultaLogRepository logRepository,
                               ObjectMapper objectMapper) {
        this.cepProvider = cepProvider;
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
    }

    public Endereco executar(String cepBruto) {
        String cep = sanitizar(cepBruto);

        try {
            Endereco endereco = cepProvider.buscar(cep);
            registrarLog(cep, StatusConsulta.SUCESSO, serializar(endereco));
            log.info("CEP {} consultado com sucesso", cep);
            return endereco;

        } catch (CepNaoEncontradoException e) {
            registrarLog(cep, StatusConsulta.NAO_ENCONTRADO, "{\"erro\":true}");
            log.info("CEP {} nao encontrado", cep);
            throw e;

        } catch (ProvedorCepException e) {
            registrarLog(cep, StatusConsulta.ERRO, "{\"erro\":\"" + escape(e.getMessage()) + "\"}");
            log.error("Erro ao consultar CEP {} no provedor", cep, e);
            throw e;
        }
    }

    private void registrarLog(String cep, StatusConsulta status, String respostaJson) {
        ConsultaLog registro = ConsultaLog.novo(cep, status, respostaJson);
        ConsultaLog salvo = logRepository.salvar(registro);
        log.debug("Log persistido: id={}, cep={}, status={}", salvo.id(), salvo.cep(), salvo.status());
    }

    private String sanitizar(String cepBruto) {
        if (cepBruto == null) {
            throw new CepInvalidoException("null");
        }
        String apenasDigitos = cepBruto.replaceAll("\\D", "");
        if (!SOMENTE_DIGITOS.matcher(apenasDigitos).matches()) {
            throw new CepInvalidoException(cepBruto);
        }
        return apenasDigitos;
    }

    private String serializar(Endereco endereco) {
        try {
            return objectMapper.writeValueAsString(endereco);
        } catch (JsonProcessingException e) {
            log.warn("Falha ao serializar endereco para log", e);
            return "{}";
        }
    }

    private String escape(String valor) {
        return valor == null ? "" : valor.replace("\"", "\\\"");
    }
}

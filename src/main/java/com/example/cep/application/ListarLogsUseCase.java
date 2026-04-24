package com.example.cep.application;

import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.port.ConsultaLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para consulta dos logs persistidos.
 * Separado de {@link ConsultarCepUseCase} para honrar o principio S do SOLID:
 * mudancas em como os logs sao listados nao impactam o fluxo principal.
 */
@Service
public class ListarLogsUseCase {

    private final ConsultaLogRepository repository;

    public ListarLogsUseCase(ConsultaLogRepository repository) {
        this.repository = repository;
    }

    public List<ConsultaLog> listarTodos() {
        return repository.listarTodos();
    }

    public List<ConsultaLog> listarPorCep(String cep) {
        String cepLimpo = cep == null ? "" : cep.replaceAll("\\D", "");
        return repository.buscarPorCep(cepLimpo);
    }
}

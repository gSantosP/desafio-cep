package com.example.cep.infrastructure.web;

import com.example.cep.application.ConsultarCepUseCase;
import com.example.cep.application.ListarLogsUseCase;
import com.example.cep.domain.model.Endereco;
import com.example.cep.infrastructure.web.dto.ConsultaLogResponse;
import com.example.cep.infrastructure.web.dto.EnderecoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de ENTRADA HTTP. Traduz requisicoes REST em chamadas aos casos
 * de uso do dominio. Zero logica de negocio aqui.
 */
@RestController
@RequestMapping("/api/cep")
@Tag(name = "CEP", description = "Consulta de CEPs e inspecao dos logs")
public class CepController {

    private final ConsultarCepUseCase consultarCepUseCase;
    private final ListarLogsUseCase listarLogsUseCase;

    public CepController(ConsultarCepUseCase consultarCepUseCase,
                         ListarLogsUseCase listarLogsUseCase) {
        this.consultarCepUseCase = consultarCepUseCase;
        this.listarLogsUseCase = listarLogsUseCase;
    }

    @GetMapping("/{cep}")
    @Operation(summary = "Consulta um CEP no provedor externo e grava log da operacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CEP encontrado",
                    content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
            @ApiResponse(responseCode = "400", description = "CEP com formato invalido"),
            @ApiResponse(responseCode = "404", description = "CEP nao encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha ao consultar o provedor externo")
    })
    public ResponseEntity<EnderecoResponse> consultar(
            @Parameter(description = "CEP com 8 digitos (aceita mascara)", example = "01310-100")
            @PathVariable String cep) {
        Endereco endereco = consultarCepUseCase.executar(cep);
        return ResponseEntity.ok(EnderecoResponse.from(endereco));
    }

    @GetMapping("/logs")
    @Operation(summary = "Lista todos os logs de consulta persistidos")
    public ResponseEntity<List<ConsultaLogResponse>> listarLogs() {
        List<ConsultaLogResponse> resposta = listarLogsUseCase.listarTodos().stream()
                .map(ConsultaLogResponse::from)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/logs/{cep}")
    @Operation(summary = "Lista os logs de consulta de um CEP especifico",
               description = "Retorna os logs em ordem decrescente de data/hora")
    public ResponseEntity<List<ConsultaLogResponse>> listarLogsPorCep(
            @Parameter(description = "CEP (apenas digitos ou com mascara)", example = "01310100")
            @PathVariable String cep) {
        List<ConsultaLogResponse> resposta = listarLogsUseCase.listarPorCep(cep).stream()
                .map(ConsultaLogResponse::from)
                .toList();
        return ResponseEntity.ok(resposta);
    }
}

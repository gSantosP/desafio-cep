package com.example.cep.infrastructure.client;

import com.example.cep.domain.exception.CepNaoEncontradoException;
import com.example.cep.domain.exception.ProvedorCepException;
import com.example.cep.domain.model.Endereco;
import com.example.cep.domain.port.CepProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador de saida: implementacao HTTP da porta {@link CepProvider},
 * compativel com o contrato da API do ViaCEP.
 *
 * Em desenvolvimento a URL aponta para o WireMock (http://wiremock:8080).
 * Para ir para producao basta trocar a propriedade {@code cep.provider.base-url}
 * para {@code https://viacep.com.br} - o contrato JSON e o mesmo.
 */
@Component
public class ViaCepClient implements CepProvider {

    private static final Logger log = LoggerFactory.getLogger(ViaCepClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ViaCepClient(RestTemplate restTemplate,
                        ObjectMapper objectMapper,
                        @Value("${cep.provider.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    @Override
    public Endereco buscar(String cep) {
        String url = String.format("%s/ws/%s/json/", baseUrl, cep);
        log.info("Consultando provedor de CEP: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();

            if (body == null || body.isBlank()) {
                throw new ProvedorCepException("Resposta vazia do provedor", null);
            }

            JsonNode json = objectMapper.readTree(body);

            // ViaCEP retorna HTTP 200 com {"erro": true} para CEPs inexistentes.
            if (json.has("erro") && json.get("erro").asBoolean()) {
                throw new CepNaoEncontradoException(cep);
            }

            return objectMapper.treeToValue(json, Endereco.class);

        } catch (CepNaoEncontradoException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ProvedorCepException("Falha ao chamar provedor de CEP", e);
        } catch (Exception e) {
            throw new ProvedorCepException("Resposta invalida do provedor de CEP", e);
        }
    }
}

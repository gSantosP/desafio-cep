package com.example.cep.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao da documentacao OpenAPI/Swagger.
 * Disponivel em /swagger-ui.html e /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Desafio CEP - API")
                        .description("API para consulta de CEP em provedor externo com log de auditoria em banco relacional.")
                        .version("1.0.0")
                        .contact(new Contact().name("Desafio Tecnico"))
                        .license(new License().name("MIT")));
    }
}

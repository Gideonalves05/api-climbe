package com.climbe.api_climbe.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String nomeEsquema = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Climbe")
                        .description("API monolítica para gestão de contratos, autenticação e fluxo operacional da Climbe.")
                        .version("v1")
                        .contact(new Contact().name("Climbe Investimentos")))
                .components(new Components()
                        .addSecuritySchemes(nomeEsquema, new SecurityScheme()
                                .name(nomeEsquema)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(nomeEsquema));
    }
}

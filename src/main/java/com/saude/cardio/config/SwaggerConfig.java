package com.saude.cardio.config;

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
    public OpenAPI openAPI() {
        return new OpenAPI()
                // Mantém os seus dados originais
                .info(new Info()
                        .title("Sistema de Acompanhamento de Saude Cardiaca")
                        .description("API REST para acompanhamento de saude cardiaca")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Saude Cardio")))
                // Adiciona o requisito de segurança global (O botão de cadeado)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // Configura o formato do token esperado (JWT)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
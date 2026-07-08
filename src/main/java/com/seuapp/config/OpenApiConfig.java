package com.seuapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI barbeariaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Barbearia API")
                        .description("""
                                API REST para gerenciamento de usuarios, servicos e agendamentos de uma barbearia.

                                Regras principais de agendamento:
                                - novos agendamentos recebem status inicial PENDENTE;
                                - conflitos sao validados por intervalo, considerando a duracao do servico;
                                - agendamentos CANCELADOS nao bloqueiam novos horarios;
                                - cancelamento deve ser feito por PATCH /agendamentos/{id}/cancelar;
                                - domingos sao bloqueados;
                                - horario de funcionamento atual fixo: 08:00 as 18:00;
                                - horarios disponiveis podem ser consultados em GET /agendamentos/horarios-disponiveis.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enio Jr.")
                                .email("eniojr100@gmail.com"))
                        .license(new License()
                                .name("Uso educacional/portfolio")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}

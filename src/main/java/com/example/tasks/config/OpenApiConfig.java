package com.example.tasks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .info(new Info().title("Task API").version("v1"))
      .servers(List.of(new Server().url("/")))
      .components(new io.swagger.v3.oas.models.Components()
        .addSecuritySchemes("bearerAuth",
          new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
      .security(List.of(new SecurityRequirement().addList("bearerAuth")));
  }
}

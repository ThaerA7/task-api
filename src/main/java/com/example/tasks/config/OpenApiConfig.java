package com.example.tasks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    // the same origin where Swagger UI is served (the forwarded Codespaces URL)
    return new OpenAPI().servers(List.of(new Server().url("/")));
  }
}

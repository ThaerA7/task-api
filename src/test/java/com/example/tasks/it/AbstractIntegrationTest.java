package com.example.tasks.it;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = AbstractIntegrationTest.DockerPostgresInitializer.class)
public abstract class AbstractIntegrationTest {

  public static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("tasks")
          .withUsername("taskuser")
          .withPassword("taskpass");

  @BeforeAll
  static void start() {
    if (!POSTGRES.isRunning()) POSTGRES.start();
  }

  public static class DockerPostgresInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override public void initialize(ConfigurableApplicationContext context) {
      TestPropertyValues.of(
          "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
          "spring.datasource.username=" + POSTGRES.getUsername(),
          "spring.datasource.password=" + POSTGRES.getPassword(),
          "spring.jpa.hibernate.ddl-auto=create-drop",
          "JWT_SECRET=test-secret-for-it-should-be-long-enough-0123456789"
      ).applyTo(context.getEnvironment());
    }
  }
}

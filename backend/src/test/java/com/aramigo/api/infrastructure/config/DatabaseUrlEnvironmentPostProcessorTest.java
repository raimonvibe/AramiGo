package com.aramigo.api.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

  private DatabaseUrlEnvironmentPostProcessor processor;
  private MockEnvironment environment;

  @BeforeEach
  void setUp() {
    processor = new DatabaseUrlEnvironmentPostProcessor();
    environment = new MockEnvironment();
  }

  @Test
  void convertsANeonConnectionStringIntoJdbcForm() {
    withDatabaseUrl(
        "postgresql://neondb_owner:secret@ep-example-12345.c-2.us-east-2.aws.neon.tech/neondb?sslmode=require");

    assertEquals(
        "jdbc:postgresql://ep-example-12345.c-2.us-east-2.aws.neon.tech/neondb?sslmode=require",
        environment.getProperty("spring.datasource.url"));
    assertEquals("neondb_owner", environment.getProperty("spring.datasource.username"));
    assertEquals("secret", environment.getProperty("spring.datasource.password"));
  }

  @Test
  void acceptsThePostgresScheme() {
    withDatabaseUrl("postgres://user:pw@db.example.com:6543/app");

    assertEquals(
        "jdbc:postgresql://db.example.com:6543/app",
        environment.getProperty("spring.datasource.url"));
  }

  @Test
  void dropsTheDefaultPort() {
    withDatabaseUrl("postgresql://user:pw@db.example.com:5432/app");

    assertEquals("jdbc:postgresql://db.example.com/app", environment.getProperty("spring.datasource.url"));
  }

  @Test
  void dropsLibpqOnlyParametersTheDriverWouldReject() {
    withDatabaseUrl("postgresql://user:pw@host/db?sslmode=require&channel_binding=require");

    assertEquals(
        "jdbc:postgresql://host/db?sslmode=require",
        environment.getProperty("spring.datasource.url"));
  }

  @Test
  void decodesAPercentEncodedPassword() {
    withDatabaseUrl("postgresql://user:p%40ss%2Fword@host/db");

    assertEquals("p@ss/word", environment.getProperty("spring.datasource.password"));
  }

  @Test
  void leavesAnExplicitJdbcUrlAlone() {
    withDatabaseUrl("jdbc:postgresql://host/db?sslmode=require");

    assertFalse(
        environment.getPropertySources().contains("aramigo-database-url"),
        "a jdbc: URL needs no conversion");
  }

  @Test
  void separateCredentialsWinOverThoseEmbeddedInTheUrl() {
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "test",
                Map.of(
                    "DATABASE_URL", "postgresql://embedded:embeddedpw@host/db",
                    "DATABASE_USERNAME", "explicit",
                    "DATABASE_PASSWORD", "explicitpw")));
    processor.postProcessEnvironment(environment, null);

    MapPropertySource added =
        (MapPropertySource) environment.getPropertySources().get("aramigo-database-url");

    // The URL is still converted, but the embedded credentials must not shadow
    // the ones set explicitly.
    assertEquals("jdbc:postgresql://host/db", added.getProperty("spring.datasource.url"));
    assertNull(added.getProperty("spring.datasource.username"));
    assertNull(added.getProperty("spring.datasource.password"));
  }

  @Test
  void ignoresAnEmptyValue() {
    withDatabaseUrl("");

    assertFalse(environment.getPropertySources().contains("aramigo-database-url"));
  }

  @Test
  void ignoresSomethingThatIsNotAConnectionString() {
    withDatabaseUrl("not a url at all");

    assertFalse(environment.getPropertySources().contains("aramigo-database-url"));
  }

  private void withDatabaseUrl(String value) {
    environment.setProperty("DATABASE_URL", value);
    processor.postProcessEnvironment(environment, null);
  }
}

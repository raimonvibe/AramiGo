package com.aramigo.api.infrastructure.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Accepts a Postgres connection string in the shape hosting providers actually
 * hand out and turns it into what the JDBC driver expects.
 *
 * <p>Neon, Supabase, Render and Heroku all give you a libpq URI:
 *
 * <pre>postgresql://user:password@host/dbname?sslmode=require</pre>
 *
 * <p>The JDBC driver cannot read that — it wants {@code jdbc:postgresql://host/dbname}
 * with the credentials supplied separately. Pasting the provider's string straight
 * into {@code DATABASE_URL} is the obvious thing to do and it would fail at startup
 * with an unhelpful driver error, so this converts it instead of complaining.
 *
 * <p>A value already starting with {@code jdbc:} is left untouched.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String PROPERTY_SOURCE_NAME = "aramigo-database-url";
  private static final String DATABASE_URL = "DATABASE_URL";
  private static final int DEFAULT_POSTGRES_PORT = 5432;

  /**
   * Query parameters the PostgreSQL JDBC driver understands. Providers append
   * libpq-only options such as {@code channel_binding} that the driver rejects,
   * so anything unrecognised is dropped rather than passed through.
   */
  private static final List<String> JDBC_SAFE_PARAMS =
      List.of("sslmode", "ssl", "sslrootcert", "options", "currentSchema", "ApplicationName");

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication app) {
    String raw = environment.getProperty(DATABASE_URL);
    if (raw == null || raw.isBlank()) {
      return;
    }

    String trimmed = raw.trim();
    if (trimmed.startsWith("jdbc:")) {
      return;
    }
    if (!trimmed.startsWith("postgres://") && !trimmed.startsWith("postgresql://")) {
      return;
    }

    Map<String, Object> converted = convert(trimmed, environment);
    if (!converted.isEmpty()) {
      // addFirst so this wins over the ${DATABASE_URL:...} placeholder in
      // application.properties, which would otherwise pass the libpq URI through.
      environment
          .getPropertySources()
          .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, converted));
    }
  }

  private Map<String, Object> convert(String connectionString, ConfigurableEnvironment env) {
    URI uri;
    try {
      uri = new URI(connectionString);
    } catch (URISyntaxException malformed) {
      // Leave it alone: a clear driver error beats a silent half-conversion.
      return Map.of();
    }

    String host = uri.getHost();
    String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
    if (host == null || host.isBlank() || database.isBlank()) {
      return Map.of();
    }

    StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(host);
    if (uri.getPort() != -1 && uri.getPort() != DEFAULT_POSTGRES_PORT) {
      jdbcUrl.append(':').append(uri.getPort());
    }
    jdbcUrl.append('/').append(database);

    String query = safeQuery(uri.getRawQuery());
    if (!query.isEmpty()) {
      jdbcUrl.append('?').append(query);
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("spring.datasource.url", jdbcUrl.toString());

    // Credentials embedded in the URI are the norm; explicit env vars still win.
    String userInfo = uri.getUserInfo();
    if (userInfo != null && !userInfo.isBlank()) {
      int separator = userInfo.indexOf(':');
      String user = separator < 0 ? userInfo : userInfo.substring(0, separator);
      String password = separator < 0 ? null : userInfo.substring(separator + 1);

      if (!user.isBlank() && isBlank(env.getProperty("DATABASE_USERNAME"))) {
        properties.put("spring.datasource.username", decode(user));
      }
      if (password != null && !password.isBlank() && isBlank(env.getProperty("DATABASE_PASSWORD"))) {
        properties.put("spring.datasource.password", decode(password));
      }
    }

    return properties;
  }

  private static String safeQuery(String rawQuery) {
    if (rawQuery == null || rawQuery.isBlank()) {
      return "";
    }
    Map<String, String> kept = new LinkedHashMap<>();
    for (String pair : rawQuery.split("&")) {
      int equals = pair.indexOf('=');
      String key = equals < 0 ? pair : pair.substring(0, equals);
      if (JDBC_SAFE_PARAMS.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(key))) {
        kept.put(key, equals < 0 ? "" : pair.substring(equals + 1));
      }
    }
    return kept.entrySet().stream()
        .map(entry -> entry.getValue().isEmpty() ? entry.getKey() : entry.getKey() + "=" + entry.getValue())
        .reduce((a, b) -> a + "&" + b)
        .orElse("");
  }

  private static String decode(String value) {
    return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  @Override
  public int getOrder() {
    // After config data (so .env and application.properties are already loaded).
    return Ordered.LOWEST_PRECEDENCE;
  }
}

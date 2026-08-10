package com.aramigo.api.infrastructure.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness probe for Render / GitHub keepalive pings.
 * Kept tiny and dependency-free so a sleeping free-tier instance can answer
 * as soon as the process is up, before heavier systems matter.
 */
@RestController
public class HealthController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "status", "healthy",
        "service", "aramigo-api",
        "timestamp", Instant.now().toString());
  }
}

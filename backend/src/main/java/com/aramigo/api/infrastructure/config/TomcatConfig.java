package com.aramigo.api.infrastructure.config;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

  /**
   * Tomcat 11.0.27 rejects requests whose Accept-Language lists more than 10 locales, and the error response has
   * no CORS headers, so the browser only shows a CORS failure. Browsers with many languages set send more than 10.
   */
  static final int MAX_LOCALE_COUNT = 100;

  @Bean
  TomcatConnectorCustomizer maxLocaleCountCustomizer() {
    return connector -> connector.setMaxLocaleCount(MAX_LOCALE_COUNT);
  }
}

package com.aramigo.api.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClientTimeZoneTest {

  @Test
  void takesTheZoneTheBrowserReports() {
    assertThat(ClientTimeZone.resolve("Europe/Amsterdam")).isEqualTo(ZoneId.of("Europe/Amsterdam"));
    assertThat(ClientTimeZone.resolve("Pacific/Auckland")).isEqualTo(ZoneId.of("Pacific/Auckland"));
  }

  @Test
  void toleratesSurroundingWhitespace() {
    assertThat(ClientTimeZone.resolve("  Asia/Baghdad  ")).isEqualTo(ZoneId.of("Asia/Baghdad"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "Mars/Olympus", "not a zone", "../../etc/passwd", "+99:00"})
  void fallsBackToUtcRatherThanFailingTheRequest(String header) {
    // The value is whatever a client chose to send. A learner with an odd browser
    // should still be able to finish a lesson, just with the day counted in UTC.
    assertThat(ClientTimeZone.resolve(header)).isEqualTo(ZoneOffset.UTC);
  }

  @Test
  void treatsAMissingHeaderAsUtc() {
    assertThat(ClientTimeZone.resolve(null)).isEqualTo(ZoneOffset.UTC);
  }

  @Test
  void acceptsAPlainOffsetToo() {
    // Not what the browser sends, but valid and harmless to honour.
    assertThat(ClientTimeZone.resolve("+02:00")).isEqualTo(ZoneId.of("+02:00"));
  }
}

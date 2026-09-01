package com.aramigo.api.infrastructure.web;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.util.StringUtils;

/**
 * The learner's own time zone, as reported by their browser.
 *
 * <p>Streaks are counted in whole days, so "which day is it" has to be answered
 * where the learner is. Counted in UTC, the day rolls over at 1pm for someone in
 * New Zealand and at 4pm on the American west coast — an evening habit would
 * register as two days in one and then a gap, and break a streak that was
 * actually kept.
 *
 * <p>A zone id rather than a fixed offset, because an offset is wrong twice a
 * year: a learner on {@code Europe/Amsterdam} is +01:00 in January and +02:00 in
 * July, and only the zone knows which applies on the day being counted.
 *
 * <p>The value comes from the client and is therefore untrusted. Anything
 * unrecognised falls back to UTC rather than failing the request — a learner
 * with an odd browser should still be able to finish a lesson.
 */
public final class ClientTimeZone {

  public static final String HEADER = "X-Time-Zone";

  private ClientTimeZone() {}

  public static ZoneId resolve(String header) {
    if (!StringUtils.hasText(header)) {
      return ZoneOffset.UTC;
    }
    try {
      return ZoneId.of(header.trim());
    } catch (RuntimeException unrecognised) {
      return ZoneOffset.UTC;
    }
  }
}

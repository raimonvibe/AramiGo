package com.aramigo.api.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Lets a test skip forward without sleeping.
 *
 * <p>Shared rather than nested in one test: review intervals are measured in days,
 * so anything exercising them has to move the clock a long way.
 */
final class MutableClock extends Clock {

  private Instant now;

  MutableClock(Instant now) {
    this.now = now;
  }

  void advance(Duration amount) {
    now = now.plus(amount);
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return now;
  }
}

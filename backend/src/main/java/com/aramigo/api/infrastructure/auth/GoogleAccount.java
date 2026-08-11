package com.aramigo.api.infrastructure.auth;

/** A Google identity that has passed signature, issuer, audience and expiry checks. */
public record GoogleAccount(String subject, String displayName, String email) {

  /** Google's {@code sub} is the only stable identifier — names and emails change. */
  public String identityKey() {
    return "google:" + subject;
  }
}

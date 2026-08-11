package com.aramigo.api.infrastructure.auth;

/**
 * Who is making this request, as far as the API is concerned.
 *
 * @param key opaque identity for the learning module ({@code guest:…} or {@code google:…})
 * @param displayName name to greet them with, null for guests
 * @param signedIn true once a Google token has been verified
 */
public record LearnerIdentity(String key, String displayName, boolean signedIn) {

  public static LearnerIdentity guest(String key) {
    return new LearnerIdentity(key, null, false);
  }

  public static LearnerIdentity of(GoogleAccount account) {
    return new LearnerIdentity(account.identityKey(), account.displayName(), true);
  }
}

package com.aramigo.api.infrastructure.auth;

/**
 * Who is making this request, as far as the API is concerned.
 *
 * @param key opaque identity for the learning module ({@code guest:…} or {@code google:…})
 * @param displayName name to greet them with, null for guests
 * @param signedIn true once a Google token has been verified
 * @param email Google email claim when signed in; never persisted
 * @param pictureUrl Google profile image URL when signed in; never persisted
 */
public record LearnerIdentity(
    String key, String displayName, boolean signedIn, String email, String pictureUrl) {

  public static LearnerIdentity guest(String key) {
    return new LearnerIdentity(key, null, false, null, null);
  }

  public static LearnerIdentity of(GoogleAccount account) {
    return new LearnerIdentity(
        account.identityKey(),
        account.displayName(),
        true,
        account.email(),
        account.pictureUrl());
  }
}

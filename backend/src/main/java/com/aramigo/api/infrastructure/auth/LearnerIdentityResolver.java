package com.aramigo.api.infrastructure.auth;

import java.util.Optional;

import com.aramigo.api.domain.exception.UnauthorizedException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Turns request headers into a learner identity.
 *
 * <p>Signing in is optional: without a token the caller keeps their guest identity,
 * so progress made before signing in is never lost. A token that is present but
 * invalid is an error rather than a silent downgrade — otherwise an expired session
 * would quietly strand someone in a different account's progress.
 */
@Component
public class LearnerIdentityResolver {

  public static final String GUEST_PREFIX = "guest:";
  private static final String BEARER = "Bearer ";
  private static final String FALLBACK_GUEST_KEY = GUEST_PREFIX + "anonymous";

  private final ObjectProvider<GoogleTokenVerifier> verifier;

  public LearnerIdentityResolver(ObjectProvider<GoogleTokenVerifier> verifier) {
    this.verifier = verifier;
  }

  public LearnerIdentity resolve(String authorizationHeader, String guestKeyHeader) {
    String idToken = bearerToken(authorizationHeader);
    if (idToken == null) {
      return LearnerIdentity.guest(guestKey(guestKeyHeader));
    }

    GoogleTokenVerifier configured = verifier.getIfAvailable();
    if (configured == null) {
      throw new UnauthorizedException("Google sign-in is not configured on this server");
    }

    return configured
        .verify(idToken)
        .map(LearnerIdentity::of)
        .orElseThrow(() -> new UnauthorizedException("Sign-in expired — please sign in again"));
  }

  /** Normalises whatever the browser sent into a namespaced guest key. */
  public String guestKey(String guestKeyHeader) {
    if (!StringUtils.hasText(guestKeyHeader)) {
      return FALLBACK_GUEST_KEY;
    }
    String trimmed = guestKeyHeader.trim();
    return trimmed.startsWith(GUEST_PREFIX) ? trimmed : GUEST_PREFIX + trimmed;
  }

  private static String bearerToken(String authorizationHeader) {
    return Optional.ofNullable(authorizationHeader)
        .filter(header -> header.regionMatches(true, 0, BEARER, 0, BEARER.length()))
        .map(header -> header.substring(BEARER.length()).trim())
        .filter(StringUtils::hasText)
        .orElse(null);
  }
}

package com.aramigo.api.infrastructure.auth;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Verifies Google ID tokens: RS256 signature against Google's published keys,
 * plus issuer, audience and expiry.
 *
 * <p>Only the {@code sub} claim is trusted as identity. The client sends the token
 * on each request; nothing about the caller is taken on faith.
 */
public class GoogleTokenVerifier {

  private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

  private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

  /** Google issues both spellings; both are legitimate. */
  private static final List<String> VALID_ISSUERS =
      List.of("https://accounts.google.com", "accounts.google.com");

  private final NimbusJwtDecoder decoder;

  public GoogleTokenVerifier(String clientId) {
    this.decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    this.decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(), issuerValidator(), audienceValidator(clientId)));
  }

  /** @return the verified account, or empty when the token is missing or invalid */
  public Optional<GoogleAccount> verify(String idToken) {
    if (idToken == null || idToken.isBlank()) {
      return Optional.empty();
    }
    try {
      Jwt jwt = decoder.decode(idToken);
      String subject = jwt.getSubject();
      if (subject == null || subject.isBlank()) {
        return Optional.empty();
      }
      return Optional.of(
          new GoogleAccount(
              subject,
              jwt.getClaimAsString("name"),
              jwt.getClaimAsString("email"),
              jwt.getClaimAsString("picture")));
    } catch (JwtException e) {
      // Expected whenever a token expires; not worth a stack trace.
      log.debug("Rejected Google ID token: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private static OAuth2TokenValidator<Jwt> issuerValidator() {
    return jwt ->
        VALID_ISSUERS.contains(jwt.getIssuer() == null ? null : jwt.getIssuer().toString())
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_issuer", "Token was not issued by Google", null));
  }

  /** Without this, a valid Google token minted for any other app would be accepted. */
  private static OAuth2TokenValidator<Jwt> audienceValidator(String clientId) {
    return jwt ->
        jwt.getAudience() != null && jwt.getAudience().contains(clientId)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_audience", "Token was issued for a different app", null));
  }
}

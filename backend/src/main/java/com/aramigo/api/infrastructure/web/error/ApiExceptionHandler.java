package com.aramigo.api.infrastructure.web.error;

import com.aramigo.api.domain.exception.DomainException;
import com.aramigo.api.domain.exception.LessonIncompleteException;
import com.aramigo.api.domain.exception.LessonLockedException;
import com.aramigo.api.domain.exception.NotFoundException;
import com.aramigo.api.domain.exception.OutOfEnergyException;
import com.aramigo.api.domain.exception.UnauthorizedException;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Every failure leaves here as {@code {status, code, message}}.
 *
 * <p>The {@code code} lets the UI react (show a timer, offer sign-in) and the
 * {@code message} is written to be read by a learner, not a developer — these
 * strings end up on screen.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
    return respond(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ErrorResponse> unauthorized(UnauthorizedException ex) {
    return respond(HttpStatus.UNAUTHORIZED, "unauthorized", ex.getMessage());
  }

  @ExceptionHandler(LessonLockedException.class)
  ResponseEntity<ErrorResponse> locked(LessonLockedException ex) {
    return respond(HttpStatus.FORBIDDEN, "lesson_locked", ex.getMessage());
  }

  @ExceptionHandler(LessonIncompleteException.class)
  ResponseEntity<ErrorResponse> incomplete(LessonIncompleteException ex) {
    return respond(HttpStatus.CONFLICT, "lesson_incomplete", ex.getMessage());
  }

  @ExceptionHandler(OutOfEnergyException.class)
  ResponseEntity<ErrorResponse> energy(OutOfEnergyException ex) {
    long retryAfter = Math.max(1, ex.getSecondsUntilNextEnergy());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter))
        .body(new ErrorResponse("error", "out_of_energy", ex.getMessage()));
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ErrorResponse> domain(DomainException ex) {
    return respond(HttpStatus.BAD_REQUEST, "invalid_request", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException ex) {
    return respond(HttpStatus.BAD_REQUEST, "invalid_request", "That request was missing something");
  }

  /** Last resort: log the detail, tell the learner nothing they can't act on. */
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(Exception ex) {
    log.error("Unhandled failure serving request", ex);
    return respond(
        HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Something went wrong on our side");
  }

  private static ResponseEntity<ErrorResponse> respond(
      HttpStatus status, String code, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse("error", code, message));
  }
}

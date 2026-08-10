package com.aramigo.api.infrastructure.web.error;

import com.aramigo.api.domain.exception.DomainException;
import com.aramigo.api.domain.exception.LessonLockedException;
import com.aramigo.api.domain.exception.NotFoundException;
import com.aramigo.api.domain.exception.OutOfEnergyException;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex));
  }

  @ExceptionHandler(LessonLockedException.class)
  ResponseEntity<ErrorResponse> locked(LessonLockedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex));
  }

  @ExceptionHandler(OutOfEnergyException.class)
  ResponseEntity<ErrorResponse> energy(OutOfEnergyException ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error(ex));
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ErrorResponse> domain(DomainException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex));
  }

  private static ErrorResponse error(DomainException ex) {
    return new ErrorResponse("error", ex.getMessage());
  }
}

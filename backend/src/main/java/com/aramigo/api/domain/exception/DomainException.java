package com.aramigo.api.domain.exception;

public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }
}

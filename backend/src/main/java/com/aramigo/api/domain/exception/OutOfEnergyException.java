package com.aramigo.api.domain.exception;

public class OutOfEnergyException extends DomainException {

  public OutOfEnergyException() {
    super("Out of energy — take a break");
  }
}

package com.aramigo.api.domain.exception;

/**
 * A gem-for-energy trade that would take something and give nothing back.
 *
 * <p>Two cases, both refused rather than quietly accepted: not enough gems, and
 * a bar that is already full. Charging for a refill nobody needed is the sort of
 * thing a learner notices once and never forgives.
 */
public class RefillRefusedException extends DomainException {

  public RefillRefusedException(String message) {
    super(message);
  }
}

package com.aramigo.api.domain.exception;

/** Thrown when a learner has no energy left. Always temporary — energy regenerates. */
public class OutOfEnergyException extends DomainException {

  private final long secondsUntilNextEnergy;

  public OutOfEnergyException(long secondsUntilNextEnergy) {
    super("Out of energy — one point comes back soon");
    this.secondsUntilNextEnergy = secondsUntilNextEnergy;
  }

  public long getSecondsUntilNextEnergy() {
    return secondsUntilNextEnergy;
  }
}

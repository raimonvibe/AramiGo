package com.aramigo.api.domain.exception;

/** Thrown when a lesson is claimed as finished but some exercises were never solved. */
public class LessonIncompleteException extends DomainException {

  private final int remainingExercises;

  public LessonIncompleteException(int remainingExercises) {
    super(
        remainingExercises == 1
            ? "One exercise still to go"
            : remainingExercises + " exercises still to go");
    this.remainingExercises = remainingExercises;
  }

  public int getRemainingExercises() {
    return remainingExercises;
  }
}

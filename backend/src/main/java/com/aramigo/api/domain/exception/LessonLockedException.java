package com.aramigo.api.domain.exception;

public class LessonLockedException extends DomainException {

  public LessonLockedException() {
    super("Lesson is still locked");
  }
}

package com.aramigo.api.domain.model;

import java.time.Duration;
import java.time.Instant;

/**
 * When a solved exercise should be asked again.
 *
 * <p>Before this existed, solving an exercise once marked it solved forever and
 * nothing ever brought it back. The only reason vocabulary recurred at all was
 * that each unit was written to reuse the last one by hand, which does not scale
 * past the point where a unit can plausibly revisit everything before it.
 *
 * <p>The ladder is Leitner-style: each correct answer moves the exercise one rung
 * up and pushes the next sighting further out. It is deliberately not FSRS — that
 * fits a memory model per learner from review history, which needs far more
 * history than this app has yet. The shape here is the same (expanding intervals,
 * lapses come back soon) so the ladder can be swapped for a fitted model later
 * without anything above this class noticing.
 *
 * <p>Pure domain: no persistence, no framework, no clock of its own.
 */
public record ReviewState(int strength, Instant dueAt) {

  /**
   * Days until the next sighting, by rung. Roughly ×2.2 each step, topping out
   * near five months — far enough that a mastered word costs almost nothing to
   * carry, close enough that it is still checked within a season.
   */
  private static final int[] LADDER_DAYS = {1, 3, 7, 16, 35, 75, 160};

  /** The top rung. Strength never climbs past it. */
  public static final int MASTERED = LADDER_DAYS.length;

  /** Where a lapse sends an exercise: back to tomorrow, whatever it had earned. */
  public static final int LAPSED_STRENGTH = 1;

  public ReviewState {
    if (strength < 1) {
      throw new IllegalArgumentException("strength starts at 1, got " + strength);
    }
    if (strength > MASTERED) {
      throw new IllegalArgumentException("strength tops out at " + MASTERED + ", got " + strength);
    }
    if (dueAt == null) {
      throw new IllegalArgumentException("dueAt is required");
    }
  }

  /** The first time an exercise is answered correctly. */
  public static ReviewState firstCorrect(Instant now) {
    return new ReviewState(1, now.plus(intervalFor(1)));
  }

  /**
   * Progress made before scheduling existed, and rows written by older versions.
   *
   * <p>Due immediately rather than silently skipped: the learner solved it at
   * some unknown point in the past, which is exactly the case review is for.
   */
  public static ReviewState unscheduled(Instant now) {
    return new ReviewState(1, now);
  }

  /** One rung up, and out of sight for longer. */
  public ReviewState afterCorrect(Instant now) {
    int next = Math.min(strength + 1, MASTERED);
    return new ReviewState(next, now.plus(intervalFor(next)));
  }

  /**
   * Back to the first rung.
   *
   * <p>Leitner's rule, and the honest one: an exercise that has just been got
   * wrong is not known, whatever its history says. It costs the learner only a
   * single extra sighting tomorrow.
   */
  public ReviewState afterLapse(Instant now) {
    return new ReviewState(LAPSED_STRENGTH, now.plus(intervalFor(LAPSED_STRENGTH)));
  }

  public boolean isDue(Instant now) {
    return !dueAt.isAfter(now);
  }

  private static Duration intervalFor(int strength) {
    return Duration.ofDays(LADDER_DAYS[strength - 1]);
  }
}

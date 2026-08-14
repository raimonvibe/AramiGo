package com.aramigo.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class ReviewStateTest {

  private static final Instant NOW = Instant.parse("2026-08-14T09:00:00Z");

  @Test
  void firstCorrectAnswerComesBackTomorrow() {
    ReviewState state = ReviewState.firstCorrect(NOW);

    assertThat(state.strength()).isEqualTo(1);
    assertThat(state.dueAt()).isEqualTo(NOW.plus(Duration.ofDays(1)));
  }

  @Test
  void eachCorrectAnswerPushesTheNextSightingFurtherOut() {
    ReviewState state = ReviewState.firstCorrect(NOW);
    Duration previous = Duration.between(NOW, state.dueAt());

    Instant clock = NOW;
    for (int rung = 2; rung <= ReviewState.MASTERED; rung++) {
      clock = state.dueAt();
      state = state.afterCorrect(clock);

      Duration interval = Duration.between(clock, state.dueAt());
      assertThat(interval)
          .describedAs("rung %d should wait longer than rung %d", rung, rung - 1)
          .isGreaterThan(previous);
      previous = interval;
    }
  }

  @Test
  void strengthStopsAtTheTopRung() {
    ReviewState state = ReviewState.firstCorrect(NOW);
    Instant clock = NOW;

    // Well past the length of the ladder.
    for (int i = 0; i < ReviewState.MASTERED * 3; i++) {
      clock = state.dueAt();
      state = state.afterCorrect(clock);
    }

    assertThat(state.strength()).isEqualTo(ReviewState.MASTERED);
    // Still scheduled, not retired — a mastered word is cheap to carry, not free.
    assertThat(state.dueAt()).isAfter(clock);
  }

  @Test
  void aLapseSendsAMasteredExerciseBackToTomorrow() {
    ReviewState mastered = new ReviewState(ReviewState.MASTERED, NOW);

    ReviewState lapsed = mastered.afterLapse(NOW);

    assertThat(lapsed.strength()).isEqualTo(1);
    assertThat(lapsed.dueAt()).isEqualTo(NOW.plus(Duration.ofDays(1)));
  }

  @Test
  void progressMadeBeforeSchedulingExistedIsDueImmediately() {
    ReviewState legacy = ReviewState.unscheduled(NOW);

    // Solved at some unknown past point, which is exactly what review is for.
    assertThat(legacy.isDue(NOW)).isTrue();
  }

  @Test
  void anExerciseIsDueOnceItsMomentArrivesAndNotBefore() {
    ReviewState state = ReviewState.firstCorrect(NOW);

    assertThat(state.isDue(NOW)).isFalse();
    assertThat(state.isDue(state.dueAt().minusSeconds(1))).isFalse();
    assertThat(state.isDue(state.dueAt())).isTrue();
    assertThat(state.isDue(state.dueAt().plusSeconds(1))).isTrue();
  }

  @Test
  void refusesAStrengthOffTheLadder() {
    assertThatThrownBy(() -> new ReviewState(0, NOW)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ReviewState(ReviewState.MASTERED + 1, NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

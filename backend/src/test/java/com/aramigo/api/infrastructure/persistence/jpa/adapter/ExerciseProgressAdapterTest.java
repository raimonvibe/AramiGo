package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.aramigo.api.infrastructure.persistence.jpa.spring.ExerciseProgressJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * The review queue against a real database.
 *
 * <p>The in-memory port fake cannot catch any of what is checked here: that the
 * JPQL parses at all, that null {@code dueAt} sorts first rather than being
 * dropped by the comparison, and that the row limit is actually applied. Every
 * one of those is silent when wrong — the queue just comes back short or empty.
 */
@DataJpaTest
class ExerciseProgressAdapterTest {

  private static final long LEARNER = 1L;
  private static final long SOMEONE_ELSE = 2L;
  private static final Instant NOW = Instant.parse("2026-08-14T09:00:00Z");

  @Autowired private ExerciseProgressJpaRepository repository;
  @Autowired private TestEntityManager entities;

  private ExerciseProgressAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ExerciseProgressAdapter(repository);
  }

  /**
   * A solved row with no schedule, exactly as {@code ddl-auto=update} leaves every
   * row that existed before these columns did.
   *
   * <p>Native SQL because there is no other way to reach that state — the entity
   * always writes a schedule, which is the point.
   */
  private void giveLegacyRow(long learnerId, long exerciseId) {
    adapter.recordCorrect(learnerId, exerciseId, NOW);
    entities
        .getEntityManager()
        .createNativeQuery(
            "update exercise_progress set strength = null, due_at = null"
                + " where learner_id = ?1 and exercise_id = ?2")
        .setParameter(1, learnerId)
        .setParameter(2, exerciseId)
        .executeUpdate();
    entities.clear();
  }

  @Test
  void progressFromBeforeSchedulingExistedIsDueImmediately() {
    giveLegacyRow(LEARNER, 10L);

    // Solved at some unknown past point, which is exactly what review is for.
    assertThat(adapter.countDueForReview(LEARNER, NOW)).isEqualTo(1);
    assertThat(adapter.findDueForReview(LEARNER, NOW, 10)).containsExactly(10L);
  }

  @Test
  void aLegacyRowIsHandedBackAheadOfAnythingWithADate() {
    adapter.recordCorrect(LEARNER, 20L, NOW.minus(Duration.ofDays(30)));
    giveLegacyRow(LEARNER, 10L);

    // Null sorts first: it has waited longer than anything that carries a date.
    assertThat(adapter.findDueForReview(LEARNER, NOW, 10)).containsExactly(10L, 20L);
  }

  @Test
  void answeringALegacyRowSchedulesItProperly() {
    giveLegacyRow(LEARNER, 10L);

    adapter.recordCorrect(LEARNER, 10L, NOW);

    // No longer perpetually due — it has joined the ladder.
    assertThat(adapter.findDueForReview(LEARNER, NOW, 10)).isEmpty();
    assertThat(adapter.findDueForReview(LEARNER, NOW.plus(Duration.ofDays(4)), 10))
        .containsExactly(10L);
  }

  @Test
  void aFreshSolveIsNotDueYet() {
    adapter.recordCorrect(LEARNER, 10L, NOW);

    assertThat(adapter.countDueForReview(LEARNER, NOW)).isZero();
    assertThat(adapter.findDueForReview(LEARNER, NOW, 10)).isEmpty();
  }

  @Test
  void aSolveComesDueOnceItsIntervalPasses() {
    adapter.recordCorrect(LEARNER, 10L, NOW);

    Instant later = NOW.plus(Duration.ofDays(2));

    assertThat(adapter.countDueForReview(LEARNER, later)).isEqualTo(1);
    assertThat(adapter.findDueForReview(LEARNER, later, 10)).containsExactly(10L);
  }

  @Test
  void answeringItRightAgainPushesItBackOut() {
    adapter.recordCorrect(LEARNER, 10L, NOW);
    Instant tomorrow = NOW.plus(Duration.ofDays(2));
    adapter.recordCorrect(LEARNER, 10L, tomorrow);

    // Rung two is three days out, so two days later it is still quiet.
    assertThat(adapter.findDueForReview(LEARNER, tomorrow.plus(Duration.ofDays(2)), 10)).isEmpty();
    assertThat(adapter.findDueForReview(LEARNER, tomorrow.plus(Duration.ofDays(4)), 10))
        .containsExactly(10L);
  }

  @Test
  void aLapseBringsItBackWithoutRemovingTheSolve() {
    adapter.recordCorrect(LEARNER, 10L, NOW);
    adapter.recordCorrect(LEARNER, 10L, NOW.plus(Duration.ofDays(2)));

    adapter.recordLapse(LEARNER, 10L, NOW.plus(Duration.ofDays(3)));

    assertThat(adapter.findDueForReview(LEARNER, NOW.plus(Duration.ofDays(5)), 10))
        .containsExactly(10L);
    // Still solved, so a lesson already finished cannot come undone.
    assertThat(adapter.solvedAmong(LEARNER, List.of(10L))).containsExactly(10L);
  }

  @Test
  void aLapseOnSomethingNeverSolvedDoesNothing() {
    adapter.recordLapse(LEARNER, 999L, NOW);

    assertThat(adapter.solvedAmong(LEARNER, List.of(999L))).isEmpty();
    assertThat(adapter.countDueForReview(LEARNER, NOW.plus(Duration.ofDays(400)))).isZero();
  }

  @Test
  void theQueueIsCappedAtTheLimitButTheCountIsNot() {
    for (long exerciseId = 1; exerciseId <= 30; exerciseId++) {
      adapter.recordCorrect(LEARNER, exerciseId, NOW);
    }

    Instant muchLater = NOW.plus(Duration.ofDays(400));

    assertThat(adapter.countDueForReview(LEARNER, muchLater)).isEqualTo(30);
    assertThat(adapter.findDueForReview(LEARNER, muchLater, 12)).hasSize(12);
  }

  @Test
  void theLongestWaitingIsHandedBackFirst() {
    adapter.recordCorrect(LEARNER, 10L, NOW);
    adapter.recordCorrect(LEARNER, 20L, NOW.plus(Duration.ofDays(5)));

    assertThat(adapter.findDueForReview(LEARNER, NOW.plus(Duration.ofDays(10)), 10))
        .containsExactly(10L, 20L);
  }

  @Test
  void oneLearnerNeverSeesAnothersQueue() {
    adapter.recordCorrect(LEARNER, 10L, NOW);
    adapter.recordCorrect(SOMEONE_ELSE, 20L, NOW);

    Instant later = NOW.plus(Duration.ofDays(2));

    assertThat(adapter.findDueForReview(LEARNER, later, 10)).containsExactly(10L);
    assertThat(adapter.findDueForReview(SOMEONE_ELSE, later, 10)).containsExactly(20L);
  }

  @Test
  void askingForNothingReturnsNothingRatherThanEverything() {
    adapter.recordCorrect(LEARNER, 10L, NOW);

    // A zero page size is an illegal argument to Pageable, not an empty result.
    assertThat(adapter.findDueForReview(LEARNER, NOW.plus(Duration.ofDays(2)), 0)).isEmpty();
  }
}

package com.aramigo.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.aramigo.api.application.dto.RefillEnergyResult;
import com.aramigo.api.domain.exception.RefillRefusedException;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Gems were awarded from the first lesson and bought nothing. Now they buy this. */
class RefillEnergyTest {

  private static final String GUEST = "guest:someone";
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
  private static final int COST = 50;

  private InMemoryPorts.Curriculum curriculum;
  private InMemoryPorts.Learners learners;
  private LearningApplicationService service;

  private Exercise exercise;

  @BeforeEach
  void setUp() {
    curriculum = new InMemoryPorts.Curriculum();
    learners = new InMemoryPorts.Learners();
    service =
        new LearningApplicationService(
            curriculum,
            learners,
            new InMemoryPorts.Progress(),
            new TokenAnswerMatchingPolicy(),
            new MutableClock(NOW));

    LessonUnit unit = curriculum.addUnit("s1-u1", 1, 1, "Basics");
    Lesson lesson = curriculum.addLesson(unit, "l1", 1, "One");
    exercise = curriculum.addExercise(lesson, "l1-e1", "hello");
  }

  private void giveGems(int gems) {
    // The only way in is finishing lessons, so hand them over directly.
    Learner learner = learners.findOrCreate(GUEST, NOW);
    learner.addGems(gems);
    learners.save(learner);
  }

  private void burnAllEnergy() {
    for (int i = 0; i < Learner.MAX_ENERGY; i++) {
      service.checkAnswer(GUEST, exercise.id(), List.of("wrong"));
    }
  }

  @Test
  void gemsBuyAFullBar() {
    giveGems(COST);
    burnAllEnergy();
    assertThat(service.getPath(GUEST).stats().energy()).isZero();

    RefillEnergyResult result = service.refillEnergy(GUEST);

    assertThat(result.gemsSpent()).isEqualTo(COST);
    assertThat(result.stats().energy()).isEqualTo(Learner.MAX_ENERGY);
    assertThat(result.stats().gems()).isZero();
  }

  @Test
  void aRefillYouCannotAffordIsRefusedAndCostsNothing() {
    giveGems(COST - 1);
    burnAllEnergy();

    assertThatThrownBy(() -> service.refillEnergy(GUEST))
        .isInstanceOf(RefillRefusedException.class)
        .hasMessageContaining("49");

    assertThat(service.getPath(GUEST).stats().gems()).isEqualTo(COST - 1);
    assertThat(service.getPath(GUEST).stats().energy()).isZero();
  }

  @Test
  void refillingAFullBarIsRefusedRatherThanCharged() {
    giveGems(COST);

    // Charging for a refill nobody needed is noticed once and never forgiven.
    assertThatThrownBy(() -> service.refillEnergy(GUEST))
        .isInstanceOf(RefillRefusedException.class)
        .hasMessageContaining("already full");

    assertThat(service.getPath(GUEST).stats().gems()).isEqualTo(COST);
  }

  @Test
  void aPartlyDrainedBarCanStillBeToppedUp() {
    giveGems(COST);
    service.checkAnswer(GUEST, exercise.id(), List.of("wrong"));

    RefillEnergyResult result = service.refillEnergy(GUEST);

    assertThat(result.stats().energy()).isEqualTo(Learner.MAX_ENERGY);
  }

  @Test
  void theRefillSurvivesTheNextRequest() {
    giveGems(COST);
    burnAllEnergy();
    service.refillEnergy(GUEST);

    // Persisted, not just returned — the bar must still be full on the next read.
    assertThat(service.getPath(GUEST).stats().energy()).isEqualTo(Learner.MAX_ENERGY);
    assertThat(service.getPath(GUEST).stats().gems()).isZero();
  }

  @Test
  void gemsCannotBeSpentTwiceOverOnOneRefill() {
    giveGems(COST);
    burnAllEnergy();
    service.refillEnergy(GUEST);

    assertThatThrownBy(() -> service.refillEnergy(GUEST))
        .isInstanceOf(RefillRefusedException.class);
    assertThat(service.getPath(GUEST).stats().gems()).isZero();
  }
}

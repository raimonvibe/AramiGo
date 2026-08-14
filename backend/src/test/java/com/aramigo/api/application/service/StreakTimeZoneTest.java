package com.aramigo.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Streaks are counted in whole days, so the answer to "which day is it" decides
 * whether a habit is recorded as kept or broken.
 */
class StreakTimeZoneTest {

  private static final String GUEST = "guest:someone";

  /** 21:00 in Amsterdam on 1 March — still 20:00 the same day in UTC. */
  private static final Instant EVENING = Instant.parse("2026-03-01T20:00:00Z");

  private static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");
  private static final ZoneId AUCKLAND = ZoneId.of("Pacific/Auckland");

  private InMemoryPorts.Curriculum curriculum;
  private InMemoryPorts.Learners learners;
  private MutableClock clock;
  private LearningApplicationService service;

  private Lesson lessonOne;
  private Lesson lessonTwo;

  @BeforeEach
  void setUp() {
    curriculum = new InMemoryPorts.Curriculum();
    learners = new InMemoryPorts.Learners();
    clock = new MutableClock(EVENING);
    service =
        new LearningApplicationService(
            curriculum,
            learners,
            new InMemoryPorts.Progress(),
            new TokenAnswerMatchingPolicy(),
            clock);

    LessonUnit unit = curriculum.addUnit("s1-u1", 1, 1, "Basics");
    lessonOne = curriculum.addLesson(unit, "l1", 1, "One");
    lessonTwo = curriculum.addLesson(unit, "l2", 2, "Two");
    curriculum.addExercise(lessonOne, "l1-e1", "hello");
    curriculum.addExercise(lessonTwo, "l2-e1", "peace");
  }

  private void finish(Lesson lesson, ZoneId zone) {
    for (Exercise exercise : curriculum.findExercisesByLessonId(lesson.id())) {
      service.checkAnswer(GUEST, exercise.id(), List.of(exercise.correctTokens()));
    }
    service.completeLesson(GUEST, lesson.id(), zone);
  }

  private int streak() {
    return learners.findByIdentityKey(GUEST).orElseThrow().getStreak();
  }

  @Test
  void twoConsecutiveLocalEveningsAreATwoDayStreak() {
    finish(lessonOne, AMSTERDAM);
    assertThat(streak()).isEqualTo(1);

    clock.advance(Duration.ofHours(24));
    finish(lessonTwo, AMSTERDAM);

    assertThat(streak()).isEqualTo(2);
  }

  @Test
  void anEveningHabitInNewZealandIsNotCountedTwiceOnOneDay() {
    // 20:00Z on 1 March is already 09:00 on 2 March in Auckland. Two sittings
    // eleven hours apart are the same local day there, however they look in UTC.
    finish(lessonOne, AUCKLAND);
    assertThat(streak()).isEqualTo(1);

    clock.advance(Duration.ofHours(11));
    finish(lessonTwo, AUCKLAND);

    assertThat(streak()).isEqualTo(1);
  }

  @Test
  void theSameTwoSittingsSpanTwoDaysInUtc() {
    // The bug this fixes, kept as a demonstration: counted in UTC those same two
    // sittings straddle midnight and read as a two-day streak nobody earned.
    finish(lessonOne, ZoneOffset.UTC);
    clock.advance(Duration.ofHours(11));
    finish(lessonTwo, ZoneOffset.UTC);

    assertThat(streak()).isEqualTo(2);
  }

  @Test
  void anUnknownZoneStillCompletesTheLessonInUtc() {
    // ClientTimeZone hands UTC over for anything it cannot parse, so a learner
    // on an odd browser loses streak accuracy rather than the ability to finish.
    finish(lessonOne, ZoneOffset.UTC);

    assertThat(streak()).isEqualTo(1);
  }
}

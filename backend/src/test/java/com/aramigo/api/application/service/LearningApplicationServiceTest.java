package com.aramigo.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.domain.exception.LessonIncompleteException;
import com.aramigo.api.domain.exception.LessonLockedException;
import com.aramigo.api.domain.exception.OutOfEnergyException;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.model.NodeStatus;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LearningApplicationServiceTest {

  private static final String GUEST = "guest:someone";
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

  private InMemoryPorts.Curriculum curriculum;
  private InMemoryPorts.Learners learners;
  private InMemoryPorts.Progress progress;
  private MutableClock clock;
  private LearningApplicationService service;

  private Lesson lessonOne;
  private Lesson lessonTwo;
  private Exercise firstExercise;
  private Exercise secondExercise;

  @BeforeEach
  void setUp() {
    curriculum = new InMemoryPorts.Curriculum();
    learners = new InMemoryPorts.Learners();
    progress = new InMemoryPorts.Progress();
    clock = new MutableClock(NOW);
    service =
        new LearningApplicationService(
            curriculum, learners, progress, new TokenAnswerMatchingPolicy(), clock);

    LessonUnit unit = curriculum.addUnit("s1-u1", 1, 1, "Basics");
    lessonOne = curriculum.addLesson(unit, "s1-u1-l1", 1, "Greetings");
    lessonTwo = curriculum.addLesson(unit, "s1-u1-l2", 2, "Names");
    firstExercise = curriculum.addExercise(lessonOne, "s1-u1-l1-e1", "hello");
    secondExercise = curriculum.addExercise(lessonOne, "s1-u1-l1-e2", "peace");
  }

  @Test
  void claimingALessonYouNeverPlayedIsRejected() {
    assertThrows(
        LessonIncompleteException.class, () -> service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC));
  }

  @Test
  void aPartlyFinishedLessonIsStillIncomplete() {
    service.checkAnswer(GUEST, firstExercise.id(), List.of("hello"));

    assertThrows(
        LessonIncompleteException.class, () -> service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC));
  }

  @Test
  void jumpingAheadToALockedLessonIsRejected() {
    assertThrows(LessonLockedException.class, () -> service.completeLesson(GUEST, lessonTwo.id(), ZoneOffset.UTC));
    assertThrows(LessonLockedException.class, () -> service.startLesson(GUEST, lessonTwo.id()));
  }

  @Test
  void solvingEveryExerciseUnlocksTheNextLesson() {
    solveLessonOne();

    CompleteLessonResult result = service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    assertEquals(10, result.gemsReward());
    assertEquals(1, result.stats().streak());
    assertEquals(
        NodeStatus.CURRENT,
        service.getPath(GUEST).units().getFirst().nodes().stream()
            .filter(node -> node.position() == 2)
            .findFirst()
            .orElseThrow()
            .status());
  }

  @Test
  void replayingALessonPaysNothing() {
    solveLessonOne();
    service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    CompleteLessonResult again = service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    assertEquals(0, again.gemsReward(), "gems must not be farmable by replaying one lesson");
    assertEquals(0, again.energyReward());
  }

  @Test
  void wrongAnswersCostEnergyAndRunningOutIsRecoverable() {
    for (int i = 0; i < Learner.MAX_ENERGY; i++) {
      service.checkAnswer(GUEST, firstExercise.id(), List.of("water"));
    }
    assertEquals(0, service.getPath(GUEST).stats().energy());
    assertThrows(OutOfEnergyException.class, () -> service.startLesson(GUEST, lessonOne.id()));

    clock.advance(Learner.ENERGY_REGEN_INTERVAL);

    assertEquals(1, service.getPath(GUEST).stats().energy());
    assertEquals(2, service.startLesson(GUEST, lessonOne.id()).exercises().size());
  }

  @Test
  void overPickingOnAOneWordPromptIsFree() {
    int before = service.getPath(GUEST).stats().energy();

    service.checkAnswer(GUEST, firstExercise.id(), List.of("hello", "peace"));

    assertEquals(before, service.getPath(GUEST).stats().energy());
  }

  @Test
  void signingInCarriesGuestProgressOntoTheAccount() {
    solveLessonOne();
    service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    service.linkGuestProgress("google:42", "Stefan", GUEST);

    assertEquals(10, service.getPath("google:42").stats().gems());
    assertEquals(
        NodeStatus.COMPLETED,
        service.getPath("google:42").units().getFirst().nodes().getFirst().status());
    assertTrue(
        learners.findByIdentityKey(GUEST).isEmpty(), "the merged guest row should be gone");
  }

  @Test
  void linkingTwiceIsHarmless() {
    solveLessonOne();
    service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    service.linkGuestProgress("google:42", "Stefan", GUEST);
    service.linkGuestProgress("google:42", "Stefan", GUEST);

    assertEquals(10, service.getPath("google:42").stats().gems(), "gems must not double up");
  }

  @Test
  void solvedExercisesAreReportedOnThePath() {
    service.checkAnswer(GUEST, firstExercise.id(), List.of("hello"));

    var node = service.getPath(GUEST).units().getFirst().nodes().getFirst();

    assertEquals(2, node.exerciseCount());
    assertEquals(1, node.solvedCount());
  }

  @Test
  void profileReportsLessonProgress() {
    var before = service.profile(GUEST, null);
    assertEquals(0, before.completedLessons());
    assertEquals(2, before.totalLessons());

    solveLessonOne();
    service.completeLesson(GUEST, lessonOne.id(), ZoneOffset.UTC);

    var after = service.profile(GUEST, null);
    assertEquals(1, after.completedLessons());
    assertEquals(2, after.totalLessons());
    assertEquals(10, after.stats().gems());
  }

  private void solveLessonOne() {
    service.checkAnswer(GUEST, firstExercise.id(), List.of("hello"));
    service.checkAnswer(GUEST, secondExercise.id(), List.of("peace"));
  }

}

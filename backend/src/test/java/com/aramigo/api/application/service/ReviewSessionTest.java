package com.aramigo.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.aramigo.api.application.dto.ReviewSessionResult;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.model.ReviewState;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** What review actually does to a learner, as opposed to what the ladder computes. */
class ReviewSessionTest {

  private static final String GUEST = "guest:someone";
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

  private InMemoryPorts.Curriculum curriculum;
  private InMemoryPorts.Progress progress;
  private MutableClock clock;
  private LearningApplicationService service;

  private Exercise hello;
  private Exercise peace;

  @BeforeEach
  void setUp() {
    curriculum = new InMemoryPorts.Curriculum();
    progress = new InMemoryPorts.Progress();
    clock = new MutableClock(NOW);
    service =
        new LearningApplicationService(
            curriculum,
            new InMemoryPorts.Learners(),
            progress,
            new TokenAnswerMatchingPolicy(),
            clock);

    LessonUnit unit = curriculum.addUnit("s1-u1", 1, 1, "Basics");
    Lesson lesson = curriculum.addLesson(unit, "s1-u1-l1", 1, "Greetings");
    hello = curriculum.addExercise(lesson, "s1-u1-l1-e1", "hello");
    peace = curriculum.addExercise(lesson, "s1-u1-l1-e2", "peace");
  }

  private void solve(Exercise exercise) {
    service.checkAnswer(GUEST, exercise.id(), List.of(exercise.correctTokens()));
  }

  @Test
  void nothingIsDueBeforeAnythingHasBeenLearned() {
    ReviewSessionResult review = service.reviewSession(GUEST);

    assertThat(review.dueCount()).isZero();
    assertThat(review.exercises()).isEmpty();
  }

  @Test
  void anExerciseJustSolvedIsNotDueYet() {
    solve(hello);

    // The whole point is spacing — asking again in the same sitting teaches nothing.
    assertThat(service.reviewSession(GUEST).exercises()).isEmpty();
  }

  @Test
  void anExerciseComesBackOnceItsIntervalHasPassed() {
    solve(hello);

    clock.advance(Duration.ofDays(2));

    ReviewSessionResult review = service.reviewSession(GUEST);
    assertThat(review.dueCount()).isEqualTo(1);
    assertThat(review.exercises()).extracting("id").containsExactly(hello.id());
  }

  @Test
  void answeringItRightAgainPushesItFurtherOut() {
    solve(hello);
    clock.advance(Duration.ofDays(2));
    solve(hello);

    // Rung two is three days, so two days on is no longer enough.
    clock.advance(Duration.ofDays(2));
    assertThat(service.reviewSession(GUEST).exercises()).isEmpty();

    clock.advance(Duration.ofDays(2));
    assertThat(service.reviewSession(GUEST).exercises()).hasSize(1);
  }

  @Test
  void gettingItWrongBringsItBackTomorrow() {
    solve(hello);
    clock.advance(Duration.ofDays(2));
    solve(hello);
    solve(hello);
    solve(hello); // well up the ladder

    clock.advance(Duration.ofDays(1));
    service.checkAnswer(GUEST, hello.id(), List.of("nonsense"));

    // Knocked back to the first rung, so it returns after a day rather than weeks.
    clock.advance(Duration.ofDays(1).plusMinutes(1));
    assertThat(service.reviewSession(GUEST).exercises()).extracting("id").containsExactly(hello.id());
  }

  @Test
  void aWrongAnswerDuringReviewDoesNotUnsolveTheExercise() {
    solve(hello);
    solve(peace);

    clock.advance(Duration.ofDays(2));
    service.checkAnswer(GUEST, hello.id(), List.of("nonsense"));

    // The lesson stays finishable — a lapse moves the schedule, not the progress.
    assertThat(progress.solvedAmong(1L, List.of(hello.id(), peace.id())))
        .containsExactlyInAnyOrder(hello.id(), peace.id());
  }

  @Test
  void aLongAbsenceHandsOutOneSittingAndSaysHowMuchIsLeft() {
    List<Exercise> many = new java.util.ArrayList<>();
    LessonUnit unit = curriculum.addUnit("s1-u2", 1, 2, "More");
    Lesson lesson = curriculum.addLesson(unit, "s1-u2-l1", 2, "Lots");
    for (int i = 0; i < 30; i++) {
      many.add(curriculum.addExercise(lesson, "e" + i, "word" + i));
    }
    many.forEach(this::solve);

    clock.advance(Duration.ofDays(400));

    ReviewSessionResult review = service.reviewSession(GUEST);
    // Everything is due, but a returning learner is handed a sitting, not a wall.
    assertThat(review.dueCount()).isEqualTo(30);
    assertThat(review.exercises()).hasSize(12);
  }

  @Test
  void theOldestDebtIsPaidFirst() {
    solve(hello);
    clock.advance(Duration.ofDays(2));
    solve(peace);
    clock.advance(Duration.ofDays(2));

    // hello has been waiting since day 1, peace only since day 3.
    assertThat(service.reviewSession(GUEST).exercises())
        .extracting("id")
        .containsExactly(hello.id(), peace.id());
  }

  @Test
  void progressFromBeforeSchedulingExistedIsDueRatherThanLost() {
    // A row with no schedule, as ddl-auto leaves every pre-existing one.
    progress.recordCorrect(1L, hello.id(), NOW);
    progress.forgetSchedule(1L, hello.id());

    ReviewSessionResult review = service.reviewSession(GUEST);

    assertThat(review.dueCount()).isEqualTo(1);
    assertThat(review.exercises()).extracting("id").containsExactly(hello.id());
    assertThat(ReviewState.unscheduled(NOW).isDue(NOW)).isTrue();
  }
}

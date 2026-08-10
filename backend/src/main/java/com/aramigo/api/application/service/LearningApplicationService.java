package com.aramigo.api.application.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LearningPathResult.PathNodeResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.dto.LessonSessionResult.ExerciseView;
import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.domain.exception.LessonLockedException;
import com.aramigo.api.domain.exception.NotFoundException;
import com.aramigo.api.domain.exception.OutOfEnergyException;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.policy.AnswerMatchingPolicy;

/**
 * Application service (use-case orchestrator). Depends only on ports + domain.
 * Transactions and HTTP stay in infrastructure adapters.
 */
public class LearningApplicationService implements LearningUseCases {

  private static final int ENERGY_COST_WRONG = 1;
  private static final int ENERGY_REWARD_LESSON = 3;
  private static final int GEMS_REWARD_LESSON = 10;

  private final CurriculumRepositoryPort curriculum;
  private final LearnerRepositoryPort learners;
  private final AnswerMatchingPolicy answers;

  public LearningApplicationService(
      CurriculumRepositoryPort curriculum,
      LearnerRepositoryPort learners,
      AnswerMatchingPolicy answers) {
    this.curriculum = curriculum;
    this.learners = learners;
    this.answers = answers;
  }

  @Override
  public LearningPathResult getPath(String guestKey) {
    Learner learner = getOrCreate(guestKey);
    LessonUnit unit =
        curriculum
            .findUnit(1, 1)
            .orElseThrow(() -> new NotFoundException("Unit not seeded"));

    List<PathNodeResult> nodes =
        curriculum.findLessonsByUnitId(unit.getId()).stream()
            .map(
                lesson ->
                    new PathNodeResult(
                        lesson.getId(),
                        lesson.getPosition(),
                        lesson.getTitle(),
                        lesson.getNodeKind(),
                        learner.statusOf(lesson.getPosition())))
            .toList();

    return new LearningPathResult(
        unit.getSectionNumber(),
        unit.getUnitNumber(),
        unit.getTitle(),
        unit.getDescription(),
        learner.stats(),
        nodes);
  }

  @Override
  public LessonSessionResult startLesson(String guestKey, long lessonId) {
    Learner learner = getOrCreate(guestKey);
    Lesson lesson =
        curriculum
            .findLessonById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));

    if (!learner.canAccess(lesson.getPosition())) {
      throw new LessonLockedException();
    }
    if (!learner.hasEnergy()) {
      throw new OutOfEnergyException();
    }

    List<ExerciseView> views =
        curriculum.findExercisesByLessonId(lessonId).stream().map(this::toView).toList();

    return new LessonSessionResult(lesson.getId(), lesson.getTitle(), learner.stats(), views);
  }

  @Override
  public CheckAnswerResult checkAnswer(String guestKey, long exerciseId, List<String> tokens) {
    Learner learner = getOrCreate(guestKey);
    Exercise exercise =
        curriculum
            .findExerciseById(exerciseId)
            .orElseThrow(() -> new NotFoundException("Exercise not found"));

    String hint = answers.friendlyHint(exercise.getCorrectTokens());
    if (answers.matches(exercise.getCorrectTokens(), tokens)) {
      return new CheckAnswerResult(true, "Great job!", hint, 0, learner.stats());
    }

    if (tokens.size() > 1 && answers.isSingleWordPrompt(exercise.getCorrectTokens())) {
      return new CheckAnswerResult(
          false, "Almost — pick just one word", hint, 0, learner.stats());
    }

    learner.spendEnergy(ENERGY_COST_WRONG);
    learners.save(learner);
    return new CheckAnswerResult(
        false, "Not quite — try again", hint, -ENERGY_COST_WRONG, learner.stats());
  }

  @Override
  public CompleteLessonResult completeLesson(String guestKey, long lessonId) {
    Learner learner = getOrCreate(guestKey);
    Lesson lesson =
        curriculum
            .findLessonById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));

    learner.markLessonCompleted(lesson.getPosition());
    learner.addEnergy(ENERGY_REWARD_LESSON);
    learner.addGems(GEMS_REWARD_LESSON);
    learners.save(learner);

    return new CompleteLessonResult(ENERGY_REWARD_LESSON, GEMS_REWARD_LESSON, learner.stats());
  }

  private Learner getOrCreate(String guestKey) {
    return learners
        .findByGuestKey(guestKey)
        .orElseGet(() -> learners.save(new Learner(guestKey)));
  }

  private ExerciseView toView(Exercise exercise) {
    List<String> bank = new ArrayList<>();
    bank.addAll(answers.bankTokensFromAnswers(exercise.getCorrectTokens()));
    bank.addAll(tokens(exercise.getDistractorTokens()));
    Collections.shuffle(bank);
    return new ExerciseView(
        exercise.getId(),
        exercise.getType(),
        exercise.getPrompt(),
        answers.tipFor(exercise.getCorrectTokens()),
        exercise.getAramaicScript(),
        exercise.getTransliteration(),
        bank);
  }

  private static List<String> tokens(String spaceSeparated) {
    if (spaceSeparated == null || spaceSeparated.isBlank()) {
      return List.of();
    }
    return Arrays.stream(spaceSeparated.trim().split("\\s+")).toList();
  }
}

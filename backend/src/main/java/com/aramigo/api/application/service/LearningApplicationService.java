package com.aramigo.api.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LearningPathResult.PathNodeResult;
import com.aramigo.api.application.dto.LearningPathResult.UnitResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.dto.LessonSessionResult.ExerciseView;
import com.aramigo.api.application.dto.ProfileResult;
import com.aramigo.api.application.dto.ReviewSessionResult;
import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.application.port.out.ExerciseProgressPort;
import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.domain.exception.LessonIncompleteException;
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

  /**
   * How many due exercises one review sitting hands out.
   *
   * <p>Capped because the queue is unbounded after a long absence, and a learner
   * returning to two hundred waiting cards closes the tab. The count of
   * everything due still goes back, so the UI can say there is more.
   */
  private static final int REVIEW_SESSION_SIZE = 12;

  private static final int ENERGY_REWARD_LESSON = 3;
  private static final int GEMS_REWARD_LESSON = 10;

  /** Guest identities are minted by the web adapter with this prefix. */
  private static final String GUEST_PREFIX = "guest:";

  private final CurriculumRepositoryPort curriculum;
  private final LearnerRepositoryPort learners;
  private final ExerciseProgressPort progress;
  private final AnswerMatchingPolicy answers;
  private final Clock clock;

  public LearningApplicationService(
      CurriculumRepositoryPort curriculum,
      LearnerRepositoryPort learners,
      ExerciseProgressPort progress,
      AnswerMatchingPolicy answers,
      Clock clock) {
    this.curriculum = curriculum;
    this.learners = learners;
    this.progress = progress;
    this.answers = answers;
    this.clock = clock;
  }

  @Override
  public LearningPathResult getPath(String identityKey) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);

    // Three queries for the whole path, not three per lesson.
    List<Lesson> allLessons = curriculum.findAllLessons();
    Map<Long, List<Exercise>> exercisesByLesson =
        curriculum
            .findExercisesByLessonIds(allLessons.stream().map(Lesson::id).toList())
            .stream()
            .collect(Collectors.groupingBy(Exercise::lessonId));
    Set<Long> solved =
        progress.solvedAmong(
            learner.getId(),
            exercisesByLesson.values().stream().flatMap(List::stream).map(Exercise::id).toList());

    Map<Long, List<Lesson>> lessonsByUnit =
        allLessons.stream().collect(Collectors.groupingBy(Lesson::unitId));

    List<UnitResult> units =
        curriculum.findAllUnits().stream()
            .sorted(
                Comparator.comparingInt(LessonUnit::sectionNumber)
                    .thenComparingInt(LessonUnit::unitNumber))
            .map(
                unit ->
                    toUnitResult(
                        unit,
                        learner,
                        lessonsByUnit.getOrDefault(unit.id(), List.of()),
                        exercisesByLesson,
                        solved))
            .toList();

    return new LearningPathResult(
        learner.stats(now), progress.countDueForReview(learner.getId(), now), units);
  }

  @Override
  public LessonSessionResult startLesson(String identityKey, long lessonId) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);
    Lesson lesson = requireLesson(lessonId);

    if (!learner.canAccess(lesson.position())) {
      throw new LessonLockedException();
    }
    if (!learner.hasEnergy()) {
      throw new OutOfEnergyException(learner.secondsUntilNextEnergy(now));
    }

    List<ExerciseView> views =
        curriculum.findExercisesByLessonId(lessonId).stream().map(this::toView).toList();

    return new LessonSessionResult(lesson.id(), lesson.title(), learner.stats(now), views);
  }

  @Override
  public ReviewSessionResult reviewSession(String identityKey) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);

    int dueCount = progress.countDueForReview(learner.getId(), now);
    List<Long> dueIds = progress.findDueForReview(learner.getId(), now, REVIEW_SESSION_SIZE);

    // Exercises pruned from the curriculum can still have progress rows pointing
    // at them until the next seed, so a missing one is skipped rather than fatal.
    List<ExerciseView> views =
        dueIds.stream().flatMap(id -> curriculum.findExerciseById(id).stream())
            .map(this::toView)
            .toList();

    return new ReviewSessionResult(dueCount, learner.stats(now), views);
  }

  @Override
  public CheckAnswerResult checkAnswer(String identityKey, long exerciseId, List<String> tokens) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);
    Exercise exercise =
        curriculum
            .findExerciseById(exerciseId)
            .orElseThrow(() -> new NotFoundException("Exercise not found"));

    String hint = answers.friendlyHint(exercise.correctTokens());

    if (answers.matches(exercise.correctTokens(), tokens)) {
      progress.recordCorrect(learner.getId(), exerciseId, now);
      return new CheckAnswerResult(true, "Great job!", hint, 0, learner.stats(now));
    }

    // Over-picking on a one-word prompt is a slip, not a mistake — don't charge for it.
    if (tokens.size() > 1 && answers.isSingleWordPrompt(exercise.correctTokens())) {
      return new CheckAnswerResult(
          false, "Almost — pick just one word", hint, 0, learner.stats(now));
    }

    // Knocks the review schedule back if this was already known. The row itself
    // survives, so a slip here can never un-complete a finished lesson.
    progress.recordLapse(learner.getId(), exerciseId, now);
    learner.spendEnergy(ENERGY_COST_WRONG);
    learners.save(learner);
    return new CheckAnswerResult(
        false, "Not quite — try again", hint, -ENERGY_COST_WRONG, learner.stats(now));
  }

  @Override
  public CompleteLessonResult completeLesson(String identityKey, long lessonId) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);
    Lesson lesson = requireLesson(lessonId);

    if (!learner.canAccess(lesson.position())) {
      throw new LessonLockedException();
    }

    // The client asking nicely isn't proof — check the recorded correct answers.
    List<Long> exerciseIds =
        curriculum.findExercisesByLessonId(lessonId).stream().map(Exercise::id).toList();
    Set<Long> solved = progress.solvedAmong(learner.getId(), exerciseIds);
    if (solved.size() < exerciseIds.size()) {
      throw new LessonIncompleteException(exerciseIds.size() - solved.size());
    }

    boolean firstTime = learner.markLessonCompleted(lesson.position());
    int energyReward = firstTime ? ENERGY_REWARD_LESSON : 0;
    int gemsReward = firstTime ? GEMS_REWARD_LESSON : 0;

    if (firstTime) {
      learner.addEnergy(energyReward);
      learner.addGems(gemsReward);
    }
    learner.recordActivityOn(LocalDate.ofInstant(now, ZoneOffset.UTC));
    learners.save(learner);

    return new CompleteLessonResult(energyReward, gemsReward, learner.stats(now));
  }

  @Override
  public ProfileResult profile(String identityKey, String displayName) {
    Instant now = clock.instant();
    Learner learner = activeLearner(identityKey, now);
    if (displayName != null && !displayName.equals(learner.getDisplayName())) {
      learner.rename(displayName);
      learners.save(learner);
    }
    return toProfile(identityKey, learner, now);
  }

  @Override
  public ProfileResult linkGuestProgress(String accountKey, String displayName, String guestKey) {
    Instant now = clock.instant();
    Learner account = activeLearner(accountKey, now);
    if (displayName != null) {
      account.rename(displayName);
    }

    boolean mergeable =
        guestKey != null
            && !guestKey.isBlank()
            && !guestKey.equals(accountKey)
            && guestKey.startsWith(GUEST_PREFIX);

    if (mergeable) {
      learners
          .findByIdentityKey(guestKey)
          .ifPresent(
              guest -> {
                account.absorb(guest);
                progress.transferAll(guest.getId(), account.getId());
                learners.delete(guest);
              });
    }

    learners.save(account);
    return toProfile(accountKey, account, now);
  }

  private ProfileResult toProfile(String identityKey, Learner learner, Instant now) {
    int totalLessons = curriculum.findAllLessons().size();
    int completedLessons = Math.min(learner.getHighestCompletedPosition(), totalLessons);
    return new ProfileResult(
        isAccount(identityKey),
        learner.getDisplayName(),
        learner.stats(now),
        completedLessons,
        totalLessons);
  }

  private static boolean isAccount(String identityKey) {
    return identityKey != null && !identityKey.startsWith(GUEST_PREFIX);
  }

  /** Loads the learner and credits any energy that regenerated while they were away. */
  private Learner activeLearner(String identityKey, Instant now) {
    Learner learner = learners.findOrCreate(identityKey, now);
    if (learner.regenerateEnergy(now)) {
      learner = learners.save(learner);
    }
    return learner;
  }

  private Lesson requireLesson(long lessonId) {
    return curriculum
        .findLessonById(lessonId)
        .orElseThrow(() -> new NotFoundException("Lesson not found"));
  }

  private UnitResult toUnitResult(
      LessonUnit unit,
      Learner learner,
      List<Lesson> lessons,
      Map<Long, List<Exercise>> exercisesByLesson,
      Set<Long> solvedExerciseIds) {

    List<PathNodeResult> nodes =
        lessons.stream()
            .sorted(Comparator.comparingInt(Lesson::position))
            .map(
                lesson -> {
                  List<Exercise> exercises =
                      exercisesByLesson.getOrDefault(lesson.id(), List.of());
                  long solved =
                      exercises.stream().filter(e -> solvedExerciseIds.contains(e.id())).count();
                  return new PathNodeResult(
                      lesson.id(),
                      lesson.position(),
                      lesson.title(),
                      lesson.nodeKind(),
                      learner.statusOf(lesson.position()),
                      exercises.size(),
                      (int) solved);
                })
            .toList();

    return new UnitResult(
        unit.sectionNumber(), unit.unitNumber(), unit.title(), unit.description(), nodes);
  }

  private ExerciseView toView(Exercise exercise) {
    List<String> bank =
        new ArrayList<>(answers.wordBank(exercise.correctTokens(), exercise.distractorTokens()));
    Collections.shuffle(bank);

    // Never ship the romanization for prompts where reading it is the answer.
    String transliteration =
        exercise.transliterationGivesAwayAnswer() ? null : exercise.transliteration();

    // Only the listening types need pronunciation text to drive playback.
    String audioText = exercise.hasAudioPrompt() ? exercise.transliteration() : null;

    return new ExerciseView(
        exercise.id(),
        exercise.type(),
        exercise.prompt(),
        answers.tipFor(exercise.correctTokens()),
        exercise.aramaicScript(),
        transliteration,
        audioText,
        bank);
  }
}

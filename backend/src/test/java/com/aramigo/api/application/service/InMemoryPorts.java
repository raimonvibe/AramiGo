package com.aramigo.api.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.application.port.out.ExerciseProgressPort;
import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;

/** Hand-rolled test doubles — the ports are small enough that fakes beat mocks. */
final class InMemoryPorts {

  private InMemoryPorts() {}

  static final class Curriculum implements CurriculumRepositoryPort {

    private final AtomicLong ids = new AtomicLong();
    private final Map<Long, LessonUnit> units = new HashMap<>();
    private final Map<Long, Lesson> lessons = new HashMap<>();
    private final Map<Long, Exercise> exercises = new HashMap<>();

    LessonUnit addUnit(String slug, int section, int unit, String title) {
      long id = ids.incrementAndGet();
      LessonUnit created = new LessonUnit(id, slug, section, unit, title, title);
      units.put(id, created);
      return created;
    }

    Lesson addLesson(LessonUnit unit, String slug, int position, String title) {
      long id = ids.incrementAndGet();
      Lesson created =
          new Lesson(id, slug, unit.id(), position, title, com.aramigo.api.domain.model.NodeKind.STAR);
      lessons.put(id, created);
      return created;
    }

    Exercise addExercise(Lesson lesson, String slug, String correctTokens) {
      long id = ids.incrementAndGet();
      Exercise created =
          new Exercise(
              id,
              slug,
              lesson.id(),
              exercises.size() + 1,
              com.aramigo.api.domain.model.ExerciseType.TRANSLATE_TO_ENGLISH,
              "What does this mean?",
              "x",
              "x",
              correctTokens,
              "");
      exercises.put(id, created);
      return created;
    }

    @Override
    public Optional<LessonUnit> findUnit(int sectionNumber, int unitNumber) {
      return units.values().stream()
          .filter(u -> u.sectionNumber() == sectionNumber && u.unitNumber() == unitNumber)
          .findFirst();
    }

    @Override
    public List<LessonUnit> findAllUnits() {
      return List.copyOf(units.values());
    }

    @Override
    public List<Lesson> findLessonsByUnitId(long unitId) {
      return lessons.values().stream().filter(l -> l.unitId() == unitId).toList();
    }

    @Override
    public List<Lesson> findAllLessons() {
      return List.copyOf(lessons.values());
    }

    @Override
    public Optional<Lesson> findLessonById(long lessonId) {
      return Optional.ofNullable(lessons.get(lessonId));
    }

    @Override
    public List<Exercise> findExercisesByLessonId(long lessonId) {
      return exercises.values().stream().filter(e -> e.lessonId() == lessonId).toList();
    }

    @Override
    public List<Exercise> findExercisesByLessonIds(Collection<Long> lessonIds) {
      return exercises.values().stream().filter(e -> lessonIds.contains(e.lessonId())).toList();
    }

    @Override
    public Optional<Exercise> findExerciseById(long exerciseId) {
      return Optional.ofNullable(exercises.get(exerciseId));
    }

    @Override
    public LessonUnit upsertUnit(LessonUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Lesson upsertLesson(Lesson lesson) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Exercise upsertExercise(Exercise exercise) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUnitsNotIn(Collection<String> keptSlugs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteLessonsNotIn(Collection<String> keptSlugs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteExercisesNotIn(Collection<String> keptSlugs) {
      throw new UnsupportedOperationException();
    }
  }

  static final class Learners implements LearnerRepositoryPort {

    private final AtomicLong ids = new AtomicLong();
    private final Map<String, Learner> byKey = new HashMap<>();

    @Override
    public Optional<Learner> findByIdentityKey(String identityKey) {
      return Optional.ofNullable(byKey.get(identityKey));
    }

    @Override
    public Learner findOrCreate(String identityKey, Instant now) {
      return byKey.computeIfAbsent(
          identityKey,
          key -> {
            Learner learner = new Learner(key, now);
            learner.assignId(ids.incrementAndGet());
            return learner;
          });
    }

    @Override
    public Learner save(Learner learner) {
      if (learner.getId() == null) {
        learner.assignId(ids.incrementAndGet());
      }
      byKey.put(learner.getIdentityKey(), learner);
      return learner;
    }

    @Override
    public void delete(Learner learner) {
      byKey.remove(learner.getIdentityKey());
    }
  }

  static final class Progress implements ExerciseProgressPort {

    private final Map<Long, Set<Long>> solved = new HashMap<>();

    @Override
    public void recordSolved(long learnerId, long exerciseId) {
      solved.computeIfAbsent(learnerId, id -> new LinkedHashSet<>()).add(exerciseId);
    }

    @Override
    public Set<Long> solvedAmong(long learnerId, Collection<Long> exerciseIds) {
      Set<Long> mine = new HashSet<>(solved.getOrDefault(learnerId, Set.of()));
      mine.retainAll(new ArrayList<>(exerciseIds));
      return mine;
    }

    @Override
    public void transferAll(long fromLearnerId, long toLearnerId) {
      Set<Long> moving = solved.remove(fromLearnerId);
      if (moving != null) {
        solved.computeIfAbsent(toLearnerId, id -> new LinkedHashSet<>()).addAll(moving);
      }
    }

    @Override
    public void deleteAllFor(long learnerId) {
      solved.remove(learnerId);
    }
  }
}

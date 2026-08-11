package com.aramigo.api.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;

/**
 * Outbound port for the learning curriculum (units, lessons, exercises).
 *
 * <p>Writes are slug-keyed upserts so re-seeding edited content keeps database
 * ids stable and learner progress intact.
 */
public interface CurriculumRepositoryPort {

  Optional<LessonUnit> findUnit(int sectionNumber, int unitNumber);

  List<LessonUnit> findAllUnits();

  List<Lesson> findLessonsByUnitId(long unitId);

  /** Whole path in one query — the path screen shouldn't fan out per lesson. */
  List<Lesson> findAllLessons();

  Optional<Lesson> findLessonById(long lessonId);

  List<Exercise> findExercisesByLessonId(long lessonId);

  List<Exercise> findExercisesByLessonIds(Collection<Long> lessonIds);

  Optional<Exercise> findExerciseById(long exerciseId);

  LessonUnit upsertUnit(LessonUnit unit);

  Lesson upsertLesson(Lesson lesson);

  Exercise upsertExercise(Exercise exercise);

  /** Removes content whose slug disappeared from the data files. */
  void deleteUnitsNotIn(Collection<String> keptSlugs);

  void deleteLessonsNotIn(Collection<String> keptSlugs);

  void deleteExercisesNotIn(Collection<String> keptSlugs);
}

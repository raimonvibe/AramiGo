package com.aramigo.api.application.port.out;

import java.util.List;
import java.util.Optional;

import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;

/** Outbound port for the learning curriculum (units, lessons, exercises). */
public interface CurriculumRepositoryPort {

  Optional<LessonUnit> findUnit(int sectionNumber, int unitNumber);

  List<Lesson> findLessonsByUnitId(long unitId);

  Optional<Lesson> findLessonById(long lessonId);

  List<Exercise> findExercisesByLessonId(long lessonId);

  Optional<Exercise> findExerciseById(long exerciseId);

  long countUnits();

  LessonUnit saveUnit(LessonUnit unit);

  Lesson saveLesson(Lesson lesson);

  Exercise saveExercise(Exercise exercise);

  void deleteAllCurriculum();
}

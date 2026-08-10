package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;

import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonUnitJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.mapper.DomainMapper;
import com.aramigo.api.infrastructure.persistence.jpa.spring.ExerciseJpaRepository;
import com.aramigo.api.infrastructure.persistence.jpa.spring.LessonJpaRepository;
import com.aramigo.api.infrastructure.persistence.jpa.spring.LessonUnitJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class CurriculumRepositoryAdapter implements CurriculumRepositoryPort {

  private final LessonUnitJpaRepository units;
  private final LessonJpaRepository lessons;
  private final ExerciseJpaRepository exercises;

  public CurriculumRepositoryAdapter(
      LessonUnitJpaRepository units, LessonJpaRepository lessons, ExerciseJpaRepository exercises) {
    this.units = units;
    this.lessons = lessons;
    this.exercises = exercises;
  }

  @Override
  public Optional<LessonUnit> findUnit(int sectionNumber, int unitNumber) {
    return units
        .findBySectionNumberAndUnitNumber(sectionNumber, unitNumber)
        .map(DomainMapper::toDomain);
  }

  @Override
  public List<Lesson> findLessonsByUnitId(long unitId) {
    return lessons.findByUnitIdOrderByPositionAsc(unitId).stream()
        .map(DomainMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Lesson> findLessonById(long lessonId) {
    return lessons.findById(lessonId).map(DomainMapper::toDomain);
  }

  @Override
  public List<Exercise> findExercisesByLessonId(long lessonId) {
    return exercises.findByLessonIdOrderByPositionAsc(lessonId).stream()
        .map(DomainMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Exercise> findExerciseById(long exerciseId) {
    return exercises.findById(exerciseId).map(DomainMapper::toDomain);
  }

  @Override
  public long countUnits() {
    return units.count();
  }

  @Override
  public LessonUnit saveUnit(LessonUnit unit) {
    LessonUnitJpaEntity saved =
        units.save(
            new LessonUnitJpaEntity(
                unit.getSectionNumber(),
                unit.getUnitNumber(),
                unit.getTitle(),
                unit.getDescription()));
    return DomainMapper.toDomain(saved);
  }

  @Override
  public Lesson saveLesson(Lesson lesson) {
    LessonJpaEntity saved =
        lessons.save(
            new LessonJpaEntity(
                lesson.getUnitId(),
                lesson.getPosition(),
                lesson.getTitle(),
                lesson.getNodeKind().name()));
    return DomainMapper.toDomain(saved);
  }

  @Override
  public Exercise saveExercise(Exercise exercise) {
    ExerciseJpaEntity saved =
        exercises.save(
            new ExerciseJpaEntity(
                exercise.getLessonId(),
                exercise.getPosition(),
                exercise.getType().name(),
                exercise.getPrompt(),
                exercise.getAramaicScript(),
                exercise.getTransliteration(),
                exercise.getCorrectTokens(),
                exercise.getDistractorTokens()));
    return DomainMapper.toDomain(saved);
  }

  @Override
  public void deleteAllCurriculum() {
    exercises.deleteAll();
    lessons.deleteAll();
    units.deleteAll();
  }
}

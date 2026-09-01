package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.util.Collection;
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
  public List<LessonUnit> findAllUnits() {
    return units.findAll().stream().map(DomainMapper::toDomain).toList();
  }

  @Override
  public List<Lesson> findLessonsByUnitId(long unitId) {
    return lessons.findByUnitIdOrderByPositionAsc(unitId).stream()
        .map(DomainMapper::toDomain)
        .toList();
  }

  @Override
  public List<Lesson> findAllLessons() {
    return lessons.findAllByOrderByPositionAsc().stream().map(DomainMapper::toDomain).toList();
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
  public List<Exercise> findExercisesByLessonIds(Collection<Long> lessonIds) {
    if (lessonIds.isEmpty()) {
      return List.of();
    }
    return exercises.findByLessonIdInOrderByLessonIdAscPositionAsc(lessonIds).stream()
        .map(DomainMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Exercise> findExerciseById(long exerciseId) {
    return exercises.findById(exerciseId).map(DomainMapper::toDomain);
  }

  @Override
  public LessonUnit upsertUnit(LessonUnit unit) {
    LessonUnitJpaEntity entity =
        units.findBySlug(unit.slug()).orElseGet(() -> new LessonUnitJpaEntity(unit.slug()));
    DomainMapper.copyToEntity(unit, entity);
    return DomainMapper.toDomain(units.save(entity));
  }

  @Override
  public Lesson upsertLesson(Lesson lesson) {
    LessonJpaEntity entity =
        lessons.findBySlug(lesson.slug()).orElseGet(() -> new LessonJpaEntity(lesson.slug()));
    DomainMapper.copyToEntity(lesson, entity);
    return DomainMapper.toDomain(lessons.save(entity));
  }

  @Override
  public Exercise upsertExercise(Exercise exercise) {
    ExerciseJpaEntity entity =
        exercises
            .findBySlug(exercise.slug())
            .orElseGet(() -> new ExerciseJpaEntity(exercise.slug()));
    DomainMapper.copyToEntity(exercise, entity);
    return DomainMapper.toDomain(exercises.save(entity));
  }

  @Override
  public void deleteUnitsNotIn(Collection<String> keptSlugs) {
    if (keptSlugs.isEmpty()) {
      units.deleteAll();
      return;
    }
    units.deleteBySlugNotIn(keptSlugs);
  }

  @Override
  public void deleteLessonsNotIn(Collection<String> keptSlugs) {
    if (keptSlugs.isEmpty()) {
      lessons.deleteAll();
      return;
    }
    lessons.deleteBySlugNotIn(keptSlugs);
  }

  @Override
  public void deleteExercisesNotIn(Collection<String> keptSlugs) {
    if (keptSlugs.isEmpty()) {
      exercises.deleteAll();
      return;
    }
    exercises.deleteBySlugNotIn(keptSlugs);
  }
}

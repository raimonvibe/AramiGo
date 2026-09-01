package com.aramigo.api.infrastructure.persistence.jpa.mapper;

import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.model.NodeKind;
import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LearnerJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonUnitJpaEntity;

public final class DomainMapper {

  private DomainMapper() {}

  public static Learner toDomain(LearnerJpaEntity entity) {
    return new Learner(
        entity.getId(),
        entity.getIdentityKey(),
        entity.getDisplayName(),
        entity.getEnergy(),
        entity.getEnergyUpdatedAt(),
        entity.getGems(),
        entity.getStreak(),
        entity.getLastLessonDate(),
        entity.getHighestCompletedPosition());
  }

  public static void copyToEntity(Learner domain, LearnerJpaEntity entity) {
    entity.setIdentityKey(domain.getIdentityKey());
    entity.setDisplayName(domain.getDisplayName());
    entity.setEnergy(domain.getEnergy());
    entity.setEnergyUpdatedAt(domain.getEnergyUpdatedAt());
    entity.setGems(domain.getGems());
    entity.setStreak(domain.getStreak());
    entity.setLastLessonDate(domain.getLastLessonDate());
    entity.setHighestCompletedPosition(domain.getHighestCompletedPosition());
  }

  public static LessonUnit toDomain(LessonUnitJpaEntity entity) {
    return new LessonUnit(
        entity.getId(),
        entity.getSlug(),
        entity.getSectionNumber(),
        entity.getUnitNumber(),
        entity.getTitle(),
        entity.getDescription());
  }

  public static void copyToEntity(LessonUnit domain, LessonUnitJpaEntity entity) {
    entity.setSectionNumber(domain.sectionNumber());
    entity.setUnitNumber(domain.unitNumber());
    entity.setTitle(domain.title());
    entity.setDescription(domain.description());
  }

  public static Lesson toDomain(LessonJpaEntity entity) {
    return new Lesson(
        entity.getId(),
        entity.getSlug(),
        entity.getUnitId(),
        entity.getPosition(),
        entity.getTitle(),
        NodeKind.valueOf(entity.getNodeKind()));
  }

  public static void copyToEntity(Lesson domain, LessonJpaEntity entity) {
    entity.setUnitId(domain.unitId());
    entity.setPosition(domain.position());
    entity.setTitle(domain.title());
    entity.setNodeKind(domain.nodeKind().name());
  }

  public static Exercise toDomain(ExerciseJpaEntity entity) {
    return new Exercise(
        entity.getId(),
        entity.getSlug(),
        entity.getLessonId(),
        entity.getPosition(),
        ExerciseType.valueOf(entity.getType()),
        entity.getPrompt(),
        entity.getAramaicScript(),
        entity.getTransliteration(),
        entity.getCorrectTokens(),
        entity.getDistractorTokens());
  }

  public static void copyToEntity(Exercise domain, ExerciseJpaEntity entity) {
    entity.setLessonId(domain.lessonId());
    entity.setPosition(domain.position());
    entity.setType(domain.type().name());
    entity.setPrompt(domain.prompt());
    entity.setAramaicScript(domain.aramaicScript());
    entity.setTransliteration(domain.transliteration());
    entity.setCorrectTokens(domain.correctTokens());
    entity.setDistractorTokens(domain.distractorTokens());
  }
}

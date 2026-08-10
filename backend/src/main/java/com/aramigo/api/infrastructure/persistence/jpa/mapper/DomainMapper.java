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
        entity.getGuestKey(),
        entity.getEnergy(),
        entity.getGems(),
        entity.getStreak(),
        entity.getHighestCompletedPosition());
  }

  public static void copyToEntity(Learner domain, LearnerJpaEntity entity) {
    entity.setGuestKey(domain.getGuestKey());
    entity.setEnergy(domain.getEnergy());
    entity.setGems(domain.getGems());
    entity.setStreak(domain.getStreak());
    entity.setHighestCompletedPosition(domain.getHighestCompletedPosition());
  }

  public static LessonUnit toDomain(LessonUnitJpaEntity entity) {
    return new LessonUnit(
        entity.getId(),
        entity.getSectionNumber(),
        entity.getUnitNumber(),
        entity.getTitle(),
        entity.getDescription());
  }

  public static Lesson toDomain(LessonJpaEntity entity) {
    return new Lesson(
        entity.getId(),
        entity.getUnitId(),
        entity.getPosition(),
        entity.getTitle(),
        NodeKind.valueOf(entity.getNodeKind()));
  }

  public static Exercise toDomain(ExerciseJpaEntity entity) {
    return new Exercise(
        entity.getId(),
        entity.getLessonId(),
        entity.getPosition(),
        ExerciseType.valueOf(entity.getType()),
        entity.getPrompt(),
        entity.getAramaicScript(),
        entity.getTransliteration(),
        entity.getCorrectTokens(),
        entity.getDistractorTokens());
  }
}

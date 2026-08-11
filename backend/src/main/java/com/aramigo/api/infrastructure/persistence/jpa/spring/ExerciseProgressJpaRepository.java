package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Collection;
import java.util.List;

import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseProgressJpaRepository
    extends JpaRepository<ExerciseProgressJpaEntity, Long> {

  List<ExerciseProgressJpaEntity> findByLearnerIdAndExerciseIdIn(
      Long learnerId, Collection<Long> exerciseIds);

  List<ExerciseProgressJpaEntity> findByLearnerId(Long learnerId);

  boolean existsByLearnerIdAndExerciseId(Long learnerId, Long exerciseId);

  void deleteByLearnerId(Long learnerId);
}

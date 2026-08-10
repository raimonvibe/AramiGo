package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.List;

import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseJpaEntity, Long> {
  List<ExerciseJpaEntity> findByLessonIdOrderByPositionAsc(Long lessonId);
}

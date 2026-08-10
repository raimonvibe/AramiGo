package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.List;

import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonJpaRepository extends JpaRepository<LessonJpaEntity, Long> {
  List<LessonJpaEntity> findByUnitIdOrderByPositionAsc(Long unitId);
}

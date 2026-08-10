package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonUnitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonUnitJpaRepository extends JpaRepository<LessonUnitJpaEntity, Long> {
  Optional<LessonUnitJpaEntity> findBySectionNumberAndUnitNumber(int sectionNumber, int unitNumber);
}

package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Collection;
import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonUnitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonUnitJpaRepository extends JpaRepository<LessonUnitJpaEntity, Long> {

  Optional<LessonUnitJpaEntity> findBySectionNumberAndUnitNumber(int sectionNumber, int unitNumber);

  Optional<LessonUnitJpaEntity> findBySlug(String slug);

  @Modifying
  @Query("delete from LessonUnitJpaEntity u where u.slug not in :slugs")
  void deleteBySlugNotIn(@Param("slugs") Collection<String> slugs);
}

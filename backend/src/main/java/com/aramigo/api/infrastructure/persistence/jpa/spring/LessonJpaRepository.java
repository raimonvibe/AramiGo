package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.LessonJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonJpaRepository extends JpaRepository<LessonJpaEntity, Long> {

  List<LessonJpaEntity> findByUnitIdOrderByPositionAsc(Long unitId);

  List<LessonJpaEntity> findAllByOrderByPositionAsc();

  Optional<LessonJpaEntity> findBySlug(String slug);

  @Modifying
  @Query("delete from LessonJpaEntity l where l.slug not in :slugs")
  void deleteBySlugNotIn(@Param("slugs") Collection<String> slugs);
}

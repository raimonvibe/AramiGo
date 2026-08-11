package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseJpaEntity, Long> {

  List<ExerciseJpaEntity> findByLessonIdOrderByPositionAsc(Long lessonId);

  List<ExerciseJpaEntity> findByLessonIdInOrderByLessonIdAscPositionAsc(Collection<Long> lessonIds);

  Optional<ExerciseJpaEntity> findBySlug(String slug);

  @Modifying
  @Query("delete from ExerciseJpaEntity e where e.slug not in :slugs")
  void deleteBySlugNotIn(@Param("slugs") Collection<String> slugs);
}

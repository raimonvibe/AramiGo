package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseProgressJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseProgressJpaRepository
    extends JpaRepository<ExerciseProgressJpaEntity, Long> {

  List<ExerciseProgressJpaEntity> findByLearnerIdAndExerciseIdIn(
      Long learnerId, Collection<Long> exerciseIds);

  List<ExerciseProgressJpaEntity> findByLearnerId(Long learnerId);

  boolean existsByLearnerIdAndExerciseId(Long learnerId, Long exerciseId);

  Optional<ExerciseProgressJpaEntity> findByLearnerIdAndExerciseId(Long learnerId, Long exerciseId);

  void deleteByLearnerId(Long learnerId);

  /**
   * Solved exercises that have come due, soonest first.
   *
   * <p>A null {@code dueAt} is a row from before scheduling existed. It sorts
   * first rather than being skipped: the learner solved it at some unknown past
   * point, so it has waited longest of anything here.
   */
  @Query(
      """
      select p from ExerciseProgressJpaEntity p
      where p.learnerId = :learnerId
        and (p.dueAt is null or p.dueAt <= :now)
      order by case when p.dueAt is null then 0 else 1 end, p.dueAt asc
      """)
  List<ExerciseProgressJpaEntity> findDue(Long learnerId, Instant now, Pageable page);

  @Query(
      """
      select count(p) from ExerciseProgressJpaEntity p
      where p.learnerId = :learnerId
        and (p.dueAt is null or p.dueAt <= :now)
      """)
  int countDue(Long learnerId, Instant now);
}

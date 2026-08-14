package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.aramigo.api.application.port.out.ExerciseProgressPort;
import com.aramigo.api.domain.model.ReviewState;
import org.springframework.data.domain.PageRequest;
import com.aramigo.api.infrastructure.persistence.jpa.entity.ExerciseProgressJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.spring.ExerciseProgressJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ExerciseProgressAdapter implements ExerciseProgressPort {

  private final ExerciseProgressJpaRepository jpa;

  public ExerciseProgressAdapter(ExerciseProgressJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public void recordCorrect(long learnerId, long exerciseId, Instant now) {
    Optional<ExerciseProgressJpaEntity> existing =
        jpa.findByLearnerIdAndExerciseId(learnerId, exerciseId);

    if (existing.isPresent()) {
      ExerciseProgressJpaEntity row = existing.get();
      row.schedule(row.reviewOr(now).afterCorrect(now));
      jpa.save(row);
      return;
    }

    try {
      jpa.save(new ExerciseProgressJpaEntity(learnerId, exerciseId, ReviewState.firstCorrect(now)));
    } catch (DataIntegrityViolationException alreadyRecorded) {
      // Answering the same exercise twice in parallel is harmless.
    }
  }

  @Override
  public void recordLapse(long learnerId, long exerciseId, Instant now) {
    // No row means the exercise has never been solved, so there is no schedule to
    // knock back — a first wrong answer costs energy and nothing else.
    jpa.findByLearnerIdAndExerciseId(learnerId, exerciseId)
        .ifPresent(
            row -> {
              row.schedule(row.reviewOr(now).afterLapse(now));
              jpa.save(row);
            });
  }

  @Override
  public List<Long> findDueForReview(long learnerId, Instant now, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    return jpa.findDue(learnerId, now, PageRequest.of(0, limit)).stream()
        .map(ExerciseProgressJpaEntity::getExerciseId)
        .toList();
  }

  @Override
  public int countDueForReview(long learnerId, Instant now) {
    return jpa.countDue(learnerId, now);
  }

  @Override
  public Set<Long> solvedAmong(long learnerId, Collection<Long> exerciseIds) {
    if (exerciseIds.isEmpty()) {
      return Set.of();
    }
    return jpa.findByLearnerIdAndExerciseIdIn(learnerId, exerciseIds).stream()
        .map(ExerciseProgressJpaEntity::getExerciseId)
        .collect(Collectors.toSet());
  }

  @Override
  public void transferAll(long fromLearnerId, long toLearnerId) {
    Set<Long> alreadyOwned =
        jpa.findByLearnerId(toLearnerId).stream()
            .map(ExerciseProgressJpaEntity::getExerciseId)
            .collect(Collectors.toSet());

    List<ExerciseProgressJpaEntity> moving =
        jpa.findByLearnerId(fromLearnerId).stream()
            .filter(row -> !alreadyOwned.contains(row.getExerciseId()))
            .toList();

    moving.forEach(row -> row.setLearnerId(toLearnerId));
    jpa.saveAll(moving);
    jpa.deleteByLearnerId(fromLearnerId);
  }

  @Override
  public void deleteAllFor(long learnerId) {
    jpa.deleteByLearnerId(learnerId);
  }
}

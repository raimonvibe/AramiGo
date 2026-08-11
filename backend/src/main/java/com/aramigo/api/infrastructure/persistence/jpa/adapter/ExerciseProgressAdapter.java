package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.aramigo.api.application.port.out.ExerciseProgressPort;
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
  public void recordSolved(long learnerId, long exerciseId) {
    if (jpa.existsByLearnerIdAndExerciseId(learnerId, exerciseId)) {
      return;
    }
    try {
      jpa.save(new ExerciseProgressJpaEntity(learnerId, exerciseId));
    } catch (DataIntegrityViolationException alreadyRecorded) {
      // Answering the same exercise twice in parallel is harmless.
    }
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

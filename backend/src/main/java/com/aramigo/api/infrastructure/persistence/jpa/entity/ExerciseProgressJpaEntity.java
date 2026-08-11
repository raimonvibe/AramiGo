package com.aramigo.api.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** One row per exercise a learner has answered correctly. */
@Entity
@Table(
    name = "exercise_progress",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_exercise_progress_learner_exercise",
            columnNames = {"learnerId", "exerciseId"}))
public class ExerciseProgressJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long learnerId;

  @Column(nullable = false)
  private Long exerciseId;

  protected ExerciseProgressJpaEntity() {}

  public ExerciseProgressJpaEntity(Long learnerId, Long exerciseId) {
    this.learnerId = learnerId;
    this.exerciseId = exerciseId;
  }

  public Long getId() {
    return id;
  }

  public Long getLearnerId() {
    return learnerId;
  }

  public void setLearnerId(Long learnerId) {
    this.learnerId = learnerId;
  }

  public Long getExerciseId() {
    return exerciseId;
  }
}

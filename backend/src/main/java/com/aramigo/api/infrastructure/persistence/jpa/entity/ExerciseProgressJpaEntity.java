package com.aramigo.api.infrastructure.persistence.jpa.entity;

import java.time.Instant;

import com.aramigo.api.domain.model.ReviewState;
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

  /**
   * Review ladder rung, and when this is next worth asking.
   *
   * <p>Both are nullable on purpose. Rows written before scheduling existed have
   * neither, and {@code ddl-auto=update} cannot invent a value for them — so null
   * is read as "solved at some unknown past point", which {@link
   * com.aramigo.api.domain.model.ReviewState#unscheduled} makes due immediately.
   * Backfilling instead would have meant guessing a date for every learner.
   */
  private Integer strength;

  private Instant dueAt;

  protected ExerciseProgressJpaEntity() {}

  public ExerciseProgressJpaEntity(Long learnerId, Long exerciseId, ReviewState review) {
    this.learnerId = learnerId;
    this.exerciseId = exerciseId;
    schedule(review);
  }

  public ReviewState reviewOr(Instant now) {
    if (strength == null || dueAt == null) {
      return ReviewState.unscheduled(now);
    }
    return new ReviewState(strength, dueAt);
  }

  public void schedule(ReviewState review) {
    this.strength = review.strength();
    this.dueAt = review.dueAt();
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

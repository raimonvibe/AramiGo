package com.aramigo.api.infrastructure.persistence.jpa.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learners")
public class LearnerJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** {@code guest:<uuid>} or {@code google:<sub>}. */
  @Column(nullable = false, unique = true)
  private String identityKey;

  private String displayName;

  @Column(nullable = false)
  private int energy = 25;

  private Instant energyUpdatedAt;

  @Column(nullable = false)
  private int gems = 0;

  @Column(nullable = false)
  private int streak = 0;

  private LocalDate lastLessonDate;

  @Column(nullable = false)
  private int highestCompletedPosition = 0;

  protected LearnerJpaEntity() {}

  public LearnerJpaEntity(String identityKey) {
    this.identityKey = identityKey;
  }

  public Long getId() {
    return id;
  }

  public String getIdentityKey() {
    return identityKey;
  }

  public void setIdentityKey(String identityKey) {
    this.identityKey = identityKey;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getEnergy() {
    return energy;
  }

  public void setEnergy(int energy) {
    this.energy = energy;
  }

  public Instant getEnergyUpdatedAt() {
    return energyUpdatedAt;
  }

  public void setEnergyUpdatedAt(Instant energyUpdatedAt) {
    this.energyUpdatedAt = energyUpdatedAt;
  }

  public int getGems() {
    return gems;
  }

  public void setGems(int gems) {
    this.gems = gems;
  }

  public int getStreak() {
    return streak;
  }

  public void setStreak(int streak) {
    this.streak = streak;
  }

  public LocalDate getLastLessonDate() {
    return lastLessonDate;
  }

  public void setLastLessonDate(LocalDate lastLessonDate) {
    this.lastLessonDate = lastLessonDate;
  }

  public int getHighestCompletedPosition() {
    return highestCompletedPosition;
  }

  public void setHighestCompletedPosition(int highestCompletedPosition) {
    this.highestCompletedPosition = highestCompletedPosition;
  }
}

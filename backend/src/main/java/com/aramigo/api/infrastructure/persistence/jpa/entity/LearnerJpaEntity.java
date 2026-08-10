package com.aramigo.api.infrastructure.persistence.jpa.entity;

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

  @Column(nullable = false, unique = true)
  private String guestKey;

  @Column(nullable = false)
  private int energy = 25;

  @Column(nullable = false)
  private int gems = 0;

  @Column(nullable = false)
  private int streak = 0;

  @Column(nullable = false)
  private int highestCompletedPosition = 0;

  protected LearnerJpaEntity() {}

  public LearnerJpaEntity(String guestKey) {
    this.guestKey = guestKey;
  }

  public Long getId() {
    return id;
  }

  public String getGuestKey() {
    return guestKey;
  }

  public void setGuestKey(String guestKey) {
    this.guestKey = guestKey;
  }

  public int getEnergy() {
    return energy;
  }

  public void setEnergy(int energy) {
    this.energy = energy;
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

  public int getHighestCompletedPosition() {
    return highestCompletedPosition;
  }

  public void setHighestCompletedPosition(int highestCompletedPosition) {
    this.highestCompletedPosition = highestCompletedPosition;
  }
}

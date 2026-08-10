package com.aramigo.api.domain.model;

/**
 * Learner aggregate — gamification + linear path progress.
 * Pure domain: no persistence or framework types.
 */
public class Learner {

  public static final int DEFAULT_ENERGY = 25;

  private Long id;
  private final String guestKey;
  private int energy;
  private int gems;
  private int streak;
  private int highestCompletedPosition;

  public Learner(String guestKey) {
    this(null, guestKey, DEFAULT_ENERGY, 0, 0, 0);
  }

  public Learner(
      Long id,
      String guestKey,
      int energy,
      int gems,
      int streak,
      int highestCompletedPosition) {
    this.id = id;
    this.guestKey = guestKey;
    this.energy = energy;
    this.gems = gems;
    this.streak = streak;
    this.highestCompletedPosition = highestCompletedPosition;
  }

  public Long getId() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
  }

  public String getGuestKey() {
    return guestKey;
  }

  public int getEnergy() {
    return energy;
  }

  public int getGems() {
    return gems;
  }

  public int getStreak() {
    return streak;
  }

  public int getHighestCompletedPosition() {
    return highestCompletedPosition;
  }

  public int currentLessonPosition() {
    return highestCompletedPosition + 1;
  }

  public boolean canAccess(int lessonPosition) {
    return lessonPosition <= currentLessonPosition();
  }

  public boolean hasEnergy() {
    return energy > 0;
  }

  public NodeStatus statusOf(int lessonPosition) {
    if (lessonPosition <= highestCompletedPosition) {
      return NodeStatus.COMPLETED;
    }
    if (lessonPosition == currentLessonPosition()) {
      return NodeStatus.CURRENT;
    }
    return NodeStatus.LOCKED;
  }

  public void spendEnergy(int amount) {
    this.energy = Math.max(0, this.energy - amount);
  }

  public void addEnergy(int amount) {
    this.energy += amount;
  }

  public void addGems(int amount) {
    this.gems += amount;
  }

  public void markLessonCompleted(int position) {
    if (position > this.highestCompletedPosition) {
      this.highestCompletedPosition = position;
    }
  }

  public LearnerStats stats() {
    return new LearnerStats(energy, gems, streak);
  }
}

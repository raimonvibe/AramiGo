package com.aramigo.api.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Learner aggregate — gamification + linear path progress.
 * Pure domain: no persistence or framework types.
 *
 * <p>{@code identityKey} is opaque to the domain. Adapters mint {@code guest:<uuid>}
 * for anonymous learners and {@code google:<sub>} once someone signs in.
 */
public class Learner {

  public static final int MAX_ENERGY = 25;

  /** One energy point returns per interval, so a bad run is never a dead end. */
  public static final Duration ENERGY_REGEN_INTERVAL = Duration.ofMinutes(10);

  private Long id;
  private String identityKey;
  private String displayName;
  private int energy;
  private Instant energyUpdatedAt;
  private int gems;
  private int streak;
  private LocalDate lastLessonDate;
  private int highestCompletedPosition;

  public Learner(String identityKey, Instant now) {
    this(null, identityKey, null, MAX_ENERGY, now, 0, 0, null, 0);
  }

  public Learner(
      Long id,
      String identityKey,
      String displayName,
      int energy,
      Instant energyUpdatedAt,
      int gems,
      int streak,
      LocalDate lastLessonDate,
      int highestCompletedPosition) {
    this.id = id;
    this.identityKey = identityKey;
    this.displayName = displayName;
    this.energy = energy;
    this.energyUpdatedAt = energyUpdatedAt;
    this.gems = gems;
    this.streak = streak;
    this.lastLessonDate = lastLessonDate;
    this.highestCompletedPosition = highestCompletedPosition;
  }

  public Long getId() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
  }

  public String getIdentityKey() {
    return identityKey;
  }

  public void reassignIdentity(String identityKey) {
    this.identityKey = identityKey;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void rename(String displayName) {
    this.displayName = displayName;
  }

  public int getEnergy() {
    return energy;
  }

  public Instant getEnergyUpdatedAt() {
    return energyUpdatedAt;
  }

  public int getGems() {
    return gems;
  }

  public int getStreak() {
    return streak;
  }

  public LocalDate getLastLessonDate() {
    return lastLessonDate;
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

  /**
   * Credits energy accrued since the last update. Called before every read so
   * running out is a pause, never a permanent lockout.
   *
   * @return true when the balance changed and the learner needs persisting
   */
  public boolean regenerateEnergy(Instant now) {
    if (energyUpdatedAt == null) {
      energyUpdatedAt = now;
      return true;
    }
    if (energy >= MAX_ENERGY) {
      // Keep the clock at "now" so a full learner doesn't bank offline credit.
      boolean moved = energyUpdatedAt.isBefore(now);
      energyUpdatedAt = now;
      return moved;
    }
    if (now.isBefore(energyUpdatedAt)) {
      return false;
    }

    long elapsedSeconds = Duration.between(energyUpdatedAt, now).toSeconds();
    long earned = elapsedSeconds / ENERGY_REGEN_INTERVAL.toSeconds();
    if (earned <= 0) {
      return false;
    }

    int credited = (int) Math.min(earned, (long) MAX_ENERGY - energy);
    energy += credited;
    energyUpdatedAt =
        energy >= MAX_ENERGY
            ? now
            : energyUpdatedAt.plus(ENERGY_REGEN_INTERVAL.multipliedBy(credited));
    return true;
  }

  /** Seconds until the next energy point lands, or 0 when the bar is full. */
  public long secondsUntilNextEnergy(Instant now) {
    if (energy >= MAX_ENERGY || energyUpdatedAt == null) {
      return 0;
    }
    long remaining =
        ENERGY_REGEN_INTERVAL.toSeconds() - Duration.between(energyUpdatedAt, now).toSeconds();
    return Math.max(0, remaining);
  }

  /**
   * Spends energy. Callers run {@link #regenerateEnergy} first, which parks the
   * clock at "now" while the bar is full — so the countdown to the next point
   * starts from the moment of the first loss, not from the last page load.
   */
  public void spendEnergy(int amount) {
    this.energy = Math.max(0, this.energy - amount);
  }

  public void addEnergy(int amount) {
    this.energy = Math.min(MAX_ENERGY, this.energy + amount);
  }

  public void addGems(int amount) {
    this.gems += amount;
  }

  /** @return true when this completion advanced the path (first time through) */
  public boolean markLessonCompleted(int position) {
    if (position > this.highestCompletedPosition) {
      this.highestCompletedPosition = position;
      return true;
    }
    return false;
  }

  /** Daily streak: consecutive calendar days with at least one finished lesson. */
  public void recordActivityOn(LocalDate today) {
    if (lastLessonDate == null) {
      streak = 1;
    } else if (lastLessonDate.equals(today)) {
      return;
    } else if (lastLessonDate.plusDays(1).equals(today)) {
      streak += 1;
    } else {
      streak = 1;
    }
    lastLessonDate = today;
  }

  /**
   * Folds an anonymous learner's progress into this one when they sign in.
   * Best-of on progress, sum on gems — signing in must never cost anything.
   */
  public void absorb(Learner guest) {
    this.highestCompletedPosition =
        Math.max(this.highestCompletedPosition, guest.highestCompletedPosition);
    this.gems += guest.gems;
    this.energy = Math.min(MAX_ENERGY, Math.max(this.energy, guest.energy));
    this.streak = Math.max(this.streak, guest.streak);
    if (guest.lastLessonDate != null
        && (this.lastLessonDate == null || guest.lastLessonDate.isAfter(this.lastLessonDate))) {
      this.lastLessonDate = guest.lastLessonDate;
    }
  }

  public LearnerStats stats(Instant now) {
    return new LearnerStats(
        energy, MAX_ENERGY, gems, streak, secondsUntilNextEnergy(now));
  }
}

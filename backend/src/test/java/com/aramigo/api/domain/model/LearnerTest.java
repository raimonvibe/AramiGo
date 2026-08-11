package com.aramigo.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LearnerTest {

  private static final Instant START = Instant.parse("2026-01-01T12:00:00Z");

  @Test
  void newLearnerStartsWithAFullBar() {
    Learner learner = new Learner("guest:new", START);

    assertEquals(Learner.MAX_ENERGY, learner.getEnergy());
    assertEquals(0, learner.stats(START).secondsUntilNextEnergy());
  }

  @Test
  void runningOutOfEnergyIsTemporary() {
    Learner learner = new Learner("guest:tired", START);
    for (int i = 0; i < Learner.MAX_ENERGY; i++) {
      learner.spendEnergy(1);
    }
    assertFalse(learner.hasEnergy());

    learner.regenerateEnergy(START.plus(Learner.ENERGY_REGEN_INTERVAL));

    assertTrue(learner.hasEnergy(), "energy must come back on its own — no permanent lockout");
    assertEquals(1, learner.getEnergy());
  }

  @Test
  void energyAccruesOncePerIntervalAndStopsAtTheCap() {
    Learner learner = new Learner("guest:away", START);
    learner.spendEnergy(5);

    learner.regenerateEnergy(START.plus(Duration.ofDays(7)));

    assertEquals(Learner.MAX_ENERGY, learner.getEnergy());
  }

  @Test
  void partialIntervalsDoNotCreditEnergy() {
    Learner learner = new Learner("guest:impatient", START);
    learner.spendEnergy(3);

    boolean changed = learner.regenerateEnergy(START.plus(Duration.ofMinutes(9)));

    assertFalse(changed);
    assertEquals(Learner.MAX_ENERGY - 3, learner.getEnergy());
  }

  @Test
  void leftoverTimeCarriesIntoTheNextInterval() {
    Learner learner = new Learner("guest:carry", START);
    learner.spendEnergy(3);

    learner.regenerateEnergy(START.plus(Duration.ofMinutes(15)));
    assertEquals(Learner.MAX_ENERGY - 2, learner.getEnergy());

    // Five minutes were already banked, so five more should be enough.
    learner.regenerateEnergy(START.plus(Duration.ofMinutes(20)));
    assertEquals(Learner.MAX_ENERGY - 1, learner.getEnergy());
  }

  @Test
  void aFullBarDoesNotBankOfflineCredit() {
    Learner learner = new Learner("guest:full", START);

    learner.regenerateEnergy(START.plus(Duration.ofDays(3)));
    learner.spendEnergy(5);
    learner.regenerateEnergy(START.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(1)));

    assertEquals(Learner.MAX_ENERGY - 5, learner.getEnergy());
  }

  @Test
  void streakCountsConsecutiveDays() {
    Learner learner = new Learner("guest:keen", START);
    LocalDate monday = LocalDate.of(2026, 1, 5);

    learner.recordActivityOn(monday);
    learner.recordActivityOn(monday.plusDays(1));
    learner.recordActivityOn(monday.plusDays(2));

    assertEquals(3, learner.getStreak());
  }

  @Test
  void twoLessonsOnTheSameDayCountOnce() {
    Learner learner = new Learner("guest:eager", START);
    LocalDate monday = LocalDate.of(2026, 1, 5);

    learner.recordActivityOn(monday);
    learner.recordActivityOn(monday);

    assertEquals(1, learner.getStreak());
  }

  @Test
  void missingADayResetsTheStreak() {
    Learner learner = new Learner("guest:lapsed", START);
    LocalDate monday = LocalDate.of(2026, 1, 5);

    learner.recordActivityOn(monday);
    learner.recordActivityOn(monday.plusDays(1));
    learner.recordActivityOn(monday.plusDays(5));

    assertEquals(1, learner.getStreak());
  }

  @Test
  void signingInKeepsTheBetterOfBothProgressRecords() {
    Learner account = new Learner("google:123", START);
    account.addGems(30);
    account.markLessonCompleted(2);

    Learner guest = new Learner("guest:abc", START);
    guest.addGems(10);
    guest.markLessonCompleted(7);
    guest.recordActivityOn(LocalDate.of(2026, 1, 5));

    account.absorb(guest);

    assertEquals(7, account.getHighestCompletedPosition());
    assertEquals(40, account.getGems());
    assertEquals(1, account.getStreak());
  }

  @Test
  void repeatingALessonDoesNotAdvanceThePath() {
    Learner learner = new Learner("guest:repeat", START);

    assertTrue(learner.markLessonCompleted(1));
    assertFalse(learner.markLessonCompleted(1), "replaying a lesson must not pay out again");
  }
}

package com.aramigo.api.domain.model;

public record LearnerStats(
    int energy, int maxEnergy, int gems, int streak, long secondsUntilNextEnergy) {}

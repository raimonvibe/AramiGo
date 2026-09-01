package com.aramigo.api.application.dto;

import com.aramigo.api.domain.model.LearnerStats;

/**
 * @param gemsSpent what the refill cost, so the UI can say so rather than
 *     leaving the learner to work it out from a number that dropped
 */
public record RefillEnergyResult(int gemsSpent, LearnerStats stats) {}

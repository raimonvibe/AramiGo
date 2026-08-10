package com.aramigo.api.application.dto;

import com.aramigo.api.domain.model.LearnerStats;

public record CheckAnswerResult(
    boolean correct, String message, String correctAnswer, int energyDelta, LearnerStats stats) {}

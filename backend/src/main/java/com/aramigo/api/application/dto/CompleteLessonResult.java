package com.aramigo.api.application.dto;

import com.aramigo.api.domain.model.LearnerStats;

public record CompleteLessonResult(int energyReward, int gemsReward, LearnerStats stats) {}

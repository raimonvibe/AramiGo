package com.aramigo.api.application.dto;

import com.aramigo.api.domain.model.LearnerStats;

public record ProfileResult(
    boolean signedIn,
    String displayName,
    LearnerStats stats,
    int completedLessons,
    int totalLessons) {}

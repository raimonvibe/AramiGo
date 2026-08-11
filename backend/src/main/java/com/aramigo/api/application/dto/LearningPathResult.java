package com.aramigo.api.application.dto;

import java.util.List;

import com.aramigo.api.domain.model.LearnerStats;
import com.aramigo.api.domain.model.NodeKind;
import com.aramigo.api.domain.model.NodeStatus;

public record LearningPathResult(LearnerStats stats, List<UnitResult> units) {

  public record UnitResult(
      int sectionNumber,
      int unitNumber,
      String title,
      String description,
      List<PathNodeResult> nodes) {}

  public record PathNodeResult(
      long lessonId,
      int position,
      String title,
      NodeKind nodeKind,
      NodeStatus status,
      int exerciseCount,
      int solvedCount) {}
}

package com.aramigo.api.application.dto;

import java.util.List;

import com.aramigo.api.domain.model.LearnerStats;
import com.aramigo.api.domain.model.NodeKind;
import com.aramigo.api.domain.model.NodeStatus;

/**
 * @param reviewDue solved exercises waiting to be reviewed, carried here so the
 *     home page can offer review without a second request
 */
public record LearningPathResult(LearnerStats stats, int reviewDue, List<UnitResult> units) {

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

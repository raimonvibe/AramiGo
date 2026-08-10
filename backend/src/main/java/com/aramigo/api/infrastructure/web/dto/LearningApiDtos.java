package com.aramigo.api.infrastructure.web.dto;

import java.util.List;

import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.NodeKind;
import com.aramigo.api.domain.model.NodeStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** HTTP-facing DTOs — stay at the inbound adapter edge. */
public final class LearningApiDtos {

  private LearningApiDtos() {}

  public record LearnerStatsResponse(int energy, int gems, int streak) {}

  public record PathNodeResponse(
      long lessonId, int position, String title, NodeKind nodeKind, NodeStatus status) {}

  public record LearningPathResponse(
      int sectionNumber,
      int unitNumber,
      String title,
      String description,
      LearnerStatsResponse stats,
      List<PathNodeResponse> nodes) {}

  public record ExerciseResponse(
      long id,
      ExerciseType type,
      String prompt,
      String tip,
      String aramaicScript,
      String transliteration,
      List<String> wordBank) {}

  public record LessonSessionResponse(
      long lessonId, String title, LearnerStatsResponse stats, List<ExerciseResponse> exercises) {}

  public record CheckAnswerRequest(@NotNull Long exerciseId, @NotEmpty List<String> tokens) {}

  public record CheckAnswerResponse(
      boolean correct,
      String message,
      String correctAnswer,
      int energyDelta,
      LearnerStatsResponse stats) {}

  public record CompleteLessonRequest(@NotNull Long lessonId) {}

  public record CompleteLessonResponse(
      int energyReward, int gemsReward, LearnerStatsResponse stats) {}

  public record ErrorResponse(String status, String message) {}
}

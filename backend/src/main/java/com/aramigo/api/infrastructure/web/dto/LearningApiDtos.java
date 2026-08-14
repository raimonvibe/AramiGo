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

  public record LearnerStatsResponse(
      int energy, int maxEnergy, int gems, int streak, long secondsUntilNextEnergy) {}

  public record PathNodeResponse(
      long lessonId,
      int position,
      String title,
      NodeKind nodeKind,
      NodeStatus status,
      int exerciseCount,
      int solvedCount) {}

  public record PathUnitResponse(
      int sectionNumber,
      int unitNumber,
      String title,
      String description,
      List<PathNodeResponse> nodes) {}

  public record LearningPathResponse(
      LearnerStatsResponse stats, int reviewDue, List<PathUnitResponse> units) {}

  public record ExerciseResponse(
      long id,
      ExerciseType type,
      String prompt,
      String tip,
      String aramaicScript,
      String transliteration,
      String audioText,
      List<String> wordBank) {}

  public record LessonSessionResponse(
      long lessonId, String title, LearnerStatsResponse stats, List<ExerciseResponse> exercises) {}

  /**
   * No lesson id and no title: a review is drawn from the whole curriculum and
   * has nothing to complete, so there is nothing for the client to post back.
   *
   * @param dueCount everything waiting, which may exceed {@code exercises}
   */
  public record ReviewSessionResponse(
      int dueCount, LearnerStatsResponse stats, List<ExerciseResponse> exercises) {}

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

  public record ProfileResponse(
      boolean signedIn,
      String displayName,
      String email,
      String pictureUrl,
      LearnerStatsResponse stats,
      int completedLessons,
      int totalLessons) {}

  public record ErrorResponse(String status, String code, String message) {}
}

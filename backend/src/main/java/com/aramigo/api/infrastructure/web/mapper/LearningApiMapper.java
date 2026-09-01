package com.aramigo.api.infrastructure.web.mapper;

import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.dto.LessonSessionResult.ExerciseView;
import com.aramigo.api.application.dto.ProfileResult;
import com.aramigo.api.application.dto.RefillEnergyResult;
import com.aramigo.api.application.dto.ReviewSessionResult;
import com.aramigo.api.domain.model.LearnerStats;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ExerciseResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearnerStatsResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearningPathResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LessonSessionResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.PathNodeResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.PathUnitResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ProfileResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.RefillEnergyResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ReviewSessionResponse;

public final class LearningApiMapper {

  private LearningApiMapper() {}

  public static LearningPathResponse toResponse(LearningPathResult result) {
    return new LearningPathResponse(
        stats(result.stats()),
        result.reviewDue(),
        result.units().stream()
            .map(
                unit ->
                    new PathUnitResponse(
                        unit.sectionNumber(),
                        unit.unitNumber(),
                        unit.title(),
                        unit.description(),
                        unit.nodes().stream()
                            .map(
                                node ->
                                    new PathNodeResponse(
                                        node.lessonId(),
                                        node.position(),
                                        node.title(),
                                        node.nodeKind(),
                                        node.status(),
                                        node.exerciseCount(),
                                        node.solvedCount()))
                            .toList()))
            .toList());
  }

  public static LessonSessionResponse toResponse(LessonSessionResult result) {
    return new LessonSessionResponse(
        result.lessonId(), result.title(), stats(result.stats()), exercises(result.exercises()));
  }

  public static RefillEnergyResponse toResponse(RefillEnergyResult result) {
    return new RefillEnergyResponse(result.gemsSpent(), stats(result.stats()));
  }

  public static ReviewSessionResponse toResponse(ReviewSessionResult result) {
    return new ReviewSessionResponse(
        result.dueCount(), stats(result.stats()), exercises(result.exercises()));
  }

  private static List<ExerciseResponse> exercises(List<ExerciseView> views) {
    return views.stream()
        .map(
            exercise ->
                new ExerciseResponse(
                    exercise.id(),
                    exercise.type(),
                    exercise.prompt(),
                    exercise.tip(),
                    exercise.aramaicScript(),
                    exercise.transliteration(),
                    exercise.audioText(),
                    exercise.wordBank()))
        .toList();
  }

  public static CheckAnswerResponse toResponse(CheckAnswerResult result) {
    return new CheckAnswerResponse(
        result.correct(),
        result.message(),
        result.correctAnswer(),
        result.energyDelta(),
        stats(result.stats()));
  }

  public static CompleteLessonResponse toResponse(CompleteLessonResult result) {
    return new CompleteLessonResponse(
        result.energyReward(), result.gemsReward(), stats(result.stats()));
  }

  public static ProfileResponse toResponse(ProfileResult result, String email, String pictureUrl) {
    return new ProfileResponse(
        result.signedIn(),
        result.displayName(),
        email,
        pictureUrl,
        stats(result.stats()),
        result.completedLessons(),
        result.totalLessons());
  }

  private static LearnerStatsResponse stats(LearnerStats stats) {
    return new LearnerStatsResponse(
        stats.energy(),
        stats.maxEnergy(),
        stats.gems(),
        stats.streak(),
        stats.secondsUntilNextEnergy());
  }
}

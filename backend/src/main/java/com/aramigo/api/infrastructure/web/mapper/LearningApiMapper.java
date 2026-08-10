package com.aramigo.api.infrastructure.web.mapper;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.domain.model.LearnerStats;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ExerciseResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearnerStatsResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearningPathResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LessonSessionResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.PathNodeResponse;

public final class LearningApiMapper {

  private LearningApiMapper() {}

  public static LearningPathResponse toResponse(LearningPathResult result) {
    return new LearningPathResponse(
        result.sectionNumber(),
        result.unitNumber(),
        result.title(),
        result.description(),
        stats(result.stats()),
        result.nodes().stream()
            .map(
                node ->
                    new PathNodeResponse(
                        node.lessonId(),
                        node.position(),
                        node.title(),
                        node.nodeKind(),
                        node.status()))
            .toList());
  }

  public static LessonSessionResponse toResponse(LessonSessionResult result) {
    return new LessonSessionResponse(
        result.lessonId(),
        result.title(),
        stats(result.stats()),
        result.exercises().stream()
            .map(
                exercise ->
                    new ExerciseResponse(
                        exercise.id(),
                        exercise.type(),
                        exercise.prompt(),
                        exercise.tip(),
                        exercise.aramaicScript(),
                        exercise.transliteration(),
                        exercise.wordBank()))
            .toList());
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

  private static LearnerStatsResponse stats(LearnerStats stats) {
    return new LearnerStatsResponse(stats.energy(), stats.gems(), stats.streak());
  }
}

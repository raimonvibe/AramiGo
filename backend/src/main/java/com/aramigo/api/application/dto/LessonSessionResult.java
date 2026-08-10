package com.aramigo.api.application.dto;

import java.util.List;

import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.LearnerStats;

public record LessonSessionResult(
    long lessonId, String title, LearnerStats stats, List<ExerciseView> exercises) {

  public record ExerciseView(
      long id,
      ExerciseType type,
      String prompt,
      String tip,
      String aramaicScript,
      String transliteration,
      List<String> wordBank) {}
}

package com.aramigo.api.application.dto;

import java.util.List;

import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.LearnerStats;

public record LessonSessionResult(
    long lessonId, String title, LearnerStats stats, List<ExerciseView> exercises) {

  /**
   * What the client is allowed to see. {@code transliteration} is withheld for
   * exercise types where printing it would be the answer.
   */
  public record ExerciseView(
      long id,
      ExerciseType type,
      String prompt,
      String tip,
      String aramaicScript,
      String transliteration,
      String audioText,
      List<String> wordBank) {}
}

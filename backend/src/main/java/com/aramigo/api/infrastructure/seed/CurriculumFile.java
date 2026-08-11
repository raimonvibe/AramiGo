package com.aramigo.api.infrastructure.seed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Shape of one file under {@code resources/curriculum/}.
 *
 * <p>Content lives in data, not in Java, so adding lessons is an edit rather than
 * a code change. Every level carries a stable {@code slug}: re-seeding matches on
 * it, which keeps database ids — and the progress pointing at them — intact.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CurriculumFile(
    String slug,
    int sectionNumber,
    int unitNumber,
    String title,
    String description,
    List<LessonFile> lessons) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LessonFile(
      String slug, String title, String nodeKind, List<ExerciseFile> exercises) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ExerciseFile(
      String slug,
      String type,
      String prompt,
      String aramaicScript,
      String transliteration,
      String correctTokens,
      String distractorTokens) {}
}

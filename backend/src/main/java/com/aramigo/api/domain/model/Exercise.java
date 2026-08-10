package com.aramigo.api.domain.model;

/**
 * One prompt inside a lesson.
 *
 * {@code correctTokens} may list alternate accepted answers separated by {@code |}.
 */
public class Exercise {

  private Long id;
  private final Long lessonId;
  private final int position;
  private final ExerciseType type;
  private final String prompt;
  private final String aramaicScript;
  private final String transliteration;
  private final String correctTokens;
  private final String distractorTokens;

  public Exercise(
      Long lessonId,
      int position,
      ExerciseType type,
      String prompt,
      String aramaicScript,
      String transliteration,
      String correctTokens,
      String distractorTokens) {
    this(
        null,
        lessonId,
        position,
        type,
        prompt,
        aramaicScript,
        transliteration,
        correctTokens,
        distractorTokens);
  }

  public Exercise(
      Long id,
      Long lessonId,
      int position,
      ExerciseType type,
      String prompt,
      String aramaicScript,
      String transliteration,
      String correctTokens,
      String distractorTokens) {
    this.id = id;
    this.lessonId = lessonId;
    this.position = position;
    this.type = type;
    this.prompt = prompt;
    this.aramaicScript = aramaicScript;
    this.transliteration = transliteration;
    this.correctTokens = correctTokens;
    this.distractorTokens = distractorTokens;
  }

  public Long getId() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
  }

  public Long getLessonId() {
    return lessonId;
  }

  public int getPosition() {
    return position;
  }

  public ExerciseType getType() {
    return type;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getAramaicScript() {
    return aramaicScript;
  }

  public String getTransliteration() {
    return transliteration;
  }

  public String getCorrectTokens() {
    return correctTokens;
  }

  public String getDistractorTokens() {
    return distractorTokens;
  }
}

package com.aramigo.api.domain.model;

/**
 * One prompt inside a lesson.
 *
 * <p>{@code slug} is the stable natural key from the curriculum data files, so
 * re-seeding edited content keeps database ids (and therefore learner progress)
 * pointing at the same exercise.
 *
 * <p>{@code correctTokens} may list alternate accepted answers separated by {@code |}.
 */
public record Exercise(
    Long id,
    String slug,
    Long lessonId,
    int position,
    ExerciseType type,
    String prompt,
    String aramaicScript,
    String transliteration,
    String correctTokens,
    String distractorTokens) {

  public Exercise withIdentity(Long id, Long lessonId) {
    return new Exercise(
        id,
        slug,
        lessonId,
        position,
        type,
        prompt,
        aramaicScript,
        transliteration,
        correctTokens,
        distractorTokens);
  }

  /**
   * True when showing the transliteration alongside the prompt would hand the
   * learner the answer instead of making them listen or read the script.
   */
  public boolean transliterationGivesAwayAnswer() {
    return type == ExerciseType.LISTEN_BUILD_ARAMAIC
        || type == ExerciseType.TRANSLATE_TO_ARAMAIC
        || type == ExerciseType.TAP_WHAT_YOU_HEAR
        || type == ExerciseType.MATCH_PAIRS;
  }

  public boolean hasAudioPrompt() {
    return type == ExerciseType.LISTEN_CHOOSE_MEANING
        || type == ExerciseType.LISTEN_BUILD_ARAMAIC
        || type == ExerciseType.TAP_WHAT_YOU_HEAR;
  }
}

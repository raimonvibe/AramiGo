package com.aramigo.api.domain.model;

public enum ExerciseType {
  /** Read Syriac script, choose the English meaning. */
  TRANSLATE_TO_ENGLISH,

  /** Read English, build the phrase from Syriac chips. */
  TRANSLATE_TO_ARAMAIC,

  /** Hear Syriac, choose the English meaning. */
  LISTEN_CHOOSE_MEANING,

  /** Hear Syriac, rebuild it from Syriac chips (dictation). */
  LISTEN_BUILD_ARAMAIC,

  /**
   * Match Syriac script chips to English meanings.
   *
   * <p>Curriculum format: {@code ܫܠܡܐ=hello ܠܚܡܐ=bread}, or {@code ;}-separated
   * pairs when a meaning has spaces. Submitted tokens are {@code script|meaning}
   * pairs; order of pairs does not matter.
   */
  MATCH_PAIRS,

  /** Hear Syriac, tap the matching Syriac script among distractors. */
  TAP_WHAT_YOU_HEAR
}

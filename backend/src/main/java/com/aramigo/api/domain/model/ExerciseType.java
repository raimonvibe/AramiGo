package com.aramigo.api.domain.model;

public enum ExerciseType {
  /** Read Syriac script, choose the English meaning. */
  TRANSLATE_TO_ENGLISH,

  /** Read English, build the phrase from Syriac chips. */
  TRANSLATE_TO_ARAMAIC,

  /** Hear Syriac, choose the English meaning. */
  LISTEN_CHOOSE_MEANING,

  /** Hear Syriac, rebuild it from Syriac chips (dictation). */
  LISTEN_BUILD_ARAMAIC
}

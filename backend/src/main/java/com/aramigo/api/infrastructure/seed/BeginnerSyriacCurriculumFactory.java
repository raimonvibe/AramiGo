package com.aramigo.api.infrastructure.seed;

import java.util.ArrayList;
import java.util.List;

import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.model.NodeKind;

/**
 * Factory for Unit 1 beginner Classical Syriac content.
 * Keeps glyphs as Unicode escapes so source files stay ASCII-safe.
 */
public final class BeginnerSyriacCurriculumFactory {

  private static final String ALAPH = "\u0710";
  private static final String BETH = "\u0712";
  private static final String HETH = "\u071A";
  private static final String YUDH = "\u071D";
  private static final String KAPH = "\u071F";
  private static final String LAMADH = "\u0720";
  private static final String MIM = "\u0721";
  private static final String NUN = "\u0722";
  private static final String E = "\u0725";
  private static final String RISH = "\u072A";
  private static final String SHIN = "\u072B";
  private static final String TAW = "\u072C";

  private static final String SHLOMO = SHIN + LAMADH + MIM + ALAPH;
  private static final String BRIKH = BETH + RISH + YUDH + KAPH;
  private static final String ENO = ALAPH + NUN + ALAPH;
  private static final String AT = ALAPH + NUN + TAW;
  private static final String ALAYK = E + LAMADH + YUDH + KAPH;
  private static final String IT = ALAPH + YUDH + TAW;
  private static final String LI = LAMADH + YUDH;
  private static final String LAHMA = LAMADH + HETH + MIM + ALAPH;
  private static final String SHOTE = SHIN + TAW + ALAPH;
  private static final String MAYA = MIM + YUDH + ALAPH;
  private static final String SHMAKH = SHIN + MIM + KAPH;

  private BeginnerSyriacCurriculumFactory() {}

  public record CurriculumBlueprint(
      LessonUnit unit, List<LessonBlueprint> lessons) {}

  public record LessonBlueprint(Lesson lesson, List<Exercise> exercises) {}

  public static CurriculumBlueprint unitOne() {
    LessonUnit unit =
        new LessonUnit(
            1,
            1,
            "Use basic phrases",
            "Greetings and simple sentences in Classical Syriac Aramaic");

    List<LessonBlueprint> lessons = new ArrayList<>();
    lessons.add(
        lesson(
            1,
            "Greetings",
            NodeKind.STAR,
            List.of(
                exercise(
                    1,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "What does this mean?",
                    SHLOMO,
                    "shlomo",
                    "hello|peace",
                    "water bread"),
                exercise(
                    2,
                    ExerciseType.LISTEN,
                    "Tap what you hear",
                    BRIKH,
                    "brikh",
                    "blessed",
                    "house you"),
                exercise(
                    3,
                    ExerciseType.TRANSLATE_TO_ARAMAIC,
                    "How do you say hello?",
                    null,
                    null,
                    "shlomo",
                    "lahma maya"))));
    lessons.add(
        lesson(
            2,
            "Names",
            NodeKind.STAR,
            List.of(
                exercise(
                    1,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "What does this mean?",
                    ENO,
                    "eno",
                    "I",
                    "you they"),
                exercise(
                    2,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "What does this mean?",
                    AT,
                    "at",
                    "you",
                    "I water"),
                exercise(
                    3,
                    ExerciseType.TRANSLATE_TO_ARAMAIC,
                    "How do you say \"I\"?",
                    null,
                    null,
                    "eno",
                    "at shlomo"))));
    lessons.add(
        lesson(
            3,
            "Treasure",
            NodeKind.CHEST,
            List.of(
                exercise(
                    1,
                    ExerciseType.LISTEN,
                    "Tap what you hear",
                    SHLOMO + " " + ALAYK,
                    "shlomo alayk",
                    "peace upon you",
                    "bread house"))));
    lessons.add(
        lesson(
            4,
            "Simple sentences",
            NodeKind.STAR,
            List.of(
                exercise(
                    1,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "Build the English sentence",
                    IT + " " + LI + " " + LAHMA,
                    "it li lahma",
                    "I have bread",
                    "you drink milk"),
                exercise(
                    2,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "Build the English sentence",
                    AT + " " + SHOTE + " " + MAYA,
                    "at shote maya",
                    "you drink water",
                    "I have bread"),
                exercise(
                    3,
                    ExerciseType.TRANSLATE_TO_ARAMAIC,
                    "Say \"I have bread\"",
                    null,
                    null,
                    "it li lahma",
                    "at shote maya"))));
    lessons.add(
        lesson(
            5,
            "Practice with Arami",
            NodeKind.CHARACTER,
            List.of(
                exercise(
                    1,
                    ExerciseType.LISTEN,
                    "Tap what you hear",
                    SHLOMO,
                    "shlomo",
                    "hello|peace",
                    "blessed water"),
                exercise(
                    2,
                    ExerciseType.TRANSLATE_TO_ENGLISH,
                    "Build the English sentence",
                    BRIKH + " " + SHMAKH,
                    "brikh shmakh",
                    "blessed is your name",
                    "I have bread"))));

    return new CurriculumBlueprint(unit, lessons);
  }

  private static LessonBlueprint lesson(
      int position, String title, NodeKind kind, List<Exercise> exercises) {
    // unitId filled in when persisted
    return new LessonBlueprint(new Lesson(null, position, title, kind), exercises);
  }

  private static Exercise exercise(
      int position,
      ExerciseType type,
      String prompt,
      String script,
      String transliteration,
      String correct,
      String distractors) {
    return new Exercise(
        null, position, type, prompt, script, transliteration, correct, distractors);
  }
}

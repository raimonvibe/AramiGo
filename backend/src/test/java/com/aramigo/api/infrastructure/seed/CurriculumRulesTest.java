package com.aramigo.api.infrastructure.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.ObjectMapper;

/**
 * Guards the authoring rules the README states for {@code resources/curriculum/*.json}.
 *
 * <p>Content is the product here, and it is edited far more often than the code
 * around it. Every rule below is one that produces a broken or unfair exercise
 * rather than a crash, so nothing else would catch it: the app boots happily on
 * a lesson whose only word bank entry is the answer.
 *
 * <p>Only mechanical rules live here. "Seven new words per unit" and "sentences
 * stay at two or three words" are judgement calls a machine reads badly — ܒܒܝܬܐ
 * is a new surface form but not really a new word — so they stay in review.
 */
class CurriculumRulesTest {

  private static final TokenAnswerMatchingPolicy POLICY = new TokenAnswerMatchingPolicy();

  private final List<CurriculumFile> curriculum = load();

  /** One exercise, with enough context to name it in a failure message. */
  private record Item(CurriculumFile unit, CurriculumFile.ExerciseFile exercise) {
    String name() {
      return exercise.slug() + " (unit " + unit.unitNumber() + ")";
    }
  }

  private List<Item> everyExercise() {
    List<Item> items = new ArrayList<>();
    for (CurriculumFile unit : curriculum) {
      for (CurriculumFile.LessonFile lesson : unit.lessons()) {
        for (CurriculumFile.ExerciseFile exercise : lesson.exercises()) {
          items.add(new Item(unit, exercise));
        }
      }
    }
    return items;
  }

  @Test
  void hasCurriculumToCheck() {
    assertThat(curriculum).isNotEmpty();
    assertThat(everyExercise()).isNotEmpty();
  }

  @Test
  void everySlugIsUnique() {
    List<String> slugs = new ArrayList<>();
    for (CurriculumFile unit : curriculum) {
      slugs.add(unit.slug());
      for (CurriculumFile.LessonFile lesson : unit.lessons()) {
        slugs.add(lesson.slug());
        lesson.exercises().forEach(exercise -> slugs.add(exercise.slug()));
      }
    }
    // Slugs are the seeder's natural key — a duplicate silently overwrites a row
    // and takes the learner progress attached to it somewhere unintended.
    assertThat(slugs).doesNotHaveDuplicates();
  }

  @Test
  void everyTypeIsKnown() {
    for (Item item : everyExercise()) {
      assertThat(ExerciseType.values())
          .describedAs("%s has an unknown type", item.name())
          .anyMatch(type -> type.name().equals(item.exercise().type()));
    }
  }

  @Test
  void everyStatedAnswerIsAccepted() {
    for (Item item : everyExercise()) {
      String spec = item.exercise().correctTokens();
      for (List<String> answer : acceptedAnswers(spec)) {
        assertThat(POLICY.matches(spec, answer))
            .describedAs("%s rejects its own answer %s", item.name(), answer)
            .isTrue();
      }
    }
  }

  @Test
  void everyExerciseOffersAWrongChoice() {
    for (Item item : everyExercise()) {
      List<String> bank =
          POLICY.wordBank(item.exercise().correctTokens(), item.exercise().distractorTokens());
      int answerSize = POLICY.bankTokensFromAnswers(item.exercise().correctTokens()).size();
      // A bank with nothing but the answer in it is a tap-to-continue, not a question.
      assertThat(bank.size())
          .describedAs("%s offers no wrong option — bank is %s", item.name(), bank)
          .isGreaterThan(answerSize);
    }
  }

  @Test
  void noDistractorDuplicatesTheAnswer() {
    List<String> duplicated = new ArrayList<>();

    for (Item item : everyExercise()) {
      Set<String> answerTokens = new HashSet<>();
      POLICY
          .bankTokensFromAnswers(item.exercise().correctTokens())
          .forEach(token -> answerTokens.add(normalize(token)));

      for (String distractor : tokens(item.exercise().distractorTokens())) {
        if (answerTokens.contains(normalize(distractor))) {
          duplicated.add(item.name() + " -> " + distractor);
        }
      }
    }

    // wordBank() drops these, so the exercise quietly ends up with fewer wrong
    // options than the author wrote — and possibly none at all.
    assertThat(duplicated).describedAs("distractors already present as answer chips").isEmpty();
  }

  @Test
  void alternativesAreEitherAllSingleWordOrAllPhrases() {
    for (Item item : everyExercise()) {
      List<List<String>> answers = acceptedAnswers(item.exercise().correctTokens());
      if (answers.size() < 2) {
        continue;
      }
      boolean anySingle = answers.stream().anyMatch(answer -> answer.size() == 1);
      boolean anyPhrase = answers.stream().anyMatch(answer -> answer.size() > 1);
      // Mixing them offers chips for both and then tells the learner to build a
      // sentence — the README calls this out as a format note that bites.
      assertThat(anySingle && anyPhrase)
          .describedAs("%s mixes a one-word and a multi-word alternative", item.name())
          .isFalse();
    }
  }

  @Test
  void matchPairsSeparatesPairsWithSemicolonsWhenAMeaningHasASpace() {
    for (Item item : everyExercise()) {
      String spec = item.exercise().correctTokens();
      if (!spec.contains("=") || spec.contains(";")) {
        continue;
      }
      // Without ';' the spec is split on whitespace, so "ܐܒܝ=my father" parses as
      // a pair plus a stray word and the exercise becomes unanswerable.
      for (String chunk : spec.trim().split("\\s+")) {
        assertThat(chunk)
            .describedAs("%s needs ';' between pairs — a meaning contains a space", item.name())
            .contains("=");
      }
    }
  }

  @Test
  void promptsThatAskForTheScriptDoNotDisplayIt() {
    for (Item item : everyExercise()) {
      if (!ExerciseType.TRANSLATE_TO_ARAMAIC.name().equals(item.exercise().type())) {
        continue;
      }
      // The script renders above the word bank, so shipping it here is the answer.
      assertThat(item.exercise().aramaicScript())
          .describedAs("%s shows the script it is asking the learner to build", item.name())
          .isNull();
    }
  }

  @Test
  void everyListeningPromptHasSomethingToSay() {
    for (Item item : everyExercise()) {
      ExerciseType type = ExerciseType.valueOf(item.exercise().type());
      if (type != ExerciseType.LISTEN_CHOOSE_MEANING
          && type != ExerciseType.LISTEN_BUILD_ARAMAIC
          && type != ExerciseType.TAP_WHAT_YOU_HEAR) {
        continue;
      }
      // audioText is the transliteration; without it the play button is silent
      // and the exercise cannot be answered at all.
      assertThat(item.exercise().transliteration())
          .describedAs("%s is a listening prompt with no pronunciation text", item.name())
          .isNotBlank();
      assertThat(item.exercise().aramaicScript())
          .describedAs("%s is a listening prompt with no script to fall back on", item.name())
          .isNotBlank();
    }
  }

  /**
   * The unit from which distractors must be *revision*.
   *
   * <p>Unit 1 cannot obey it — at its first exercise nothing has been taught yet,
   * so there is no known vocabulary to draw a wrong option from — and unit 2 was
   * written before the rule was stated, introducing its words as it goes. The
   * README declares the rule from unit 3, and so does this test. Everything the
   * rule cannot cover is still covered by {@link #noDistractorIsInventedOutOfThinAir()}.
   */
  private static final int FIRST_UNIT_HELD_TO_THE_REVISION_RULE = 3;

  @Test
  void noDistractorIsInventedOutOfThinAir() {
    Set<String> everTaught = new LinkedHashSet<>();
    for (Item item : everyExercise()) {
      POLICY
          .bankTokensFromAnswers(item.exercise().correctTokens())
          .forEach(token -> everTaught.add(normalize(token)));
      tokens(item.exercise().aramaicScript()).forEach(token -> everTaught.add(normalize(token)));
    }

    List<String> never = new ArrayList<>();
    for (Item item : everyExercise()) {
      for (String distractor : tokens(item.exercise().distractorTokens())) {
        if (!everTaught.contains(normalize(distractor))) {
          never.add(item.name() + " -> " + distractor);
        }
      }
    }

    // This one holds everywhere, unit 1 included. A distractor the curriculum
    // never teaches at all cannot be eliminated by knowing anything, so the
    // exercise is decided by luck rather than by what the learner has learnt.
    assertThat(never).describedAs("distractors the curriculum never teaches").isEmpty();
  }

  @Test
  void everyDistractorIsVocabularyTheLearnerHasAlreadyMet() {
    Set<String> taught = new LinkedHashSet<>();
    List<String> unmet = new ArrayList<>();

    for (Item item : everyExercise()) {
      if (item.unit().unitNumber() >= FIRST_UNIT_HELD_TO_THE_REVISION_RULE) {
        for (String distractor : tokens(item.exercise().distractorTokens())) {
          if (!taught.contains(normalize(distractor))) {
            unmet.add(item.name() + " -> " + distractor);
          }
        }
      }
      // Taught only *after* checking: a distractor introduced by the exercise it
      // sits in has not been met either.
      POLICY
          .bankTokensFromAnswers(item.exercise().correctTokens())
          .forEach(token -> taught.add(normalize(token)));
      // The script above the word bank teaches too — ܦܬܘܪܐ is met as the prompt
      // of a translate-to-English exercise long before it is ever an answer.
      tokens(item.exercise().aramaicScript()).forEach(token -> taught.add(normalize(token)));
    }

    // A wrong tap should still be revision. A distractor the learner has never
    // seen teaches nothing and is unfair — it can only be eliminated by luck.
    assertThat(unmet).describedAs("distractors never taught anywhere earlier").isEmpty();
  }

  private static List<List<String>> acceptedAnswers(String spec) {
    if (spec == null || spec.isBlank() || spec.contains("=")) {
      return List.of();
    }
    List<List<String>> answers = new ArrayList<>();
    for (String alternative : spec.split("\\|")) {
      List<String> tokens = tokens(alternative);
      if (!tokens.isEmpty()) {
        answers.add(tokens);
      }
    }
    return answers;
  }

  private static List<String> tokens(String spaceSeparated) {
    if (spaceSeparated == null || spaceSeparated.isBlank()) {
      return List.of();
    }
    return Arrays.stream(spaceSeparated.trim().split("\\s+")).toList();
  }

  private static String normalize(String token) {
    return token.trim().toLowerCase(Locale.ROOT);
  }

  private static List<CurriculumFile> load() {
    ObjectMapper mapper = new ObjectMapper();
    List<CurriculumFile> files = new ArrayList<>();
    try {
      Resource[] resources =
          new PathMatchingResourcePatternResolver().getResources("classpath:curriculum/*.json");
      for (Resource resource : resources) {
        try (InputStream in = resource.getInputStream()) {
          files.add(mapper.readValue(in, CurriculumFile.class));
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not read the curriculum", e);
    }
    files.sort(
        Comparator.comparingInt(CurriculumFile::sectionNumber)
            .thenComparingInt(CurriculumFile::unitNumber));
    return files;
  }
}

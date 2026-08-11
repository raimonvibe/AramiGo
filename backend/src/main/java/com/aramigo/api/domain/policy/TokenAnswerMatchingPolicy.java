package com.aramigo.api.domain.policy;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Default Strategy: space-separated tokens, {@code |} between alternate answers.
 *
 * <p>Examples: {@code hello|peace}, {@code I have bread}.
 *
 * <p>Comparison is Unicode-aware so Syriac chips match whether or not the content
 * carries vowel pointing: ܫܠܵܡܵܐ and ܫܠܡܐ are the same answer.
 */
public final class TokenAnswerMatchingPolicy implements AnswerMatchingPolicy {

  /** Syriac combining marks (U+0730-U+074A): vowels, qushshaya/rukkakha, seyame. */
  private static final char SYRIAC_MARKS_START = '\u0730';
  private static final char SYRIAC_MARKS_END = '\u074A';

  @Override
  public boolean matches(String correctTokensSpec, List<String> submitted) {
    List<String> normalized = normalize(submitted);
    return acceptedAnswers(correctTokensSpec).stream()
        .map(this::normalize)
        .anyMatch(normalized::equals);
  }

  @Override
  public String friendlyHint(String correctTokensSpec) {
    List<List<String>> answers = acceptedAnswers(correctTokensSpec);
    List<String> phrases =
        answers.stream().map(answer -> String.join(" ", answer)).toList();

    // One-word synonyms must not look like a phrase to build ("hello or peace"
    // invites tapping both chips). Spell out that only one is needed.
    if (answers.size() > 1 && answers.stream().allMatch(answer -> answer.size() == 1)) {
      return String.join(" or ", phrases) + " (one word)";
    }
    return String.join(" or ", phrases);
  }

  /**
   * Chips for the correct answers, keeping repeats.
   *
   * <p>A sentence that uses the same word twice needs two chips — deduplicating
   * here would make it unbuildable.
   */
  @Override
  public List<String> bankTokensFromAnswers(String correctTokensSpec) {
    Map<String, Integer> mostCopiesNeeded = new LinkedHashMap<>();
    for (List<String> answer : acceptedAnswers(correctTokensSpec)) {
      Map<String, Integer> countsInThisAnswer = new LinkedHashMap<>();
      for (String token : answer) {
        countsInThisAnswer.merge(token, 1, Integer::sum);
      }
      countsInThisAnswer.forEach((token, count) -> mostCopiesNeeded.merge(token, count, Math::max));
    }

    List<String> bank = new ArrayList<>();
    mostCopiesNeeded.forEach(
        (token, copies) -> {
          for (int i = 0; i < copies; i++) {
            bank.add(token);
          }
        });
    return bank;
  }

  @Override
  public String tipFor(String correctTokensSpec) {
    List<List<String>> answers = acceptedAnswers(correctTokensSpec);
    if (answers.size() > 1 && answers.stream().allMatch(answer -> answer.size() == 1)) {
      return "Pick one word — more than one meaning can be right.";
    }
    if (answers.size() == 1 && answers.getFirst().size() == 1) {
      return "Pick one word.";
    }
    if (answers.stream().anyMatch(answer -> answer.size() > 1)) {
      return "Tap words in order to build the sentence.";
    }
    return null;
  }

  @Override
  public boolean isSingleWordPrompt(String correctTokensSpec) {
    List<List<String>> answers = acceptedAnswers(correctTokensSpec);
    return !answers.isEmpty() && answers.stream().allMatch(answer -> answer.size() == 1);
  }

  private List<List<String>> acceptedAnswers(String correctTokensSpec) {
    if (correctTokensSpec == null || correctTokensSpec.isBlank()) {
      return List.of();
    }
    List<List<String>> answers = new ArrayList<>();
    for (String alternative : correctTokensSpec.split("\\|")) {
      List<String> tokens = tokens(alternative);
      if (!tokens.isEmpty()) {
        answers.add(tokens);
      }
    }
    return answers;
  }

  private List<String> tokens(String spaceSeparated) {
    if (spaceSeparated == null || spaceSeparated.isBlank()) {
      return List.of();
    }
    return Arrays.stream(spaceSeparated.trim().split("\\s+")).toList();
  }

  private List<String> normalize(List<String> tokens) {
    return tokens.stream().map(TokenAnswerMatchingPolicy::normalizeToken).toList();
  }

  private static String normalizeToken(String token) {
    String composed = Normalizer.normalize(token.trim(), Normalizer.Form.NFC);
    StringBuilder stripped = new StringBuilder(composed.length());
    for (int i = 0; i < composed.length(); i++) {
      char c = composed.charAt(i);
      if (c < SYRIAC_MARKS_START || c > SYRIAC_MARKS_END) {
        stripped.append(c);
      }
    }
    return stripped.toString().toLowerCase(Locale.ROOT);
  }
}

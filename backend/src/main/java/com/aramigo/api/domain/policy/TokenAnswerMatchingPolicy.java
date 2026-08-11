package com.aramigo.api.domain.policy;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default Strategy: space-separated tokens, {@code |} between alternate answers.
 *
 * <p>Examples: {@code hello|peace}, {@code I have bread}.
 *
 * <p>Match-pair exercises use {@code script=meaning} pairs in the curriculum and
 * submit {@code script|meaning} tokens (order of pairs does not matter).
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
    if (isPairSpec(correctTokensSpec)) {
      return matchesPairs(correctTokensSpec, submitted);
    }
    List<String> normalized = normalize(submitted);
    return acceptedAnswers(correctTokensSpec).stream()
        .map(this::normalize)
        .anyMatch(normalized::equals);
  }

  @Override
  public String friendlyHint(String correctTokensSpec) {
    if (isPairSpec(correctTokensSpec)) {
      return parsePairs(correctTokensSpec).stream()
          .map(pair -> pair.script() + " = " + pair.meaning())
          .collect(Collectors.joining(", "));
    }
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
   *
   * <p>For pair specs, returns every script and meaning as separate chips.
   */
  @Override
  public List<String> bankTokensFromAnswers(String correctTokensSpec) {
    if (isPairSpec(correctTokensSpec)) {
      List<String> bank = new ArrayList<>();
      for (Pair pair : parsePairs(correctTokensSpec)) {
        bank.add(pair.script());
        bank.add(pair.meaning());
      }
      return bank;
    }

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
  public List<String> wordBank(String correctTokensSpec, String distractorTokens) {
    List<String> bank = new ArrayList<>(bankTokensFromAnswers(correctTokensSpec));
    Set<String> present = new HashSet<>();
    for (String token : bank) {
      present.add(normalizeToken(token));
    }
    for (String distractor : tokens(distractorTokens)) {
      if (present.add(normalizeToken(distractor))) {
        bank.add(distractor);
      }
    }
    return bank;
  }

  @Override
  public String tipFor(String correctTokensSpec) {
    if (isPairSpec(correctTokensSpec)) {
      return "Match each script to its meaning.";
    }
    List<List<String>> answers = acceptedAnswers(correctTokensSpec);
    if (answers.size() > 1 && answers.stream().allMatch(answer -> answer.size() == 1)) {
      return "Pick one word — more than one meaning can be right.";
    }
    if (answers.size() == 1 && answers.getFirst().size() == 1) {
      return "Pick one word.";
    }
    if (answers.stream().anyMatch(answer -> answer.size() > 1)) {
      // Chip lanes stay LTR; say so explicitly so RTL readers don't reverse tap order.
      return "Tap words left to right in order to build the sentence.";
    }
    return null;
  }

  @Override
  public boolean isSingleWordPrompt(String correctTokensSpec) {
    if (isPairSpec(correctTokensSpec)) {
      return false;
    }
    List<List<String>> answers = acceptedAnswers(correctTokensSpec);
    return !answers.isEmpty() && answers.stream().allMatch(answer -> answer.size() == 1);
  }

  private boolean matchesPairs(String correctTokensSpec, List<String> submitted) {
    List<Pair> expected = parsePairs(correctTokensSpec);
    if (submitted == null || submitted.size() != expected.size()) {
      return false;
    }
    List<Pair> actual = new ArrayList<>();
    for (String token : submitted) {
      int bar = token.indexOf('|');
      if (bar <= 0 || bar >= token.length() - 1) {
        return false;
      }
      actual.add(new Pair(token.substring(0, bar), token.substring(bar + 1)));
    }
    List<Pair> sortedExpected =
        expected.stream()
            .map(this::normalizePair)
            .sorted(Comparator.comparing(Pair::script).thenComparing(Pair::meaning))
            .toList();
    List<Pair> sortedActual =
        actual.stream()
            .map(this::normalizePair)
            .sorted(Comparator.comparing(Pair::script).thenComparing(Pair::meaning))
            .toList();
    return sortedExpected.equals(sortedActual);
  }

  private static boolean isPairSpec(String correctTokensSpec) {
    return correctTokensSpec != null && correctTokensSpec.contains("=");
  }

  private List<Pair> parsePairs(String correctTokensSpec) {
    List<Pair> pairs = new ArrayList<>();
    if (correctTokensSpec == null || correctTokensSpec.isBlank()) {
      return pairs;
    }
    // Prefer ';' between pairs so meanings can contain spaces: ܐܒܝ=my father;ܐܡܝ=my mother
    String[] chunks =
        correctTokensSpec.contains(";")
            ? correctTokensSpec.trim().split("\\s*;\\s*")
            : correctTokensSpec.trim().split("\\s+");
    for (String chunk : chunks) {
      int eq = chunk.indexOf('=');
      if (eq <= 0 || eq >= chunk.length() - 1) {
        continue;
      }
      pairs.add(new Pair(chunk.substring(0, eq).trim(), chunk.substring(eq + 1).trim()));
    }
    return pairs;
  }

  private Pair normalizePair(Pair pair) {
    return new Pair(normalizeToken(pair.script()), normalizeToken(pair.meaning()));
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

  private record Pair(String script, String meaning) {}
}

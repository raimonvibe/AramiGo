package com.aramigo.api.domain.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Default Strategy: space-separated tokens, {@code |} between alternate answers.
 *
 * <p>Examples: {@code hello|peace}, {@code I have bread}.
 */
public final class TokenAnswerMatchingPolicy implements AnswerMatchingPolicy {

  @Override
  public boolean matches(String correctTokensSpec, List<String> submitted) {
    List<String> normalized = normalize(submitted);
    return acceptedAnswers(correctTokensSpec).stream()
        .map(this::normalize)
        .anyMatch(normalized::equals);
  }

  @Override
  public String friendlyHint(String correctTokensSpec) {
    return String.join(
        " or ",
        acceptedAnswers(correctTokensSpec).stream().map(answer -> String.join(" ", answer)).toList());
  }

  @Override
  public List<String> bankTokensFromAnswers(String correctTokensSpec) {
    Set<String> unique = new LinkedHashSet<>();
    for (List<String> answer : acceptedAnswers(correctTokensSpec)) {
      unique.addAll(answer);
    }
    return new ArrayList<>(unique);
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
    return acceptedAnswers(correctTokensSpec).stream().allMatch(answer -> answer.size() == 1);
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
    return tokens.stream().map(token -> token.trim().toLowerCase(Locale.ROOT)).toList();
  }
}

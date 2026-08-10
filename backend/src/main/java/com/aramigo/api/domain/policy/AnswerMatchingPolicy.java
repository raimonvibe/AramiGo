package com.aramigo.api.domain.policy;

import java.util.List;

/**
 * Strategy for deciding whether a learner's chips match an exercise.
 * Implementations stay in the domain so use cases never hard-code matching rules.
 */
public interface AnswerMatchingPolicy {

  boolean matches(String correctTokensSpec, List<String> submitted);

  String friendlyHint(String correctTokensSpec);

  List<String> bankTokensFromAnswers(String correctTokensSpec);

  String tipFor(String correctTokensSpec);

  boolean isSingleWordPrompt(String correctTokensSpec);
}

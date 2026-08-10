package com.aramigo.api.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenAnswerMatchingPolicyTest {

  private AnswerMatchingPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new TokenAnswerMatchingPolicy();
  }

  @Test
  void acceptsEitherSynonymAlone() {
    assertTrue(policy.matches("hello|peace", List.of("hello")));
    assertTrue(policy.matches("hello|peace", List.of("peace")));
    assertTrue(policy.matches("hello|peace", List.of("HELLO")));
  }

  @Test
  void rejectsBothSynonymsTogether() {
    assertFalse(policy.matches("hello|peace", List.of("hello", "peace")));
  }

  @Test
  void multiWordAnswersKeepOrder() {
    assertTrue(policy.matches("I have bread", List.of("I", "have", "bread")));
    assertFalse(policy.matches("I have bread", List.of("bread", "have", "I")));
  }

  @Test
  void friendlyHintListsAlternatives() {
    assertEquals("hello or peace", policy.friendlyHint("hello|peace"));
  }
}

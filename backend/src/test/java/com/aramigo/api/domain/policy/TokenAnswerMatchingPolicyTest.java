package com.aramigo.api.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenAnswerMatchingPolicyTest {

  /** shlomo ("peace"): shin-lamadh-mim-alaph, in logical order. */
  private static final String SHLOMO = "\u072B\u0720\u0721\u0710";

  /** The same word with vowel pointing, as a pointed manuscript writes it. */
  private static final String SHLOMO_POINTED = "\u072B\u0720\u0735\u0721\u0735\u0710";

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
    assertEquals("hello or peace (one word)", policy.friendlyHint("hello|peace"));
  }

  @Test
  void syriacSentenceMatchesChipOrderNotVisualRtl() {
    // Lesson 4 — "I have bread" in Syriac. Tap order must equal token order.
    String correct = "\u0710\u071D\u072C \u0720\u071D \u0720\u071A\u0721\u0710";
    assertTrue(
        policy.matches(
            correct,
            List.of(
                "\u0710\u071D\u072C",
                "\u0720\u071D",
                "\u0720\u071A\u0721\u0710")));
    assertFalse(
        policy.matches(
            correct,
            List.of(
                "\u0720\u071A\u0721\u0710",
                "\u0720\u071D",
                "\u0710\u071D\u072C")));
    assertEquals(correct, policy.friendlyHint(correct));
  }

  @Test
  void vowelPointingDoesNotChangeTheAnswer() {
    assertTrue(policy.matches(SHLOMO, List.of(SHLOMO_POINTED)));
    assertTrue(policy.matches(SHLOMO_POINTED, List.of(SHLOMO)));
  }

  @Test
  void repeatedWordsGetAChipEach() {
    List<String> bank = policy.bankTokensFromAnswers("li lahma li");

    assertEquals(3, bank.size(), "a sentence using a word twice needs two chips for it");
    assertEquals(2, bank.stream().filter("li"::equals).count());
  }

  @Test
  void alternativesShareChipsRatherThanDoublingThem() {
    List<String> bank = policy.bankTokensFromAnswers("hello|peace");

    assertEquals(List.of("hello", "peace"), bank);
  }

  @Test
  void aSentenceIsBuildableFromItsOwnWordBank() {
    String answer = "li lahma li";
    List<String> bank = policy.bankTokensFromAnswers(answer);

    // The chips are shuffled before display, so what matters is that the bank
    // holds exactly the tokens the answer needs — including the repeat.
    assertEquals(sorted(List.of("li", "lahma", "li")), sorted(bank));
    assertTrue(policy.matches(answer, List.of("li", "lahma", "li")));
  }

  private static List<String> sorted(List<String> tokens) {
    return tokens.stream().sorted().toList();
  }

  @Test
  void blankSpecIsNotASingleWordPrompt() {
    assertFalse(policy.isSingleWordPrompt(""));
    assertFalse(policy.isSingleWordPrompt(null));
  }

  @Test
  void matchPairsIgnoreOrder() {
    String spec = SHLOMO + "=hello \u0720\u071A\u0721\u0710=bread";
    assertTrue(
        policy.matches(
            spec,
            List.of(SHLOMO + "|hello", "\u0720\u071A\u0721\u0710|bread")));
    assertTrue(
        policy.matches(
            spec,
            List.of("\u0720\u071A\u0721\u0710|bread", SHLOMO + "|hello")));
    assertFalse(policy.matches(spec, List.of(SHLOMO + "|bread")));
  }

  @Test
  void matchPairsSupportMultiWordMeanings() {
    String spec = SHLOMO + "=peace upon you;\u0720\u071A\u0721\u0710=fresh bread";
    assertTrue(
        policy.matches(
            spec,
            List.of(SHLOMO + "|peace upon you", "\u0720\u071A\u0721\u0710|fresh bread")));
    assertEquals(
        List.of(SHLOMO, "peace upon you", "\u0720\u071A\u0721\u0710", "fresh bread"),
        policy.bankTokensFromAnswers(spec));
  }
}

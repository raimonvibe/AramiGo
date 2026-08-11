package com.aramigo.api.infrastructure.config;

import java.time.Clock;

import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.application.port.out.ExerciseProgressPort;
import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.application.service.LearningApplicationService;
import com.aramigo.api.domain.policy.AnswerMatchingPolicy;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import com.aramigo.api.infrastructure.auth.GoogleTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: chooses Strategies and wraps use cases with
 * infrastructure concerns without polluting the domain.
 */
@Configuration
public class ApplicationConfig {

  private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

  @Bean
  AnswerMatchingPolicy answerMatchingPolicy() {
    return new TokenAnswerMatchingPolicy();
  }

  /** Injected rather than called statically so energy and streak rules stay testable. */
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  /**
   * Only present once a client id is configured. Without it the app still runs —
   * everyone stays a guest — rather than failing to start.
   */
  @Bean
  GoogleTokenVerifier googleTokenVerifier(
      @Value("${aramigo.auth.google.client-id:}") String clientId) {
    if (clientId == null || clientId.isBlank()) {
      log.info("aramigo.auth.google.client-id is unset — Google sign-in is disabled");
      return null;
    }
    log.info("Google sign-in enabled for client id ending {}", tail(clientId));
    return new GoogleTokenVerifier(clientId);
  }

  @Bean
  LearningUseCases learningUseCases(
      CurriculumRepositoryPort curriculum,
      LearnerRepositoryPort learners,
      ExerciseProgressPort progress,
      AnswerMatchingPolicy answers,
      Clock clock) {
    LearningApplicationService core =
        new LearningApplicationService(curriculum, learners, progress, answers, clock);
    return new TransactionalLearningFacade(core);
  }

  private static String tail(String clientId) {
    return clientId.length() <= 6 ? "***" : "..." + clientId.substring(clientId.length() - 6);
  }
}

package com.aramigo.api.infrastructure.config;

import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.application.service.LearningApplicationService;
import com.aramigo.api.domain.policy.AnswerMatchingPolicy;
import com.aramigo.api.domain.policy.TokenAnswerMatchingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: chooses Strategies and wraps use cases with
 * infrastructure concerns without polluting the domain.
 */
@Configuration
public class ApplicationConfig {

  @Bean
  AnswerMatchingPolicy answerMatchingPolicy() {
    return new TokenAnswerMatchingPolicy();
  }

  @Bean
  LearningUseCases learningUseCases(
      CurriculumRepositoryPort curriculum,
      LearnerRepositoryPort learners,
      AnswerMatchingPolicy answers) {
    LearningApplicationService core =
        new LearningApplicationService(curriculum, learners, answers);
    return new TransactionalLearningFacade(core);
  }
}

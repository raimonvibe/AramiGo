package com.aramigo.api.infrastructure.config;

import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.port.in.LearningUseCases;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator Adapter: applies Spring transactions around pure use cases
 * without putting {@code @Transactional} on the application service itself.
 */
public class TransactionalLearningFacade implements LearningUseCases {

  private final LearningUseCases delegate;

  public TransactionalLearningFacade(LearningUseCases delegate) {
    this.delegate = delegate;
  }

  @Override
  @Transactional(readOnly = true)
  public LearningPathResult getPath(String guestKey) {
    return delegate.getPath(guestKey);
  }

  @Override
  @Transactional
  public LessonSessionResult startLesson(String guestKey, long lessonId) {
    return delegate.startLesson(guestKey, lessonId);
  }

  @Override
  @Transactional
  public CheckAnswerResult checkAnswer(String guestKey, long exerciseId, List<String> tokens) {
    return delegate.checkAnswer(guestKey, exerciseId, tokens);
  }

  @Override
  @Transactional
  public CompleteLessonResult completeLesson(String guestKey, long lessonId) {
    return delegate.completeLesson(guestKey, lessonId);
  }
}

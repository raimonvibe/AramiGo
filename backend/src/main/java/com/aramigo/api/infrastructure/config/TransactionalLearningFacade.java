package com.aramigo.api.infrastructure.config;

import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.dto.ProfileResult;
import com.aramigo.api.application.dto.RefillEnergyResult;
import com.aramigo.api.application.dto.ReviewSessionResult;
import com.aramigo.api.application.port.in.LearningUseCases;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator Adapter: applies Spring transactions around pure use cases
 * without putting {@code @Transactional} on the application service itself.
 *
 * <p>Nothing here is read-only: every entry point may create a learner or credit
 * regenerated energy.
 */
public class TransactionalLearningFacade implements LearningUseCases {

  private final LearningUseCases delegate;

  public TransactionalLearningFacade(LearningUseCases delegate) {
    this.delegate = delegate;
  }

  @Override
  @Transactional
  public LearningPathResult getPath(String identityKey) {
    return delegate.getPath(identityKey);
  }

  @Override
  @Transactional
  public LessonSessionResult startLesson(String identityKey, long lessonId) {
    return delegate.startLesson(identityKey, lessonId);
  }

  @Override
  @Transactional
  public ReviewSessionResult reviewSession(String identityKey) {
    return delegate.reviewSession(identityKey);
  }

  @Override
  @Transactional
  public CheckAnswerResult checkAnswer(String identityKey, long exerciseId, List<String> tokens) {
    return delegate.checkAnswer(identityKey, exerciseId, tokens);
  }

  @Override
  @Transactional
  public CompleteLessonResult completeLesson(
      String identityKey, long lessonId, java.time.ZoneId learnerZone) {
    return delegate.completeLesson(identityKey, lessonId, learnerZone);
  }

  @Override
  @Transactional
  public RefillEnergyResult refillEnergy(String identityKey) {
    return delegate.refillEnergy(identityKey);
  }

  @Override
  @Transactional
  public ProfileResult profile(String identityKey, String displayName) {
    return delegate.profile(identityKey, displayName);
  }

  @Override
  @Transactional
  public ProfileResult linkGuestProgress(
      String accountKey, String displayName, String guestKey) {
    return delegate.linkGuestProgress(accountKey, displayName, guestKey);
  }
}

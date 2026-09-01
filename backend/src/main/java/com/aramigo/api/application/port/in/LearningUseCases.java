package com.aramigo.api.application.port.in;

import java.time.ZoneId;
import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;
import com.aramigo.api.application.dto.ProfileResult;
import com.aramigo.api.application.dto.RefillEnergyResult;
import com.aramigo.api.application.dto.ReviewSessionResult;

/**
 * Inbound ports — what the outside world can ask the learning module to do.
 *
 * <p>{@code identityKey} is already resolved by the inbound adapter: a guest key
 * for anonymous learners, a verified Google subject once they sign in.
 */
public interface LearningUseCases {

  LearningPathResult getPath(String identityKey);

  LessonSessionResult startLesson(String identityKey, long lessonId);

  /** Solved exercises that have come due again, drawn from the whole curriculum. */
  ReviewSessionResult reviewSession(String identityKey);

  CheckAnswerResult checkAnswer(String identityKey, long exerciseId, List<String> tokens);

  /**
   * @param learnerZone where the learner is, so the streak's "today" is theirs.
   *     Counted in UTC, an evening habit rolls over mid-afternoon for much of the
   *     world and breaks a streak that was actually kept.
   */
  CompleteLessonResult completeLesson(String identityKey, long lessonId, ZoneId learnerZone);

  /** Trades gems for a full energy bar — the only thing gems are good for. */
  RefillEnergyResult refillEnergy(String identityKey);

  ProfileResult profile(String identityKey, String displayName);

  /** Folds anonymous progress into a signed-in account. Idempotent. */
  ProfileResult linkGuestProgress(String accountKey, String displayName, String guestKey);
}

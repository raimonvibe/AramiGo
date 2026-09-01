package com.aramigo.api.application.port.out;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Records which exercises a learner has actually answered correctly.
 *
 * <p>Without this, "complete this lesson" is just a claim the client makes.
 */
public interface ExerciseProgressPort {

  /** Records a correct answer and moves it up the review ladder. */
  void recordCorrect(long learnerId, long exerciseId, Instant now);

  /**
   * Records a wrong answer on an exercise already solved once.
   *
   * <p>Only the review schedule moves — the row itself stays, so a slip during
   * review can never un-complete a lesson the learner already finished.
   * A no-op when the exercise has not been solved before.
   */
  void recordLapse(long learnerId, long exerciseId, Instant now);

  /** Solved exercises whose next sighting has come due, soonest first. */
  List<Long> findDueForReview(long learnerId, Instant now, int limit);

  /** How many solved exercises are waiting to be reviewed. */
  int countDueForReview(long learnerId, Instant now);

  Set<Long> solvedAmong(long learnerId, Collection<Long> exerciseIds);

  /** Moves an anonymous learner's solved exercises onto their new account. */
  void transferAll(long fromLearnerId, long toLearnerId);

  void deleteAllFor(long learnerId);
}

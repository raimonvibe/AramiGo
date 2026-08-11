package com.aramigo.api.application.port.out;

import java.util.Collection;
import java.util.Set;

/**
 * Records which exercises a learner has actually answered correctly.
 *
 * <p>Without this, "complete this lesson" is just a claim the client makes.
 */
public interface ExerciseProgressPort {

  void recordSolved(long learnerId, long exerciseId);

  Set<Long> solvedAmong(long learnerId, Collection<Long> exerciseIds);

  /** Moves an anonymous learner's solved exercises onto their new account. */
  void transferAll(long fromLearnerId, long toLearnerId);

  void deleteAllFor(long learnerId);
}

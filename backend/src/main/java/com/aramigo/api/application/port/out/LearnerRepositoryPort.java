package com.aramigo.api.application.port.out;

import java.time.Instant;
import java.util.Optional;

import com.aramigo.api.domain.model.Learner;

public interface LearnerRepositoryPort {

  Optional<Learner> findByIdentityKey(String identityKey);

  /**
   * Returns the learner for this identity, creating one if needed.
   *
   * <p>Implementations must tolerate a concurrent insert of the same key — two
   * parallel first requests are normal, not an error.
   */
  Learner findOrCreate(String identityKey, Instant now);

  Learner save(Learner learner);

  void delete(Learner learner);
}

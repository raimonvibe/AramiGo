package com.aramigo.api.application.port.out;

import java.util.Optional;

import com.aramigo.api.domain.model.Learner;

public interface LearnerRepositoryPort {

  Optional<Learner> findByGuestKey(String guestKey);

  Learner save(Learner learner);
}

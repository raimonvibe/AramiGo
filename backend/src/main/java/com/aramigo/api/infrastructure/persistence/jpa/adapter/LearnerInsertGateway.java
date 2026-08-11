package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.time.Instant;

import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LearnerJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.mapper.DomainMapper;
import com.aramigo.api.infrastructure.persistence.jpa.spring.LearnerJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts a brand-new learner in its own transaction.
 *
 * <p>Separate bean on purpose: a losing race on the identity key throws at flush
 * time and poisons whatever transaction it ran in. Isolating the insert means the
 * caller can catch that, re-read the winner, and carry on. Calling a
 * {@code REQUIRES_NEW} method on {@code this} would skip the proxy and defeat the
 * whole arrangement.
 */
@Component
public class LearnerInsertGateway {

  private final LearnerJpaRepository jpa;

  public LearnerInsertGateway(LearnerJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Learner insert(String identityKey, Instant now) {
    LearnerJpaEntity entity = new LearnerJpaEntity(identityKey);
    DomainMapper.copyToEntity(new Learner(identityKey, now), entity);
    return DomainMapper.toDomain(jpa.saveAndFlush(entity));
  }
}

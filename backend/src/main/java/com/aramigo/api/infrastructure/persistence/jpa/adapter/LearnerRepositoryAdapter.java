package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.time.Instant;
import java.util.Optional;

import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LearnerJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.mapper.DomainMapper;
import com.aramigo.api.infrastructure.persistence.jpa.spring.LearnerJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class LearnerRepositoryAdapter implements LearnerRepositoryPort {

  private final LearnerJpaRepository jpa;
  private final LearnerInsertGateway inserts;

  public LearnerRepositoryAdapter(LearnerJpaRepository jpa, LearnerInsertGateway inserts) {
    this.jpa = jpa;
    this.inserts = inserts;
  }

  @Override
  public Optional<Learner> findByIdentityKey(String identityKey) {
    return jpa.findByIdentityKey(identityKey).map(DomainMapper::toDomain);
  }

  /**
   * A first-time visitor usually fires several requests at once, so two threads
   * racing to create the same learner is the normal case. Let the unique index
   * pick a winner and read back whatever it chose instead of failing the request.
   */
  @Override
  public Learner findOrCreate(String identityKey, Instant now) {
    Optional<Learner> existing = findByIdentityKey(identityKey);
    if (existing.isPresent()) {
      return existing.get();
    }

    try {
      return inserts.insert(identityKey, now);
    } catch (DataIntegrityViolationException raced) {
      return findByIdentityKey(identityKey)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Learner " + identityKey + " vanished after a concurrent insert", raced));
    }
  }

  @Override
  public Learner save(Learner learner) {
    LearnerJpaEntity entity =
        learner.getId() == null
            ? new LearnerJpaEntity(learner.getIdentityKey())
            : jpa.findById(learner.getId())
                .orElseGet(() -> new LearnerJpaEntity(learner.getIdentityKey()));
    DomainMapper.copyToEntity(learner, entity);
    return DomainMapper.toDomain(jpa.save(entity));
  }

  @Override
  public void delete(Learner learner) {
    if (learner.getId() != null) {
      jpa.deleteById(learner.getId());
    }
  }
}

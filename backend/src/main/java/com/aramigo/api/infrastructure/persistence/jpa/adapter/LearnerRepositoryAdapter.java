package com.aramigo.api.infrastructure.persistence.jpa.adapter;

import java.util.Optional;

import com.aramigo.api.application.port.out.LearnerRepositoryPort;
import com.aramigo.api.domain.model.Learner;
import com.aramigo.api.infrastructure.persistence.jpa.entity.LearnerJpaEntity;
import com.aramigo.api.infrastructure.persistence.jpa.mapper.DomainMapper;
import com.aramigo.api.infrastructure.persistence.jpa.spring.LearnerJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class LearnerRepositoryAdapter implements LearnerRepositoryPort {

  private final LearnerJpaRepository jpa;

  public LearnerRepositoryAdapter(LearnerJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Learner> findByGuestKey(String guestKey) {
    return jpa.findByGuestKey(guestKey).map(DomainMapper::toDomain);
  }

  @Override
  public Learner save(Learner learner) {
    LearnerJpaEntity entity =
        learner.getId() == null
            ? new LearnerJpaEntity(learner.getGuestKey())
            : jpa.findById(learner.getId()).orElseGet(() -> new LearnerJpaEntity(learner.getGuestKey()));
    DomainMapper.copyToEntity(learner, entity);
    LearnerJpaEntity saved = jpa.save(entity);
    return DomainMapper.toDomain(saved);
  }
}

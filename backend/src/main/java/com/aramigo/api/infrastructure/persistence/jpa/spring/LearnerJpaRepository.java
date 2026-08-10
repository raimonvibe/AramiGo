package com.aramigo.api.infrastructure.persistence.jpa.spring;

import java.util.Optional;

import com.aramigo.api.infrastructure.persistence.jpa.entity.LearnerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnerJpaRepository extends JpaRepository<LearnerJpaEntity, Long> {
  Optional<LearnerJpaEntity> findByGuestKey(String guestKey);
}

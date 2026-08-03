package com.resumebuilder.user.adapter.out.persistence;

import com.resumebuilder.user.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByEmail(String email);
}

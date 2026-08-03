package com.resumebuilder.resume.adapter.out.persistence;

import com.resumebuilder.resume.adapter.out.persistence.entity.ResumeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeJpaRepository extends JpaRepository<ResumeJpaEntity, UUID> {
    Optional<ResumeJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<ResumeJpaEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}

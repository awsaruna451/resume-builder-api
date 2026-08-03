package com.resumebuilder.resume.application.port.out;

import com.resumebuilder.resume.domain.Resume;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumePort {
    Resume save(Resume resume);
    Optional<Resume> findById(UUID id);
    Optional<Resume> findByIdAndUserId(UUID id, UUID userId);
    List<Resume> findAllByUserId(UUID userId);
    void deleteById(UUID id);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}

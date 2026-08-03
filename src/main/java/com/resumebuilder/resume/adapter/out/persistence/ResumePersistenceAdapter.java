package com.resumebuilder.resume.adapter.out.persistence;

import com.resumebuilder.resume.adapter.out.persistence.entity.ResumeJpaEntity;
import com.resumebuilder.resume.application.port.out.ResumePort;
import com.resumebuilder.resume.domain.Resume;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ResumePersistenceAdapter implements ResumePort {

    private final ResumeJpaRepository resumeJpaRepository;

    @Override
    public Resume save(Resume resume) {
        return toDomain(resumeJpaRepository.save(toEntity(resume)));
    }

    @Override
    public Optional<Resume> findById(UUID id) {
        return resumeJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Resume> findByIdAndUserId(UUID id, UUID userId) {
        return resumeJpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<Resume> findAllByUserId(UUID userId) {
        return resumeJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        resumeJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return resumeJpaRepository.existsByIdAndUserId(id, userId);
    }

    private Resume toDomain(ResumeJpaEntity e) {
        return Resume.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .title(e.getTitle())
                .templateId(e.getTemplateId())
                .resumeData(e.getResumeData())
                .filePath(e.getFilePath())
                .fileName(e.getFileName())
                .fileSize(e.getFileSize())
                .mimeType(e.getMimeType())
                .isPublic(e.isPublic())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private ResumeJpaEntity toEntity(Resume r) {
        return ResumeJpaEntity.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .title(r.getTitle())
                .templateId(r.getTemplateId())
                .resumeData(r.getResumeData())
                .filePath(r.getFilePath())
                .fileName(r.getFileName())
                .fileSize(r.getFileSize())
                .mimeType(r.getMimeType())
                .isPublic(r.isPublic())
                .build();
    }
}

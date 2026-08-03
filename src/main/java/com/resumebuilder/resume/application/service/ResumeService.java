package com.resumebuilder.resume.application.service;

import com.resumebuilder.common.exception.NotFoundException;
import com.resumebuilder.resume.application.port.in.ResumeUseCase;
import com.resumebuilder.resume.application.port.out.ResumePort;
import com.resumebuilder.resume.domain.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService implements ResumeUseCase {

    private final ResumePort resumePort;

    @Override
    @Transactional
    public Resume createResume(CreateResumeCommand command) {
        Resume resume = Resume.builder()
                .id(UUID.randomUUID())
                .userId(command.userId())
                .title(command.title())
                .templateId(command.templateId() != null ? command.templateId() : "default")
                .resumeData(command.resumeData())
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Resume saved = resumePort.save(resume);
        log.info("Resume created: {} for user: {}", saved.getId(), command.userId());
        return saved;
    }

    @Override
    @Transactional
    public Resume updateResume(UpdateResumeCommand command) {
        Resume existing = findOwnedResume(command.resumeId(), command.userId());
        Resume updated  = existing.updateContent(
                command.title(), command.templateId(), command.resumeData());
        return resumePort.save(updated);
    }

    @Override
    @Transactional
    public Resume updateResumeData(UpdateResumeDataCommand command) {
        Resume existing = findOwnedResume(command.resumeId(), command.userId());
        Resume updated  = existing.withResumeData(command.resumeData())
                                  .withUpdatedAt(LocalDateTime.now());
        return resumePort.save(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Resume getResume(UUID resumeId, UUID userId) {
        return findOwnedResume(resumeId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resume> getUserResumes(UUID userId) {
        return resumePort.findAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteResume(UUID resumeId, UUID userId) {
        findOwnedResume(resumeId, userId); // ownership check
        resumePort.deleteById(resumeId);
        log.info("Resume deleted: {} by user: {}", resumeId, userId);
    }

    private Resume findOwnedResume(UUID resumeId, UUID userId) {
        return resumePort.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Resume not found: " + resumeId));
    }
}

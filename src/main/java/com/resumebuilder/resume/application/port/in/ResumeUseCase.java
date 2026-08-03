package com.resumebuilder.resume.application.port.in;

import com.resumebuilder.resume.domain.Resume;
import com.resumebuilder.resume.domain.model.ResumeData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface ResumeUseCase {

    Resume createResume(@Valid CreateResumeCommand command);

    Resume updateResume(@Valid UpdateResumeCommand command);

    Resume updateResumeData(@Valid UpdateResumeDataCommand command);

    Resume getResume(UUID resumeId, UUID userId);

    List<Resume> getUserResumes(UUID userId);

    void deleteResume(UUID resumeId, UUID userId);

    record CreateResumeCommand(
        @NotNull UUID userId,
        @NotBlank String title,
        String templateId,
        ResumeData resumeData
    ) {}

    record UpdateResumeCommand(
        @NotNull UUID resumeId,
        @NotNull UUID userId,
        @NotBlank String title,
        String templateId,
        ResumeData resumeData
    ) {}

    record UpdateResumeDataCommand(
        @NotNull UUID resumeId,
        @NotNull UUID userId,
        @NotNull ResumeData resumeData
    ) {}
}

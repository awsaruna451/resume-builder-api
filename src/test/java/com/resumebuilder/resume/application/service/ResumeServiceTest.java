package com.resumebuilder.resume.application.service;

import com.resumebuilder.common.exception.NotFoundException;
import com.resumebuilder.resume.application.port.in.ResumeUseCase;
import com.resumebuilder.resume.application.port.out.ResumePort;
import com.resumebuilder.resume.domain.Resume;
import com.resumebuilder.resume.domain.model.ResumeData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock ResumePort resumePort;
    @InjectMocks ResumeService resumeService;

    private Resume buildResume(UUID id, UUID userId) {
        return Resume.builder()
                .id(id)
                .userId(userId)
                .title("My CV")
                .templateId("default")
                .resumeData(ResumeData.builder().build())
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createResume: saves and returns resume with generated ID")
    void createResume_success() {
        UUID userId = UUID.randomUUID();
        when(resumePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Resume result = resumeService.createResume(
                new ResumeUseCase.CreateResumeCommand(userId, "My CV", "modern", null));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo("My CV");
        assertThat(result.getTemplateId()).isEqualTo("modern");
    }

    @Test
    @DisplayName("createResume: uses default template when templateId is null")
    void createResume_defaultTemplate() {
        when(resumePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Resume result = resumeService.createResume(
                new ResumeUseCase.CreateResumeCommand(UUID.randomUUID(), "CV", null, null));

        assertThat(result.getTemplateId()).isEqualTo("default");
    }

    @Test
    @DisplayName("updateResume: updates title and data, saves")
    void updateResume_success() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Resume existing = buildResume(id, userId);

        when(resumePort.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));
        when(resumePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Resume result = resumeService.updateResume(
                new ResumeUseCase.UpdateResumeCommand(id, userId, "Updated CV", "classic", null));

        assertThat(result.getTitle()).isEqualTo("Updated CV");
        assertThat(result.getTemplateId()).isEqualTo("classic");
    }

    @Test
    @DisplayName("updateResume: throws NotFoundException when resume not owned by user")
    void updateResume_notFound_throws() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(resumePort.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.updateResume(
                new ResumeUseCase.UpdateResumeCommand(id, userId, "Title", null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getUserResumes: returns all resumes for user")
    void getUserResumes_returnsList() {
        UUID userId = UUID.randomUUID();
        List<Resume> resumes = List.of(
                buildResume(UUID.randomUUID(), userId),
                buildResume(UUID.randomUUID(), userId));

        when(resumePort.findAllByUserId(userId)).thenReturn(resumes);

        List<Resume> result = resumeService.getUserResumes(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteResume: deletes when user owns resume")
    void deleteResume_success() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Resume existing = buildResume(id, userId);

        when(resumePort.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));

        assertThatNoException().isThrownBy(
                () -> resumeService.deleteResume(id, userId));

        verify(resumePort).deleteById(id);
    }
}

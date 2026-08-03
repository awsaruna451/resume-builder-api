package com.resumebuilder.resume.adapter.in.web;

import com.resumebuilder.common.response.ApiResponse;
import com.resumebuilder.common.security.CurrentUser;
import com.resumebuilder.file.application.port.in.FileUploadUseCase;
import com.resumebuilder.resume.adapter.in.web.dto.ResumeDtos;
import com.resumebuilder.resume.application.port.in.ResumeUseCase;
import com.resumebuilder.resume.domain.Resume;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Resumes", description = "Create, update and manage resumes")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeUseCase resumeUseCase;
    private final FileUploadUseCase fileUploadUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new resume")
    public ApiResponse<ResumeDtos.ResumeResponse> createResume(
            @CurrentUser UUID userId,
            @Valid @RequestBody ResumeDtos.CreateResumeRequest request) {

        Resume resume = resumeUseCase.createResume(new ResumeUseCase.CreateResumeCommand(
                userId,
                request.title(),
                request.templateId(),
                ResumeDtos.toDomain(request.resumeData())));

        return ApiResponse.success(ResumeDtos.ResumeResponse.from(resume));
    }

    @PutMapping("/{resumeId}")
    @Operation(summary = "Update resume title, template and data")
    public ApiResponse<ResumeDtos.ResumeResponse> updateResume(
            @CurrentUser UUID userId,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeDtos.UpdateResumeRequest request) {

        Resume resume = resumeUseCase.updateResume(new ResumeUseCase.UpdateResumeCommand(
                resumeId,
                userId,
                request.title(),
                request.templateId(),
                ResumeDtos.toDomain(request.resumeData())));

        return ApiResponse.success(ResumeDtos.ResumeResponse.from(resume));
    }

    @PatchMapping("/{resumeId}/data")
    @Operation(summary = "Update only the resume JSON data")
    public ApiResponse<ResumeDtos.ResumeResponse> updateResumeData(
            @CurrentUser UUID userId,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeDtos.UpdateResumeDataRequest request) {

        Resume resume = resumeUseCase.updateResumeData(
                new ResumeUseCase.UpdateResumeDataCommand(
                        resumeId, userId, ResumeDtos.toDomain(request.resumeData())));

        return ApiResponse.success(ResumeDtos.ResumeResponse.from(resume));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get a resume by ID")
    public ApiResponse<ResumeDtos.ResumeResponse> getResume(
            @CurrentUser UUID userId,
            @PathVariable UUID resumeId) {

        return ApiResponse.success(ResumeDtos.ResumeResponse.from(
                resumeUseCase.getResume(resumeId, userId)));
    }

    @GetMapping
    @Operation(summary = "Get all resumes for the current user")
    public ApiResponse<List<ResumeDtos.ResumeResponse>> getUserResumes(
            @CurrentUser UUID userId) {

        List<ResumeDtos.ResumeResponse> resumes = resumeUseCase.getUserResumes(userId)
                .stream().map(ResumeDtos.ResumeResponse::from).toList();

        return ApiResponse.success(resumes);
    }

    @DeleteMapping("/{resumeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a resume")
    public void deleteResume(
            @CurrentUser UUID userId,
            @PathVariable UUID resumeId) {
        resumeUseCase.deleteResume(resumeId, userId);
    }

    @PostMapping(value = "/{resumeId}/upload",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an existing CV file (PDF or Word)")
    public ApiResponse<ResumeDtos.ResumeResponse> uploadCvFile(
            @CurrentUser UUID userId,
            @PathVariable UUID resumeId,
            @RequestPart("file") MultipartFile file) throws IOException {

        fileUploadUseCase.uploadResumeFile(new FileUploadUseCase.UploadCommand(
                userId,
                resumeId,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()));

        return ApiResponse.success(ResumeDtos.ResumeResponse.from(
                resumeUseCase.getResume(resumeId, userId)));
    }
}

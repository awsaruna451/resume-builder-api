package com.resumebuilder.file.application.service;

import com.resumebuilder.common.exception.BadRequestException;
import com.resumebuilder.file.application.port.in.FileUploadUseCase;
import com.resumebuilder.file.application.port.out.FileStoragePort;
import com.resumebuilder.resume.application.port.out.ResumePort;
import com.resumebuilder.resume.domain.Resume;
import com.resumebuilder.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService implements FileUploadUseCase {

    private final FileStoragePort fileStoragePort;
    private final ResumePort resumePort;

    @Value("${app.file.allowed-types}")
    private String allowedTypes;

    @Value("${app.file.max-size-bytes}")
    private long maxSizeBytes;

    @Override
    @Transactional
    public FileUploadResult uploadResumeFile(UploadCommand command) {
        validateFile(command.contentType(), command.fileSize());

        String path = buildStoragePath(command.userId(), command.resumeId(),
                command.originalFileName());

        String storedPath = fileStoragePort.store(
                command.inputStream(), path, command.contentType());

        Resume resume = resumePort.findByIdAndUserId(command.resumeId(), command.userId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));

        Resume updated = resume.attachFile(
                storedPath,
                command.originalFileName(),
                command.fileSize(),
                command.contentType());

        resumePort.save(updated);

        log.info("File uploaded for resume {} by user {}: {}",
                command.resumeId(), command.userId(), storedPath);

        return new FileUploadResult(
                storedPath,
                command.originalFileName(),
                command.fileSize(),
                command.contentType());
    }

    @Override
    public void deleteFile(String filePath) {
        if (fileStoragePort.exists(filePath)) {
            fileStoragePort.delete(filePath);
            log.info("File deleted: {}", filePath);
        }
    }

    private void validateFile(String contentType, long fileSize) {
        List<String> allowed = List.of(allowedTypes.split(","));
        if (!allowed.contains(contentType)) {
            throw new BadRequestException(
                    "Unsupported file type: " + contentType +
                    ". Allowed: " + allowedTypes);
        }
        if (fileSize > maxSizeBytes) {
            throw new BadRequestException(
                    "File too large. Max size: " + (maxSizeBytes / 1024 / 1024) + "MB");
        }
    }

    private String buildStoragePath(UUID userId, UUID resumeId, String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("resumes/%s/%s/%s", userId, resumeId, sanitized);
    }
}

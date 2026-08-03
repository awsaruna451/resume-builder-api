package com.resumebuilder.file.application.port.in;

import jakarta.validation.constraints.NotNull;

import java.io.InputStream;
import java.util.UUID;

public interface FileUploadUseCase {

    FileUploadResult uploadResumeFile(UploadCommand command);

    void deleteFile(String filePath);

    record UploadCommand(
        @NotNull UUID userId,
        @NotNull UUID resumeId,
        @NotNull InputStream inputStream,
        @NotNull String originalFileName,
        @NotNull String contentType,
        long fileSize
    ) {}

    record FileUploadResult(
        String filePath,
        String fileName,
        long fileSize,
        String contentType
    ) {}
}

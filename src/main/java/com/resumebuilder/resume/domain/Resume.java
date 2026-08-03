package com.resumebuilder.resume.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
@With
public class Resume {
    UUID id;
    UUID userId;
    String title;
    String templateId;
    com.resumebuilder.resume.domain.model.ResumeData resumeData;
    String filePath;
    String fileName;
    Long fileSize;
    String mimeType;
    boolean isPublic;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public Resume updateContent(String title, String templateId,
                                com.resumebuilder.resume.domain.model.ResumeData data) {
        return this.withTitle(title)
                   .withTemplateId(templateId)
                   .withResumeData(data)
                   .withUpdatedAt(LocalDateTime.now());
    }

    public Resume attachFile(String filePath, String fileName, Long fileSize, String mimeType) {
        return this.withFilePath(filePath)
                   .withFileName(fileName)
                   .withFileSize(fileSize)
                   .withMimeType(mimeType)
                   .withUpdatedAt(LocalDateTime.now());
    }
}

package com.resumebuilder.file.adapter.out.storage;

import com.resumebuilder.common.exception.InternalServerException;
import com.resumebuilder.file.application.port.out.FileStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

@Slf4j
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path baseDir;

    public LocalFileStorageAdapter(@Value("${app.file.upload-dir}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new InternalServerException("Cannot create upload directory: " + uploadDir, e);
        }
    }

    @Override
    public String store(InputStream inputStream, String path, String contentType) {
        Path target = resolveSecurePath(path);
        try {
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                inputStream.transferTo(out);
            }
            log.debug("File stored at: {}", target);
            return path;
        } catch (IOException e) {
            throw new InternalServerException("Failed to store file: " + path, e);
        }
    }

    @Override
    public InputStream retrieve(String path) {
        Path target = resolveSecurePath(path);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new InternalServerException("Failed to read file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        Path target = resolveSecurePath(path);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(resolveSecurePath(path));
    }

    private Path resolveSecurePath(String relativePath) {
        Path resolved = baseDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt: " + relativePath);
        }
        return resolved;
    }
}

package com.resumebuilder.file.application.port.out;

import java.io.InputStream;

public interface FileStoragePort {
    String store(InputStream inputStream, String path, String contentType);
    InputStream retrieve(String path);
    void delete(String path);
    boolean exists(String path);
}

package com.resumebuilder.auth.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {
    void save(UUID userId, String token, LocalDateTime expiresAt);
    Optional<RefreshTokenData> findByToken(String token);
    void revokeByToken(String token);
    void revokeAllByUserId(UUID userId);

    record RefreshTokenData(
        UUID userId,
        String token,
        LocalDateTime expiresAt,
        boolean revoked
    ) {}
}

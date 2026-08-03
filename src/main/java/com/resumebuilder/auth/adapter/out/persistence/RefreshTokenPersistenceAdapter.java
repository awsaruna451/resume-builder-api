package com.resumebuilder.auth.adapter.out.persistence;

import com.resumebuilder.auth.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import com.resumebuilder.auth.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public void save(UUID userId, String token, LocalDateTime expiresAt) {
        refreshTokenJpaRepository.save(RefreshTokenJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .revoked(false)
                .build());
    }

    @Override
    public Optional<RefreshTokenData> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .map(e -> new RefreshTokenData(
                        e.getUserId(), e.getToken(), e.getExpiresAt(), e.isRevoked()));
    }

    @Override
    public void revokeByToken(String token) {
        refreshTokenJpaRepository.revokeByToken(token);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        refreshTokenJpaRepository.revokeAllByUserId(userId);
    }
}

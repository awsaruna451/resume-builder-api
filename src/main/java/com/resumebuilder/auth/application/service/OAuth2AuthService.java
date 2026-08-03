package com.resumebuilder.auth.application.service;

import com.resumebuilder.auth.application.port.in.OAuth2LoginUseCase;
import com.resumebuilder.auth.application.port.in.TokenUseCase;
import com.resumebuilder.auth.application.port.out.AuthUserPort;
import com.resumebuilder.auth.application.port.out.RefreshTokenPort;
import com.resumebuilder.common.exception.UnauthorizedException;
import com.resumebuilder.common.security.JwtTokenProvider;
import com.resumebuilder.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AuthService implements OAuth2LoginUseCase, TokenUseCase {

    private final AuthUserPort     authUserPort;
    private final RefreshTokenPort refreshTokenPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ── OAuth2 Login ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OAuth2LoginUseCase.TokenResult processOAuth2Login(OAuth2UserInfo info) {
        User user = authUserPort
                .findByProviderAndProviderId(info.provider(), info.providerId())
                .map(existing -> updateExistingUser(existing, info))
                .orElseGet(() -> registerNewUser(info));

        String access  = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = generateAndSaveRefresh(user.getId());

        log.info("OAuth2 login success: provider={} email={}", info.provider(), info.email());

        return new OAuth2LoginUseCase.TokenResult(
                access, refresh,
                jwtTokenProvider.getExpirationMs() / 1000,
                toUserInfo(user));
    }

    // ── Token Refresh ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TokenUseCase.TokenResult refresh(String refreshToken) {
        RefreshTokenPort.RefreshTokenData data = refreshTokenPort
                .findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (data.revoked() || data.expiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = authUserPort.findById(data.userId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        refreshTokenPort.revokeByToken(refreshToken);

        String newAccess  = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefresh = generateAndSaveRefresh(user.getId());

        return new TokenUseCase.TokenResult(newAccess, newRefresh, jwtTokenProvider.getExpirationMs() / 1000);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenPort.revokeByToken(refreshToken);
        log.debug("Refresh token revoked");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User registerNewUser(OAuth2UserInfo info) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(info.email())
                .firstName(info.firstName())
                .lastName(info.lastName())
                .pictureUrl(info.pictureUrl())
                .provider(info.provider())
                .providerId(info.providerId())
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        log.info("Registering new OAuth2 user: {}", info.email());
        return authUserPort.save(user);
    }

    private User updateExistingUser(User existing, OAuth2UserInfo info) {
        // Sync profile picture and name from Google on every login
        User updated = existing
                .withFirstName(info.firstName())
                .withLastName(info.lastName())
                .withPictureUrl(info.pictureUrl())
                .withUpdatedAt(LocalDateTime.now());
        return authUserPort.save(updated);
    }

    private String generateAndSaveRefresh(UUID userId) {
        String token   = UUID.randomUUID().toString();
        LocalDateTime exp = LocalDateTime.now()
                .plusNanos(refreshExpirationMs * 1_000_000L);
        refreshTokenPort.save(userId, token, exp);
        return token;
    }

    private OAuth2LoginUseCase.UserInfo toUserInfo(User u) {
        return new OAuth2LoginUseCase.UserInfo(
                u.getId().toString(), u.getEmail(),
                u.getFirstName(), u.getLastName(), u.getPictureUrl());
    }
}

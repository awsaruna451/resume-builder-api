package com.resumebuilder.auth.application.service;

import com.resumebuilder.auth.application.port.in.OAuth2LoginUseCase;
import com.resumebuilder.auth.application.port.out.AuthUserPort;
import com.resumebuilder.auth.application.port.out.RefreshTokenPort;
import com.resumebuilder.common.security.JwtTokenProvider;
import com.resumebuilder.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthServiceTest {

    @Mock AuthUserPort     authUserPort;
    @Mock RefreshTokenPort refreshTokenPort;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks OAuth2AuthService service;

    private final OAuth2LoginUseCase.OAuth2UserInfo googleUserInfo =
            new OAuth2LoginUseCase.OAuth2UserInfo(
                    "google-sub-123", "john@gmail.com",
                    "John", "Doe", "https://photo.url",
                    User.AuthProvider.GOOGLE);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("processOAuth2Login: registers new user when not found")
    void processOAuth2Login_newUser_registersAndReturnsTokens() {
        when(authUserPort.findByProviderAndProviderId(User.AuthProvider.GOOGLE, "google-sub-123"))
                .thenReturn(Optional.empty());
        when(authUserPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any(), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        OAuth2LoginUseCase.TokenResult result = service.processOAuth2Login(googleUserInfo);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.user().email()).isEqualTo("john@gmail.com");
        verify(authUserPort).save(argThat(u ->
                u.getEmail().equals("john@gmail.com") &&
                u.getProvider() == User.AuthProvider.GOOGLE &&
                u.getProviderId().equals("google-sub-123")));
    }

    @Test
    @DisplayName("processOAuth2Login: updates existing user profile on re-login")
    void processOAuth2Login_existingUser_updatesProfile() {
        User existing = User.builder()
                .id(UUID.randomUUID())
                .email("john@gmail.com")
                .firstName("Johnny")      // old name
                .lastName("Doe")
                .pictureUrl("https://old-photo.url")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("google-sub-123")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();

        when(authUserPort.findByProviderAndProviderId(User.AuthProvider.GOOGLE, "google-sub-123"))
                .thenReturn(Optional.of(existing));
        when(authUserPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        service.processOAuth2Login(googleUserInfo);

        // Should save with updated name and picture from Google
        verify(authUserPort).save(argThat(u ->
                u.getFirstName().equals("John") &&
                u.getPictureUrl().equals("https://photo.url")));
    }

    @Test
    @DisplayName("processOAuth2Login: sets role to USER for new users")
    void processOAuth2Login_newUser_roleIsUser() {
        when(authUserPort.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(authUserPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("t");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        service.processOAuth2Login(googleUserInfo);

        verify(authUserPort).save(argThat(u -> u.getRole() == User.Role.USER));
    }

    @Test
    @DisplayName("logout: revokes refresh token")
    void logout_revokesToken() {
        service.logout("some-refresh-token");
        verify(refreshTokenPort).revokeByToken("some-refresh-token");
    }
}

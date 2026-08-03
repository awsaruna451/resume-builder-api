package com.resumebuilder.auth.adapter.in.web.handler;

import com.resumebuilder.auth.application.port.in.OAuth2LoginUseCase;
import com.resumebuilder.auth.application.port.out.AuthUserPort;
import com.resumebuilder.common.exception.BadRequestException;
import com.resumebuilder.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock OAuth2LoginUseCase oauth2LoginUseCase;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock RedirectStrategy    redirectStrategy;  // mock the redirect strategy
    @Mock AuthUserPort authUserPort;
    @Mock JwtTokenProvider jwtTokenProvider;

    // Use @Spy so we can inject the mock RedirectStrategy while keeping real logic
    @Spy
    OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(oauth2LoginUseCase);

    private OAuth2AuthenticationToken buildGoogleAuth(Map<String, Object> attrs) {
        OAuth2User oauthUser = new DefaultOAuth2User(List.of(), attrs, "sub");
        return new OAuth2AuthenticationToken(oauthUser, List.of(), "google");
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "authorizedRedirectUris",
                "http://localhost:3000/oauth2/redirect,http://localhost:5173/oauth2/redirect");
        // Inject the mock RedirectStrategy so sendRedirect is captured, not executed for real
        handler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("onAuthenticationSuccess: redirects to frontend with token and refresh params")
    void onAuthenticationSuccess_redirectsWithTokens() throws IOException {
        OAuth2AuthenticationToken auth = buildGoogleAuth(Map.of(
                "sub",         "g-123",
                "email",       "user@gmail.com",
                "given_name",  "Jane",
                "family_name", "Doe",
                "picture",     "https://pic.url"));

        when(request.getParameter("redirect_uri"))
                .thenReturn("http://localhost:3000/oauth2/redirect");
        when(response.isCommitted()).thenReturn(false);
        // clearAuthenticationAttributes calls getSession(false) — return null safely
        when(request.getSession(false)).thenReturn(null);

        when(oauth2LoginUseCase.processOAuth2Login(any())).thenReturn(
                new OAuth2LoginUseCase.TokenResult(
                        "access-abc", "refresh-xyz", 86400L,
                        new OAuth2LoginUseCase.UserInfo(
                                "id1", "user@gmail.com", "Jane", "Doe", "https://pic.url")));

        handler.onAuthenticationSuccess(request, response, auth);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).startsWith("http://localhost:3000/oauth2/redirect");
        assertThat(redirectUrl).contains("token=access-abc");
        assertThat(redirectUrl).contains("refresh=refresh-xyz");
    }

    @Test
    @DisplayName("onAuthenticationSuccess: skips redirect when response already committed")
    void onAuthenticationSuccess_responseCommitted_noRedirect() throws IOException {
        OAuth2AuthenticationToken auth = buildGoogleAuth(Map.of(
                "sub", "g-456", "email", "x@gmail.com",
                "given_name", "X", "family_name", "Y", "picture", "p"));

        when(request.getParameter("redirect_uri"))
                .thenReturn("http://localhost:3000/oauth2/redirect");
        when(response.isCommitted()).thenReturn(true);  // already committed
        when(oauth2LoginUseCase.processOAuth2Login(any())).thenReturn(
                new OAuth2LoginUseCase.TokenResult("t", "r", 86400L,
                        new OAuth2LoginUseCase.UserInfo("id2", "x@gmail.com", "X", "Y", "p")));

        handler.onAuthenticationSuccess(request, response, auth);

        verifyNoInteractions(redirectStrategy);
    }

    @Test
    @DisplayName("determineTargetUrl: throws BadRequestException for unauthorized redirect URI")
    void determineTargetUrl_unauthorizedRedirectUri_throws() {
        OAuth2AuthenticationToken auth = buildGoogleAuth(Map.of(
                "sub", "g-789", "email", "y@gmail.com",
                "given_name", "Y", "family_name", "Z", "picture", "pic"));

        when(request.getParameter("redirect_uri"))
                .thenReturn("http://evil.attacker.com/steal");

        assertThatThrownBy(() -> handler.determineTargetUrl(request, response, auth))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unauthorized redirect URI");
    }

    @Test
    @DisplayName("determineTargetUrl: Google user info is correctly extracted and passed to use case")
    void determineTargetUrl_extractsGoogleAttributesCorrectly() throws IOException {
        OAuth2AuthenticationToken auth = buildGoogleAuth(Map.of(
                "sub",         "google-sub-999",
                "email",       "test@gmail.com",
                "given_name",  "Test",
                "family_name", "User",
                "picture",     "https://lh3.googleusercontent.com/photo.jpg"));

        when(request.getParameter("redirect_uri"))
                .thenReturn("http://localhost:3000/oauth2/redirect");
        when(oauth2LoginUseCase.processOAuth2Login(any())).thenReturn(
                new OAuth2LoginUseCase.TokenResult("tok", "ref", 86400L,
                        new OAuth2LoginUseCase.UserInfo("id3", "test@gmail.com", "Test", "User", "pic")));

        handler.determineTargetUrl(request, response, auth);

        verify(oauth2LoginUseCase).processOAuth2Login(argThat(info ->
                info.providerId().equals("google-sub-999") &&
                        info.email().equals("test@gmail.com") &&
                        info.firstName().equals("Test") &&
                        info.lastName().equals("User")));
    }
}
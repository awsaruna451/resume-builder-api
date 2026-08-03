package com.resumebuilder.auth.adapter.in.web.handler;

import com.resumebuilder.auth.application.port.in.OAuth2LoginUseCase;
import com.resumebuilder.common.exception.BadRequestException;
import com.resumebuilder.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Called by Spring Security after Google's OAuth2 callback is verified.
 *
 * Flow:
 *  1. Frontend hits  GET /oauth2/authorize/google?redirect_uri=<frontend>
 *  2. CustomAuthorizationRequestResolver saves redirect_uri into the HTTP session
 *     (because the parameter is lost after Google's redirect)
 *  3. Google returns to  GET /oauth2/callback/google?code=...&state=...
 *  4. Spring verifies the state, then calls this handler
 *  5. We restore redirect_uri from the session, issue JWT, redirect to frontend
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // Session key — set by CustomAuthorizationRequestResolver during step 1
    public static final String REDIRECT_URI_SESSION_KEY = "oauth2_redirect_uri";

    private final OAuth2LoginUseCase oauth2LoginUseCase;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String authorizedRedirectUris;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response already committed, cannot redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        // Restore redirect_uri from session (it was saved before Google redirect)
        String redirectUri = getRedirectUriFromSession(request);

        if (redirectUri == null) {
            // Fallback: try query param (works for direct local testing)
            redirectUri = request.getParameter("redirect_uri");
        }

        if (redirectUri != null && !isAuthorizedRedirectUri(redirectUri)) {
            throw new BadRequestException("Unauthorized redirect URI: " + redirectUri);
        }

        String targetUrl = redirectUri != null ? redirectUri : getDefaultTargetUrl();

        // Extract Google user info
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        OAuth2LoginUseCase.OAuth2UserInfo userInfo = extractUserInfo(
                oauthUser, oauthToken.getAuthorizedClientRegistrationId());

        // Call domain use case — finds or creates user, issues JWT
        OAuth2LoginUseCase.TokenResult result = oauth2LoginUseCase.processOAuth2Login(userInfo);

        log.info("OAuth2 login success for {}, redirecting to {}", userInfo.email(), targetUrl);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token",   result.accessToken())
                .queryParam("refresh", result.refreshToken())
                .build().toUriString();
    }

    private String getRedirectUriFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        String uri = (String) session.getAttribute(REDIRECT_URI_SESSION_KEY);
        session.removeAttribute(REDIRECT_URI_SESSION_KEY); // consume once
        return uri;
    }

    private OAuth2LoginUseCase.OAuth2UserInfo extractUserInfo(OAuth2User oauthUser,
                                                              String registrationId) {
        String sub        = oauthUser.getAttribute("sub");
        String email      = oauthUser.getAttribute("email");
        String givenName  = oauthUser.getAttribute("given_name");
        String familyName = oauthUser.getAttribute("family_name");
        String picture    = oauthUser.getAttribute("picture");

        return new OAuth2LoginUseCase.OAuth2UserInfo(
                sub, email, givenName, familyName, picture, User.AuthProvider.GOOGLE);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientUri = URI.create(uri);
        List<String> allowed = Arrays.asList(authorizedRedirectUris.split(","));
        return allowed.stream().anyMatch(authorizedUri -> {
            URI authUri = URI.create(authorizedUri.trim());
            return authUri.getHost().equalsIgnoreCase(clientUri.getHost())
                    && authUri.getPort() == clientUri.getPort();
        });
    }
}
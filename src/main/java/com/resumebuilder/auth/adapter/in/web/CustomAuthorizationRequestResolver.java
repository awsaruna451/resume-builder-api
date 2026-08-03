package com.resumebuilder.auth.adapter.in.web;

import com.resumebuilder.auth.adapter.in.web.handler.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Intercepts GET /oauth2/authorize/google?redirect_uri=<frontend>
 * and saves redirect_uri into the HTTP session BEFORE Spring redirects
 * the browser to Google. Without this, the parameter is lost after
 * Google's roundtrip and the success handler has no target URL.
 */
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                repo, "/oauth2/authorize");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = delegate.resolve(request);
        if (authRequest != null) {
            saveRedirectUri(request);
        }
        return authRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
                                              String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest = delegate.resolve(request, clientRegistrationId);
        if (authRequest != null) {
            saveRedirectUri(request);
        }
        return authRequest;
    }

    private void saveRedirectUri(HttpServletRequest request) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !redirectUri.isBlank()) {
            HttpSession session = request.getSession(true); // create if absent
            session.setAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY,
                    redirectUri);
        }
    }
}
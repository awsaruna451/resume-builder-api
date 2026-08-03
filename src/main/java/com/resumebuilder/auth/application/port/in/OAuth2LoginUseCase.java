package com.resumebuilder.auth.application.port.in;

import com.resumebuilder.user.domain.User;

/**
 * Called after Google OAuth2 succeeds.
 * Finds or creates the user, then issues our own JWT pair.
 */
public interface OAuth2LoginUseCase {

    TokenResult processOAuth2Login(OAuth2UserInfo userInfo);

    record OAuth2UserInfo(
        String providerId,       // Google's "sub" claim
        String email,
        String firstName,
        String lastName,
        String pictureUrl,
        User.AuthProvider provider
    ) {}

    record TokenResult(
        String accessToken,
        String refreshToken,
        long   expiresIn,
        UserInfo user
    ) {}

    record UserInfo(
        String id,
        String email,
        String firstName,
        String lastName,
        String pictureUrl
    ) {}
}

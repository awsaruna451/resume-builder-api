package com.resumebuilder.auth.application.port.in;

/**
 * Handles JWT access-token refresh and logout.
 */
public interface TokenUseCase {

    TokenResult refresh(String refreshToken);

    void logout(String refreshToken);

    record TokenResult(
        String accessToken,
        String refreshToken,
        long   expiresIn
    ) {}
}

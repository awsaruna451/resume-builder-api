package com.resumebuilder.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenDtos {

    public record RefreshRequest(
        @NotBlank String refreshToken
    ) {}

    public record LogoutRequest(
        @NotBlank String refreshToken
    ) {}

    public record TokenResponse(
        String accessToken,
        String refreshToken,
        long   expiresIn,
        String tokenType
    ) {}
}

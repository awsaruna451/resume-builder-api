package com.resumebuilder.auth.adapter.in.web;

import com.resumebuilder.auth.adapter.in.web.dto.TokenDtos;
import com.resumebuilder.auth.application.port.in.TokenUseCase;
import com.resumebuilder.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Token refresh and logout")
public class TokenController {

    private final TokenUseCase tokenUseCase;

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access token")
    public ApiResponse<TokenDtos.TokenResponse> refresh(
            @Valid @RequestBody TokenDtos.RefreshRequest request) {

        TokenUseCase.TokenResult result = tokenUseCase.refresh(request.refreshToken());
        return ApiResponse.success(new TokenDtos.TokenResponse(
                result.accessToken(), result.refreshToken(), result.expiresIn(), "Bearer"));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke a refresh token (logout)")
    public void logout(@Valid @RequestBody TokenDtos.LogoutRequest request) {
        tokenUseCase.logout(request.refreshToken());
    }
}

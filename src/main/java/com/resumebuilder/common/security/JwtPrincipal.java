package com.resumebuilder.common.security;

import java.util.UUID;

public record JwtPrincipal(UUID userId, String email) {}

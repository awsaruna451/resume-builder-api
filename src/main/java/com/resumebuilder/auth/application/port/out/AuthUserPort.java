package com.resumebuilder.auth.application.port.out;

import com.resumebuilder.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserPort {
    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
}

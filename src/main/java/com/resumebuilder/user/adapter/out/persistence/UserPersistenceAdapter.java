package com.resumebuilder.user.adapter.out.persistence;

import com.resumebuilder.auth.application.port.out.AuthUserPort;
import com.resumebuilder.user.adapter.out.persistence.entity.UserJpaEntity;
import com.resumebuilder.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements AuthUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId) {
        return userJpaRepository
                .findByProviderAndProviderId(provider.name(), providerId)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        return toDomain(userJpaRepository.save(toEntity(user)));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private User toDomain(UserJpaEntity e) {
        return User.builder()
                .id(e.getId())
                .email(e.getEmail())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .pictureUrl(e.getPictureUrl())
                .provider(User.AuthProvider.valueOf(e.getProvider()))
                .providerId(e.getProviderId())
                .role(User.Role.valueOf(e.getRole()))
                .enabled(e.isEnabled())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private UserJpaEntity toEntity(User u) {
        return UserJpaEntity.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .pictureUrl(u.getPictureUrl())
                .provider(u.getProvider().name())
                .providerId(u.getProviderId())
                .role(u.getRole().name())
                .enabled(u.isEnabled())
                .build();
    }
}

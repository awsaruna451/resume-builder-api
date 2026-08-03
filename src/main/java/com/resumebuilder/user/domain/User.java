package com.resumebuilder.user.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
@With
public class User {
    UUID id;
    String email;
    String firstName;
    String lastName;
    String pictureUrl;
    AuthProvider provider;
    String providerId;
    Role role;
    boolean enabled;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public enum Role {
        USER, ADMIN
    }

    public enum AuthProvider {
        GOOGLE
    }

    public String fullName() {
        if (firstName == null && lastName == null) return email;
        return String.join(" ",
            firstName != null ? firstName : "",
            lastName  != null ? lastName  : "").trim();
    }
}

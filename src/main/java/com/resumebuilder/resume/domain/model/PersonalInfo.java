package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonalInfo {
    String firstName;
    String lastName;
    String email;
    String phone;
    String address;
    String linkedin;
    String github;
    String website;
}

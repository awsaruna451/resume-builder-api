package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Certification {
    String name;
    String issuer;
    String issueDate;
    String expiryDate;
    String credentialId;
    String url;
}

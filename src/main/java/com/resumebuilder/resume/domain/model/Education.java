package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Education {
    String university;
    String degree;
    String fieldOfStudy;
    String startDate;
    String endDate;
    String gpa;
    String description;
}

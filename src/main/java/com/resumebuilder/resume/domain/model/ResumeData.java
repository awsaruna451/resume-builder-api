package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ResumeData {
    PersonalInfo personalInfo;
    String summary;
    List<Experience> experiences;
    List<Education> education;
    List<Skill> skills;
    List<Project> projects;
    List<Certification> certifications;
}

package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Skill {
    String name;
    String category;
    String level; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}

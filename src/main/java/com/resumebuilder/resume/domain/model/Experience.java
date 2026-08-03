package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Experience {
    String company;
    String position;
    String location;
    String startDate;
    String endDate;
    boolean current;
    List<String> achievements;
}

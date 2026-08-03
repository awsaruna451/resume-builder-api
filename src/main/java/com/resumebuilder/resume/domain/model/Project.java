package com.resumebuilder.resume.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Project {
    String name;
    String description;
    String url;
    String repoUrl;
    List<String> technologies;
    String startDate;
    String endDate;
}

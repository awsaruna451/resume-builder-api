package com.resumebuilder.resume.adapter.in.web.dto;

import com.resumebuilder.resume.domain.Resume;
import com.resumebuilder.resume.domain.model.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ResumeDtos {

    public record CreateResumeRequest(
        @NotBlank String title,
        String templateId,
        ResumeDataDto resumeData
    ) {}

    public record UpdateResumeRequest(
        @NotBlank String title,
        String templateId,
        ResumeDataDto resumeData
    ) {}

    public record UpdateResumeDataRequest(
        ResumeDataDto resumeData
    ) {}

    public record ResumeDataDto(
        PersonalInfoDto personalInfo,
        String summary,
        List<ExperienceDto> experiences,
        List<EducationDto> education,
        List<SkillDto> skills,
        List<ProjectDto> projects,
        List<CertificationDto> certifications
    ) {}

    public record PersonalInfoDto(
        String firstName, String lastName, String email, String phone,
        String address, String linkedin, String github, String website
    ) {}

    public record ExperienceDto(
        String company, String position, String location,
        String startDate, String endDate, boolean current,
        List<String> achievements
    ) {}

    public record EducationDto(
        String university, String degree, String fieldOfStudy,
        String startDate, String endDate, String gpa, String description
    ) {}

    public record SkillDto(
        String name, String category, String level
    ) {}

    public record ProjectDto(
        String name, String description, String url, String repoUrl,
        List<String> technologies, String startDate, String endDate
    ) {}

    public record CertificationDto(
        String name, String issuer, String issueDate,
        String expiryDate, String credentialId, String url
    ) {}

    public record ResumeResponse(
        UUID id,
        UUID userId,
        String title,
        String templateId,
        ResumeDataDto resumeData,
        String fileName,
        Long fileSize,
        String mimeType,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        public static ResumeResponse from(Resume r) {
            return new ResumeResponse(
                r.getId(), r.getUserId(), r.getTitle(), r.getTemplateId(),
                r.getResumeData() != null ? toDto(r.getResumeData()) : null,
                r.getFileName(), r.getFileSize(), r.getMimeType(),
                r.isPublic(), r.getCreatedAt(), r.getUpdatedAt());
        }

        private static ResumeDataDto toDto(ResumeData d) {
            return new ResumeDataDto(
                d.getPersonalInfo()  != null ? toPersonalInfoDto(d.getPersonalInfo()) : null,
                d.getSummary(),
                d.getExperiences()   != null ? d.getExperiences().stream().map(ResumeDtos::toExpDto).toList() : null,
                d.getEducation()     != null ? d.getEducation().stream().map(ResumeDtos::toEduDto).toList() : null,
                d.getSkills()        != null ? d.getSkills().stream().map(ResumeDtos::toSkillDto).toList() : null,
                d.getProjects()      != null ? d.getProjects().stream().map(ResumeDtos::toProjDto).toList() : null,
                d.getCertifications()!= null ? d.getCertifications().stream().map(ResumeDtos::toCertDto).toList() : null
            );
        }

        private static PersonalInfoDto toPersonalInfoDto(PersonalInfo p) {
            return new PersonalInfoDto(p.getFirstName(), p.getLastName(), p.getEmail(),
                p.getPhone(), p.getAddress(), p.getLinkedin(), p.getGithub(), p.getWebsite());
        }
    }

    public static ResumeData toDomain(ResumeDataDto dto) {
        if (dto == null) return null;
        return ResumeData.builder()
            .personalInfo(dto.personalInfo() != null ? PersonalInfo.builder()
                .firstName(dto.personalInfo().firstName())
                .lastName(dto.personalInfo().lastName())
                .email(dto.personalInfo().email())
                .phone(dto.personalInfo().phone())
                .address(dto.personalInfo().address())
                .linkedin(dto.personalInfo().linkedin())
                .github(dto.personalInfo().github())
                .website(dto.personalInfo().website())
                .build() : null)
            .summary(dto.summary())
            .experiences(dto.experiences() != null ? dto.experiences().stream()
                .map(e -> Experience.builder()
                    .company(e.company()).position(e.position()).location(e.location())
                    .startDate(e.startDate()).endDate(e.endDate()).current(e.current())
                    .achievements(e.achievements()).build()).toList() : null)
            .education(dto.education() != null ? dto.education().stream()
                .map(e -> Education.builder()
                    .university(e.university()).degree(e.degree()).fieldOfStudy(e.fieldOfStudy())
                    .startDate(e.startDate()).endDate(e.endDate()).gpa(e.gpa())
                    .description(e.description()).build()).toList() : null)
            .skills(dto.skills() != null ? dto.skills().stream()
                .map(s -> Skill.builder().name(s.name()).category(s.category()).level(s.level()).build()).toList() : null)
            .projects(dto.projects() != null ? dto.projects().stream()
                .map(p -> Project.builder()
                    .name(p.name()).description(p.description()).url(p.url()).repoUrl(p.repoUrl())
                    .technologies(p.technologies()).startDate(p.startDate()).endDate(p.endDate()).build()).toList() : null)
            .certifications(dto.certifications() != null ? dto.certifications().stream()
                .map(c -> Certification.builder()
                    .name(c.name()).issuer(c.issuer()).issueDate(c.issueDate())
                    .expiryDate(c.expiryDate()).credentialId(c.credentialId()).url(c.url()).build()).toList() : null)
            .build();
    }

    private static ExperienceDto toExpDto(Experience e) {
        return new ExperienceDto(e.getCompany(), e.getPosition(), e.getLocation(),
            e.getStartDate(), e.getEndDate(), e.isCurrent(), e.getAchievements());
    }

    private static EducationDto toEduDto(Education e) {
        return new EducationDto(e.getUniversity(), e.getDegree(), e.getFieldOfStudy(),
            e.getStartDate(), e.getEndDate(), e.getGpa(), e.getDescription());
    }

    private static SkillDto toSkillDto(Skill s) {
        return new SkillDto(s.getName(), s.getCategory(), s.getLevel());
    }

    private static ProjectDto toProjDto(Project p) {
        return new ProjectDto(p.getName(), p.getDescription(), p.getUrl(), p.getRepoUrl(),
            p.getTechnologies(), p.getStartDate(), p.getEndDate());
    }

    private static CertificationDto toCertDto(Certification c) {
        return new CertificationDto(c.getName(), c.getIssuer(), c.getIssueDate(),
            c.getExpiryDate(), c.getCredentialId(), c.getUrl());
    }
}

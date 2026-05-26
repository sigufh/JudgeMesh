package com.judgemesh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDTO {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private List<String> tags;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    /** testcase manifest URL, submit-service uses it to build JudgeTask */
    private String testcaseManifestUrl;
    private Long setterId;
    private Boolean published;
    private Integer totalSubmit;
    private Integer totalAc;
    /** DRAFT / PUBLISHED / OFFLINE */
    private String status;
}

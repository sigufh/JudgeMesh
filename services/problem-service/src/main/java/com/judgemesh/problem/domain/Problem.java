package com.judgemesh.problem.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Problem {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private String difficulty;
    private Long setterId;
    private Boolean published;
    private Integer totalSubmit;
    private Integer totalAc;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}

package com.judgemesh.problem.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCase {
    private Integer caseIndex;
    private String input;
    private String expectedOutput;
    private Integer score;
}

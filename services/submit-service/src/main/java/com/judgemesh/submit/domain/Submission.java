package com.judgemesh.submit.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Submission {
    private Long id;
    private Long userId;
    private Long problemId;
    private Long contestId;
    private String language;
    private String code;
    private Integer codeLength;
    private SubmissionStatus status;
    private Integer score;
    private Integer timeUsedMs;
    private Integer memoryUsedKb;
    private String judgeMessage;
    private String judgedByWorker;
    private Instant submittedAt;
    private Instant judgedAt;
}

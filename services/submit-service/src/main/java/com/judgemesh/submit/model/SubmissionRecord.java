package com.judgemesh.submit.model;

import com.judgemesh.api.enumx.LanguageType;
import com.judgemesh.api.enumx.SubmitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRecord {
    private Long id;
    private Long userId;
    private Long problemId;
    private Long contestId;
    private LanguageType language;
    private String code;
    private SubmitStatus status;
    private Integer score;
    private Integer timeUsedMs;
    private Integer memoryUsedKb;
    private String judgeMessage;
    private String judgedByWorker;
    private Instant submittedAt;
    private Instant judgedAt;
}

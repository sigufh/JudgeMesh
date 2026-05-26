package com.judgemesh.api.dto;

import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.api.enumx.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {
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

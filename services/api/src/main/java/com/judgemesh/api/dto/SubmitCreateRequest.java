package com.judgemesh.api.dto;

import com.judgemesh.api.enumx.LanguageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCreateRequest {

    @NotNull
    private Long problemId;

    private Long contestId;

    @NotNull
    private LanguageType language;

    @NotBlank
    private String code;

    /** Optional override used by local bootstrap / smoke-friendly flows. */
    private Integer timeLimitMs;

    /** Optional override used by local bootstrap / smoke-friendly flows. */
    private Integer memoryLimitMb;

    /** Optional override, usually resolved from problem-service. */
    private String testcaseManifestUrl;
}

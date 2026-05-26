package com.judgemesh.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestUpsertRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @Builder.Default
    private Integer freezeBeforeMin = 30;

    @Builder.Default
    private List<Long> problemIds = List.of();
}

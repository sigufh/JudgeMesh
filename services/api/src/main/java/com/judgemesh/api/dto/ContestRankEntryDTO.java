package com.judgemesh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestRankEntryDTO {
    private Long rank;
    private Long userId;
    private Integer solved;
    private Integer penaltyMinutes;
    private Integer score;
    private Instant lastAcceptedAt;
}

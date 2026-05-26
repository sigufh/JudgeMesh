package com.judgemesh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankEntryDTO {
    private Integer rank;
    private Long userId;
    private String username;
    private Integer solved;
    private Integer penaltyMinutes;
    private Double score;
}

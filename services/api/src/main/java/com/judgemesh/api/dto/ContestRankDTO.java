package com.judgemesh.api.dto;

import com.judgemesh.api.enumx.ContestStatus;
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
public class ContestRankDTO {
    private Long contestId;
    private ContestStatus status;
    private boolean frozen;
    private Instant frozenAt;
    private List<ContestRankEntryDTO> entries;
}

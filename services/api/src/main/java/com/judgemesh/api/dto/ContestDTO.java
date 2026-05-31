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
public class ContestDTO {
    private Long id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer freezeBeforeMin;
    private Long createdBy;
    private Instant createdAt;
    private List<Long> problemIds;
    private Long registeredCount;
    private ContestStatus status;
    private Boolean frozen;
    private Boolean registered;
}

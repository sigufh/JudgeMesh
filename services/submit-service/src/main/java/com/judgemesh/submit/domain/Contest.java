package com.judgemesh.submit.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class Contest {
    private Long id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer freezeBeforeMin;
    private Long createdBy;
    @Builder.Default
    private List<Long> problemIds = new ArrayList<>();
    @Builder.Default
    private Set<Long> registeredUserIds = new HashSet<>();

    public boolean frozen(Instant now) {
        return endTime.minusSeconds((long) freezeBeforeMin * 60).isBefore(now) && now.isBefore(endTime);
    }
}

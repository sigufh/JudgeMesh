package com.judgemesh.submit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestRecord {
    private Long id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer freezeBeforeMin;
    private Long createdBy;
    private Instant createdAt;

    @Builder.Default
    private List<Long> problemIds = new CopyOnWriteArrayList<>();

    @Builder.Default
    private Set<Long> registeredUserIds = ConcurrentHashMap.newKeySet();
}

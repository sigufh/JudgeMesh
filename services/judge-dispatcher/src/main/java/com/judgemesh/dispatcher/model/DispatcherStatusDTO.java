package com.judgemesh.dispatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatcherStatusDTO {
    private String mode;
    private boolean leader;
    private String leaderId;
    private Instant lastDispatchAt;
    private List<String> workers;
    private Map<String, Integer> inflight;
    private Map<String, Instant> blacklistedWorkers;
}

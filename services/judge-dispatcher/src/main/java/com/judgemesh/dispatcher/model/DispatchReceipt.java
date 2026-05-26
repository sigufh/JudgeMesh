package com.judgemesh.dispatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchReceipt {
    private Long submissionId;
    private String workerUrl;
    private Instant dispatchedAt;
    private int attempt;
    private String leaderId;
}

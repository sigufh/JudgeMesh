package com.judgemesh.submit.controller;

import com.judgemesh.api.dto.SubmissionDTO;
import com.judgemesh.api.dto.SubmitCreateRequest;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.submit.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/submit")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ApiResponse<SubmissionDTO> submit(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody SubmitCreateRequest request) {
        return ApiResponse.ok(submissionService.submit(userId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubmissionDTO> getSubmission(@PathVariable Long id) {
        return ApiResponse.ok(submissionService.getSubmission(id));
    }

    @GetMapping("/mine")
    public ApiResponse<List<SubmissionDTO>> mine(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        return ApiResponse.ok(submissionService.listMine(userId));
    }

    @PostMapping("/internal/result")
    public ApiResponse<SubmissionDTO> internalResult(@RequestBody JudgeResult result) {
        return ApiResponse.ok(submissionService.applyJudgeResult(result));
    }
}

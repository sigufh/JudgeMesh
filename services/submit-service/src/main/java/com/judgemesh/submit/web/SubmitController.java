package com.judgemesh.submit.web;

import com.judgemesh.api.dto.SubmitDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.submit.service.SubmissionService;
import com.judgemesh.submit.service.SubmissionService.SubmitCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class SubmitController {
    private final SubmissionService submissionService;

    public SubmitController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping({"/api/submit", "/api/submits"})
    public ApiResponse<SubmitDTO> submit(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody SubmitRequest request) {
        Long userId = headerUserId == null ? request.userId() : headerUserId;
        if (userId == null) {
            userId = 1002L;
        }
        SubmitCommand command = new SubmitCommand(
                userId,
                request.problemId(),
                request.contestId(),
                request.language(),
                request.code());
        return ApiResponse.ok(submissionService.submit(command));
    }

    @GetMapping({"/api/submit/{id}", "/api/submits/{id}"})
    public ApiResponse<SubmitDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(submissionService.get(id));
    }

    @GetMapping({"/api/submit/mine", "/api/submits/mine"})
    public ApiResponse<List<SubmitDTO>> mine(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long resolvedUserId = headerUserId == null ? (userId == null ? 1002L : userId) : headerUserId;
        return ApiResponse.ok(submissionService.mine(resolvedUserId));
    }

    @PostMapping("/api/submit/internal/result")
    public SubmitDTO workerCallback(@RequestBody JudgeResult result) {
        return submissionService.applyResult(result);
    }

    @PostMapping("/api/submit/internal/{id}/judging")
    public SubmitDTO markJudging(@PathVariable Long id, @RequestParam String workerId) {
        return submissionService.markJudging(id, workerId);
    }

    public record SubmitRequest(
            Long userId,
            @NotNull Long problemId,
            Long contestId,
            @NotBlank String language,
            @NotBlank String code) {
    }
}

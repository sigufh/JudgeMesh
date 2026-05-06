package com.judgemesh.submit.controller;

import com.judgemesh.api.dto.ContestDTO;
import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.api.dto.ContestUpsertRequest;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.submit.service.ContestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contest")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @PostMapping
    public ApiResponse<ContestDTO> create(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody ContestUpsertRequest request) {
        return ApiResponse.ok(contestService.createContest(userId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContestDTO> update(@PathVariable Long id, @Valid @RequestBody ContestUpsertRequest request) {
        return ApiResponse.ok(contestService.updateContest(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContestDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(contestService.getContest(id));
    }

    @GetMapping("/list")
    public ApiResponse<List<ContestDTO>> list() {
        return ApiResponse.ok(contestService.listContests());
    }

    @PostMapping("/{id}/register")
    public ApiResponse<ContestDTO> register(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        return ApiResponse.ok(contestService.registerContest(id, userId));
    }

    @GetMapping("/{id}/rank")
    public ApiResponse<ContestRankDTO> rank(@PathVariable Long id) {
        return ApiResponse.ok(contestService.contestRank(id));
    }
}

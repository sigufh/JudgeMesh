package com.judgemesh.submit.web;

import com.judgemesh.api.dto.ContestDTO;
import com.judgemesh.api.dto.RankEntryDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.submit.service.ContestService;
import com.judgemesh.submit.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class ContestController {
    private final ContestService contestService;
    private final RankingService rankingService;

    public ContestController(ContestService contestService, RankingService rankingService) {
        this.contestService = contestService;
        this.rankingService = rankingService;
    }

    @GetMapping({"/api/contest/list", "/api/contests"})
    public ApiResponse<List<ContestDTO>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long resolvedUserId = headerUserId == null ? userId : headerUserId;
        return ApiResponse.ok(contestService.list(resolvedUserId));
    }

    @PostMapping("/api/contest/{id}/register")
    public ApiResponse<ContestDTO> register(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long resolvedUserId = headerUserId == null ? (userId == null ? 1002L : userId) : headerUserId;
        return ApiResponse.ok(contestService.register(id, resolvedUserId));
    }

    @GetMapping("/api/contest/{id}/rank")
    public ApiResponse<List<RankEntryDTO>> contestRank(@PathVariable Long id) {
        return ApiResponse.ok(contestService.rank(id));
    }

    @GetMapping("/api/rank/global")
    public ApiResponse<List<RankEntryDTO>> globalRank() {
        return ApiResponse.ok(rankingService.globalRank());
    }
}

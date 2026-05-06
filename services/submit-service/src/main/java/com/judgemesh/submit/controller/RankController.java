package com.judgemesh.submit.controller;

import com.judgemesh.api.dto.ContestRankEntryDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.submit.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ApiResponse<List<ContestRankEntryDTO>> global() {
        List<ContestRankEntryDTO> entries = leaderboardService.globalRank();
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank((long) i + 1);
        }
        return ApiResponse.ok(entries);
    }
}

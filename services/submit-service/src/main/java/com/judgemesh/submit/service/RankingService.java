package com.judgemesh.submit.service;

import com.judgemesh.api.dto.RankEntryDTO;
import com.judgemesh.submit.domain.Submission;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RankingService {
    private final Map<Long, ScoreBoard> global = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, ScoreBoard>> contests = new ConcurrentHashMap<>();
    private final Set<String> solvedPairs = ConcurrentHashMap.newKeySet();
    private final Set<String> solvedContestPairs = ConcurrentHashMap.newKeySet();

    public void accepted(Submission submission, Instant contestStart) {
        String pair = submission.getUserId() + ":" + submission.getProblemId();
        if (solvedPairs.add(pair)) {
            global.computeIfAbsent(submission.getUserId(), ScoreBoard::new)
                    .accept(minutesSince(submission.getSubmittedAt(), submission.getJudgedAt()));
        }
        if (submission.getContestId() != null && contestStart != null) {
            String contestPair = submission.getContestId() + ":" + pair;
            if (solvedContestPairs.add(contestPair)) {
                contests.computeIfAbsent(submission.getContestId(), ignored -> new ConcurrentHashMap<>())
                        .computeIfAbsent(submission.getUserId(), ScoreBoard::new)
                        .accept(minutesSince(contestStart, submission.getJudgedAt()));
            }
        }
    }

    public List<RankEntryDTO> globalRank() {
        return rank(global);
    }

    public List<RankEntryDTO> contestRank(Long contestId) {
        return rank(contests.getOrDefault(contestId, Map.of()));
    }

    private static List<RankEntryDTO> rank(Map<Long, ScoreBoard> scores) {
        List<ScoreBoard> sorted = new ArrayList<>(scores.values());
        sorted.sort(Comparator.comparing(ScoreBoard::score).reversed());
        List<RankEntryDTO> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ScoreBoard board = sorted.get(i);
            entries.add(RankEntryDTO.builder()
                    .rank(i + 1)
                    .userId(board.userId)
                    .username("user-" + board.userId)
                    .solved(board.solved)
                    .penaltyMinutes(board.penaltyMinutes)
                    .score(board.score())
                    .build());
        }
        return entries;
    }

    private static int minutesSince(Instant start, Instant end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        return (int) Math.max(0, Duration.between(start, end).toMinutes());
    }

    private static final class ScoreBoard {
        private final Long userId;
        private int solved;
        private int penaltyMinutes;

        private ScoreBoard(Long userId) {
            this.userId = userId;
        }

        private synchronized void accept(int penalty) {
            solved++;
            penaltyMinutes += penalty;
        }

        private double score() {
            return solved * 100_000D - penaltyMinutes;
        }
    }
}

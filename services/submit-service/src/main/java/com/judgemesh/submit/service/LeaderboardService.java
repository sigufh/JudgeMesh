package com.judgemesh.submit.service;

import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.api.dto.ContestRankEntryDTO;
import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.submit.config.SubmitProperties;
import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.model.SubmissionRecord;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final SubmitProperties properties;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    private final ConcurrentMap<Long, GlobalScore> globalScores = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ConcurrentMap<Long, ContestScore>> contestScores = new ConcurrentHashMap<>();

    public void recordResult(SubmissionRecord submission, ContestRecord contest, SubmitStatus status, Instant judgedAt) {
        if (status == null) {
            return;
        }
        if (status == SubmitStatus.AC) {
            recordGlobalAccept(submission, judgedAt);
            if (contest != null) {
                recordContestAccept(submission, contest, judgedAt);
            }
        } else if (contest != null) {
            recordContestReject(submission, contest);
        }
        mirrorToRedis(contest);
    }

    public List<ContestRankEntryDTO> contestRank(long contestId) {
        ConcurrentMap<Long, ContestScore> scores = contestScores.get(contestId);
        if (scores == null || scores.isEmpty()) {
            return List.of();
        }
        return scores.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<Long, ContestScore> e) -> e.getValue().getSolved()).reversed()
                        .thenComparing(e -> e.getValue().getPenaltyMinutes())
                        .thenComparing(e -> Optional.ofNullable(e.getValue().getLastAcceptedAt()).orElse(Instant.MAX))
                        .thenComparing(Map.Entry::getKey))
                .map(entry -> toContestRankEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<ContestRankEntryDTO> globalRank() {
        return globalScores.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<Long, GlobalScore> e) -> e.getValue().getSolved()).reversed()
                        .thenComparing(e -> e.getValue().getAcceptedSeconds())
                        .thenComparing(Map.Entry::getKey))
                .map(entry -> ContestRankEntryDTO.builder()
                        .rank(0L)
                        .userId(entry.getKey())
                        .solved(entry.getValue().getSolved())
                        .penaltyMinutes((int) (entry.getValue().getAcceptedSeconds() / 60))
                        .score(entry.getValue().getSolved() * 1_000_000
                                - (int) Math.min(Integer.MAX_VALUE, entry.getValue().getAcceptedSeconds()))
                        .lastAcceptedAt(entry.getValue().getLastAcceptedAt())
                        .build())
                .toList();
    }

    private void recordGlobalAccept(SubmissionRecord submission, Instant judgedAt) {
        GlobalScore score = globalScores.computeIfAbsent(submission.getUserId(), key -> new GlobalScore());
        synchronized (score) {
            if (score.getSolvedProblems().add(submission.getProblemId())) {
                score.setSolved(score.getSolved() + 1);
                score.setAcceptedSeconds(score.getAcceptedSeconds() + acceptedSeconds(submission, judgedAt));
                score.setLastAcceptedAt(judgedAt);
            }
        }
    }

    private void recordContestAccept(SubmissionRecord submission, ContestRecord contest, Instant judgedAt) {
        ConcurrentMap<Long, ContestScore> contestScoreMap =
                contestScores.computeIfAbsent(contest.getId(), key -> new ConcurrentHashMap<>());
        ContestScore score = contestScoreMap.computeIfAbsent(submission.getUserId(), key -> new ContestScore());
        synchronized (score) {
            ContestProblemProgress progress = score.getProblemProgress()
                    .computeIfAbsent(submission.getProblemId(), key -> new ContestProblemProgress());
            if (progress.isAccepted()) {
                return;
            }
            progress.setAccepted(true);
            score.setSolved(score.getSolved() + 1);
            score.setPenaltyMinutes(score.getPenaltyMinutes()
                    + contestAcceptedMinutes(submission, contest)
                    + progress.getWrongAttempts() * 20);
            score.setLastAcceptedAt(judgedAt);
        }
    }

    private void recordContestReject(SubmissionRecord submission, ContestRecord contest) {
        ConcurrentMap<Long, ContestScore> contestScoreMap =
                contestScores.computeIfAbsent(contest.getId(), key -> new ConcurrentHashMap<>());
        ContestScore score = contestScoreMap.computeIfAbsent(submission.getUserId(), key -> new ContestScore());
        synchronized (score) {
            ContestProblemProgress progress = score.getProblemProgress()
                    .computeIfAbsent(submission.getProblemId(), key -> new ContestProblemProgress());
            if (!progress.isAccepted()) {
                progress.setWrongAttempts(progress.getWrongAttempts() + 1);
            }
        }
    }

    private ContestRankEntryDTO toContestRankEntry(Long userId, ContestScore score) {
        return ContestRankEntryDTO.builder()
                .rank(0L)
                .userId(userId)
                .solved(score.getSolved())
                .penaltyMinutes(score.getPenaltyMinutes())
                .score(score.getSolved() * 100_000 - score.getPenaltyMinutes())
                .lastAcceptedAt(score.getLastAcceptedAt())
                .build();
    }

    private long acceptedSeconds(SubmissionRecord submission, Instant judgedAt) {
        if (submission.getTimeUsedMs() != null) {
            return Math.max(0L, submission.getTimeUsedMs() / 1000L);
        }
        if (submission.getSubmittedAt() != null && judgedAt != null) {
            return Math.max(0L, Duration.between(submission.getSubmittedAt(), judgedAt).toSeconds());
        }
        return 0L;
    }

    private int contestAcceptedMinutes(SubmissionRecord submission, ContestRecord contest) {
        if (submission.getSubmittedAt() == null || contest.getStartTime() == null) {
            return 0;
        }
        long minutes = Duration.between(contest.getStartTime(), submission.getSubmittedAt()).toMinutes();
        return (int) Math.max(0L, minutes);
    }

    private void mirrorToRedis(ContestRecord contest) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        mirrorGlobalToRedis(redisTemplate);
        if (contest != null) {
            mirrorContestToRedis(redisTemplate, contest.getId());
        }
    }

    private void mirrorGlobalToRedis(StringRedisTemplate redisTemplate) {
        String key = properties.getLeaderboard().getGlobalKey();
        redisTemplate.delete(key);
        globalRank().forEach(entry -> redisTemplate.opsForZSet().add(
                key,
                String.valueOf(entry.getUserId()),
                entry.getScore().doubleValue()));
    }

    private void mirrorContestToRedis(StringRedisTemplate redisTemplate, Long contestId) {
        String key = properties.getLeaderboard().getContestKeyPrefix() + contestId;
        redisTemplate.delete(key);
        contestRank(contestId).forEach(entry -> redisTemplate.opsForZSet().add(
                key,
                String.valueOf(entry.getUserId()),
                entry.getScore().doubleValue()));
    }

    @Data
    private static class GlobalScore {
        private int solved;
        private long acceptedSeconds;
        private Instant lastAcceptedAt;
        private Set<Long> solvedProblems = new HashSet<>();
    }

    @Data
    private static class ContestScore {
        private int solved;
        private int penaltyMinutes;
        private Instant lastAcceptedAt;
        private Map<Long, ContestProblemProgress> problemProgress = new ConcurrentHashMap<>();
    }

    @Data
    private static class ContestProblemProgress {
        private boolean accepted;
        private int wrongAttempts;
    }
}

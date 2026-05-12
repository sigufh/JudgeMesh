package com.judgemesh.submit.service;

import com.judgemesh.api.dto.RankEntryDTO;
import com.judgemesh.submit.domain.Submission;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
    private static final int PAGE_SIZE = 100;

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final Map<Long, ScoreBoard> global = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, ScoreBoard>> contests = new ConcurrentHashMap<>();
    private final Set<String> solvedPairs = ConcurrentHashMap.newKeySet();
    private final Set<String> solvedContestPairs = ConcurrentHashMap.newKeySet();

    public RankingService(
            ObjectProvider<StringRedisTemplate> redisTemplate,
            @Value("${judgemesh.leaderboard.redis-key-prefix:leaderboard}") String keyPrefix) {
        this.redisTemplate = redisTemplate.getIfAvailable();
        this.keyPrefix = keyPrefix;
    }

    public void accepted(Submission submission, Instant contestStart) {
        if (acceptedRedis(submission, contestStart)) {
            return;
        }
        acceptedMemory(submission, contestStart);
    }

    public List<RankEntryDTO> globalRank() {
        List<RankEntryDTO> redisRank = rankRedis("global");
        if (redisRank != null) {
            return redisRank;
        }
        return rank(global);
    }

    public List<RankEntryDTO> contestRank(Long contestId) {
        List<RankEntryDTO> redisRank = rankRedis("contest:" + contestId);
        if (redisRank != null) {
            return redisRank;
        }
        return rank(contests.getOrDefault(contestId, Map.of()));
    }

    private boolean acceptedRedis(Submission submission, Instant contestStart) {
        if (redisTemplate == null) {
            return false;
        }
        try {
            String pair = submission.getUserId() + ":" + submission.getProblemId();
            Long added = redisTemplate.opsForSet().add(key("global:solved"), pair);
            if (added != null && added == 1L) {
                updateRedisRank("global", submission.getUserId(),
                        minutesSince(submission.getSubmittedAt(), submission.getJudgedAt()));
            }
            if (submission.getContestId() != null && contestStart != null) {
                String scope = "contest:" + submission.getContestId();
                String contestPair = submission.getContestId() + ":" + pair;
                Long contestAdded = redisTemplate.opsForSet().add(key(scope + ":solved"), contestPair);
                if (contestAdded != null && contestAdded == 1L) {
                    updateRedisRank(scope, submission.getUserId(),
                            minutesSince(contestStart, submission.getJudgedAt()));
                }
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void updateRedisRank(String scope, Long userId, int penaltyMinutes) {
        String detailKey = key(scope + ":user:" + userId);
        Long solved = redisTemplate.opsForHash().increment(detailKey, "solved", 1);
        Long penalty = redisTemplate.opsForHash().increment(detailKey, "penalty", penaltyMinutes);
        double score = (solved == null ? 0 : solved) * 100_000D - (penalty == null ? 0 : penalty);
        redisTemplate.opsForZSet().add(key(scope + ":zset"), userId.toString(), score);
    }

    private List<RankEntryDTO> rankRedis(String scope) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(key(scope + ":zset"), 0, PAGE_SIZE - 1);
            if (tuples == null) {
                return List.of();
            }
            List<RankEntryDTO> entries = new ArrayList<>();
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                Long userId = Long.parseLong(tuple.getValue());
                String detailKey = key(scope + ":user:" + userId);
                int solved = parseInt(redisTemplate.opsForHash().get(detailKey, "solved"));
                int penalty = parseInt(redisTemplate.opsForHash().get(detailKey, "penalty"));
                entries.add(RankEntryDTO.builder()
                        .rank(rank++)
                        .userId(userId)
                        .username("user-" + userId)
                        .solved(solved)
                        .penaltyMinutes(penalty)
                        .score(tuple.getScore() == null ? 0 : tuple.getScore())
                        .build());
            }
            return entries;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void acceptedMemory(Submission submission, Instant contestStart) {
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

    private String key(String suffix) {
        return keyPrefix + ":" + suffix;
    }

    private static int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
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

package com.judgemesh.submit.repository;

import com.judgemesh.submit.domain.Contest;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ContestStore {
    private final AtomicLong ids = new AtomicLong(2000);
    private final ConcurrentHashMap<Long, Contest> contests = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        if (contests.isEmpty()) {
            Instant now = Instant.now();
            save(Contest.builder()
                    .title("JudgeMesh Demo Contest")
                    .description("Local smoke contest for the distributed judge flow.")
                    .startTime(now.minus(1, ChronoUnit.HOURS))
                    .endTime(now.plus(3, ChronoUnit.HOURS))
                    .freezeBeforeMin(30)
                    .createdBy(1001L)
                    .problemIds(List.of(1001L, 1002L))
                    .build());
        }
    }

    public Contest save(Contest contest) {
        if (contest.getId() == null) {
            contest.setId(ids.incrementAndGet());
        }
        contests.put(contest.getId(), contest);
        return contest;
    }

    public Optional<Contest> findById(Long id) {
        return Optional.ofNullable(contests.get(id));
    }

    public List<Contest> findAll() {
        return contests.values().stream()
                .sorted(Comparator.comparing(Contest::getStartTime).reversed())
                .toList();
    }
}

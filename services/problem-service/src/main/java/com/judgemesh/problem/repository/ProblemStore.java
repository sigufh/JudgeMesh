package com.judgemesh.problem.repository;

import com.judgemesh.problem.domain.Problem;
import com.judgemesh.problem.domain.TestCase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProblemStore {
    private final AtomicLong ids = new AtomicLong(1000);
    private final ConcurrentHashMap<Long, Problem> problems = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        if (problems.isEmpty()) {
            save(Problem.builder()
                    .title("A+B Problem")
                    .description("Read two integers and print their sum.")
                    .difficulty("EASY")
                    .setterId(1001L)
                    .published(true)
                    .timeLimitMs(1000)
                    .memoryLimitMb(256)
                    .tags(List.of("math", "warmup"))
                    .testCases(List.of(
                            TestCase.builder().caseIndex(1).input("1 2\n").expectedOutput("3\n").score(50).build(),
                            TestCase.builder().caseIndex(2).input("10 -7\n").expectedOutput("3\n").score(50).build()))
                    .totalSubmit(0)
                    .totalAc(0)
                    .build());
            save(Problem.builder()
                    .title("Echo Lines")
                    .description("Read a line and print it unchanged.")
                    .difficulty("EASY")
                    .setterId(1001L)
                    .published(true)
                    .timeLimitMs(1000)
                    .memoryLimitMb(128)
                    .tags(List.of("string"))
                    .testCases(List.of(
                            TestCase.builder().caseIndex(1).input("JudgeMesh\n").expectedOutput("JudgeMesh\n").score(100).build()))
                    .totalSubmit(0)
                    .totalAc(0)
                    .build());
        }
    }

    public Problem save(Problem problem) {
        Instant now = Instant.now();
        if (problem.getId() == null) {
            problem.setId(ids.incrementAndGet());
            problem.setCreatedAt(now);
        }
        problem.setUpdatedAt(now);
        if (problem.getPublished() == null) {
            problem.setPublished(false);
        }
        if (problem.getTotalSubmit() == null) {
            problem.setTotalSubmit(0);
        }
        if (problem.getTotalAc() == null) {
            problem.setTotalAc(0);
        }
        problems.put(problem.getId(), problem);
        return problem;
    }

    public Optional<Problem> findById(Long id) {
        return Optional.ofNullable(problems.get(id));
    }

    public List<Problem> findAll() {
        return new ArrayList<>(problems.values());
    }
}

package com.judgemesh.problem.repository;

import com.judgemesh.problem.domain.Problem;
import com.judgemesh.problem.domain.TestCase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProblemStore {
    private final AtomicLong ids = new AtomicLong(1000);
    private final ConcurrentHashMap<Long, Problem> problems = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public ProblemStore(ObjectProvider<JdbcTemplate> jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
    }

    @PostConstruct
    void seed() {
        if (findAll().isEmpty()) {
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
        if (jdbcTemplate != null) {
            return saveJdbc(problem);
        }
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
        if (jdbcTemplate != null) {
            List<Problem> result = jdbcTemplate.query("select * from problem where id = ?", mapper(), id);
            return result.stream().findFirst();
        }
        return Optional.ofNullable(problems.get(id));
    }

    public List<Problem> findAll() {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query("select * from problem order by id", mapper());
        }
        return new ArrayList<>(problems.values());
    }

    private Problem saveJdbc(Problem problem) {
        Instant now = Instant.now();
        if (problem.getPublished() == null) {
            problem.setPublished(false);
        }
        if (problem.getTotalSubmit() == null) {
            problem.setTotalSubmit(0);
        }
        if (problem.getTotalAc() == null) {
            problem.setTotalAc(0);
        }
        if (problem.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        insert into problem(title, description, time_limit_ms, memory_limit_mb, difficulty,
                                            setter_id, published, total_submit, total_ac, created_at, updated_at)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, problem.getTitle());
                ps.setString(2, problem.getDescription());
                ps.setInt(3, problem.getTimeLimitMs());
                ps.setInt(4, problem.getMemoryLimitMb());
                ps.setString(5, problem.getDifficulty());
                ps.setLong(6, problem.getSetterId());
                ps.setBoolean(7, problem.getPublished());
                ps.setInt(8, problem.getTotalSubmit());
                ps.setInt(9, problem.getTotalAc());
                ps.setTimestamp(10, Timestamp.from(now));
                ps.setTimestamp(11, Timestamp.from(now));
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("problem insert did not return generated id");
            }
            problem.setId(key.longValue());
            problem.setCreatedAt(now);
        } else {
            jdbcTemplate.update("""
                    update problem
                    set title = ?, description = ?, time_limit_ms = ?, memory_limit_mb = ?, difficulty = ?,
                        setter_id = ?, published = ?, total_submit = ?, total_ac = ?, updated_at = ?
                    where id = ?
                    """,
                    problem.getTitle(),
                    problem.getDescription(),
                    problem.getTimeLimitMs(),
                    problem.getMemoryLimitMb(),
                    problem.getDifficulty(),
                    problem.getSetterId(),
                    problem.getPublished(),
                    problem.getTotalSubmit(),
                    problem.getTotalAc(),
                    Timestamp.from(now),
                    problem.getId());
        }
        problem.setUpdatedAt(now);
        jdbcTemplate.update("delete from problem_tag where problem_id = ?", problem.getId());
        for (String tag : problem.getTags()) {
            jdbcTemplate.update("insert into problem_tag(problem_id, tag) values (?, ?)", problem.getId(), tag);
        }
        jdbcTemplate.update("delete from testcase_manifest where problem_id = ?", problem.getId());
        for (TestCase tc : problem.getTestCases()) {
            jdbcTemplate.update("""
                    insert into testcase_manifest(problem_id, case_index, input_object, output_object, score)
                    values (?, ?, ?, ?, ?)
                    """, problem.getId(), tc.getCaseIndex(), tc.getInput(), tc.getExpectedOutput(), tc.getScore());
        }
        return problem;
    }

    private RowMapper<Problem> mapper() {
        return (rs, rowNum) -> {
            Long id = rs.getLong("id");
            return Problem.builder()
                    .id(id)
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .timeLimitMs(rs.getInt("time_limit_ms"))
                    .memoryLimitMb(rs.getInt("memory_limit_mb"))
                    .difficulty(rs.getString("difficulty"))
                    .setterId(rs.getLong("setter_id"))
                    .published(rs.getBoolean("published"))
                    .totalSubmit(rs.getInt("total_submit"))
                    .totalAc(rs.getInt("total_ac"))
                    .tags(loadTags(id))
                    .testCases(loadTestcases(id))
                    .createdAt(toInstant(rs.getTimestamp("created_at")))
                    .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                    .build();
        };
    }

    private List<String> loadTags(Long problemId) {
        return jdbcTemplate.query("select tag from problem_tag where problem_id = ? order by tag",
                (rs, rowNum) -> rs.getString("tag"),
                problemId);
    }

    private List<TestCase> loadTestcases(Long problemId) {
        return jdbcTemplate.query("""
                select case_index, input_object, output_object, score
                from testcase_manifest
                where problem_id = ?
                order by case_index
                """,
                (rs, rowNum) -> TestCase.builder()
                        .caseIndex(rs.getInt("case_index"))
                        .input(rs.getString("input_object"))
                        .expectedOutput(rs.getString("output_object"))
                        .score(rs.getInt("score"))
                        .build(),
                problemId);
    }

    private static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC);
    }
}

package com.judgemesh.submit.repository;

import com.judgemesh.submit.domain.Contest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ContestStore {
    private final AtomicLong ids = new AtomicLong(2000);
    private final ConcurrentHashMap<Long, Contest> contests = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public ContestStore(ObjectProvider<JdbcTemplate> jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
    }

    @PostConstruct
    void seed() {
        if (findAll().isEmpty()) {
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
        if (jdbcTemplate != null) {
            return saveJdbc(contest);
        }
        if (contest.getId() == null) {
            contest.setId(ids.incrementAndGet());
        }
        contests.put(contest.getId(), contest);
        return contest;
    }

    public Optional<Contest> findById(Long id) {
        if (jdbcTemplate != null) {
            List<Contest> result = jdbcTemplate.query("select * from contest where id = ?", mapper(), id);
            return result.stream().findFirst();
        }
        return Optional.ofNullable(contests.get(id));
    }

    public List<Contest> findAll() {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query("select * from contest order by start_time desc", mapper());
        }
        return contests.values().stream()
                .sorted(Comparator.comparing(Contest::getStartTime).reversed())
                .toList();
    }

    private Contest saveJdbc(Contest contest) {
        if (contest.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        insert into contest(title, description, start_time, end_time, freeze_min, created_by)
                        values (?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, contest.getTitle());
                ps.setString(2, contest.getDescription());
                ps.setTimestamp(3, Timestamp.from(contest.getStartTime()));
                ps.setTimestamp(4, Timestamp.from(contest.getEndTime()));
                ps.setInt(5, contest.getFreezeBeforeMin());
                ps.setLong(6, contest.getCreatedBy());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("contest insert did not return generated id");
            }
            contest.setId(key.longValue());
        } else {
            jdbcTemplate.update("""
                    update contest
                    set title = ?, description = ?, start_time = ?, end_time = ?, freeze_min = ?, created_by = ?
                    where id = ?
                    """,
                    contest.getTitle(),
                    contest.getDescription(),
                    Timestamp.from(contest.getStartTime()),
                    Timestamp.from(contest.getEndTime()),
                    contest.getFreezeBeforeMin(),
                    contest.getCreatedBy(),
                    contest.getId());
        }
        jdbcTemplate.update("delete from contest_problem where contest_id = ?", contest.getId());
        int seq = 1;
        for (Long problemId : contest.getProblemIds()) {
            jdbcTemplate.update("insert into contest_problem(contest_id, problem_id, seq) values (?, ?, ?)",
                    contest.getId(), problemId, seq++);
        }
        jdbcTemplate.update("delete from contest_register where contest_id = ?", contest.getId());
        for (Long userId : contest.getRegisteredUserIds()) {
            jdbcTemplate.update("insert into contest_register(contest_id, user_id) values (?, ?)",
                    contest.getId(), userId);
        }
        return contest;
    }

    private RowMapper<Contest> mapper() {
        return (rs, rowNum) -> {
            Long id = rs.getLong("id");
            return Contest.builder()
                    .id(id)
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .startTime(toInstant(rs.getTimestamp("start_time")))
                    .endTime(toInstant(rs.getTimestamp("end_time")))
                    .freezeBeforeMin(rs.getInt("freeze_min"))
                    .createdBy(rs.getLong("created_by"))
                    .problemIds(loadProblems(id))
                    .registeredUserIds(new HashSet<>(loadRegisteredUsers(id)))
                    .build();
        };
    }

    private List<Long> loadProblems(Long contestId) {
        return jdbcTemplate.query("select problem_id from contest_problem where contest_id = ? order by seq",
                (rs, rowNum) -> rs.getLong("problem_id"),
                contestId);
    }

    private List<Long> loadRegisteredUsers(Long contestId) {
        return jdbcTemplate.query("select user_id from contest_register where contest_id = ? order by registered_at",
                (rs, rowNum) -> rs.getLong("user_id"),
                contestId);
    }

    private static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC);
    }
}

package com.judgemesh.submit.repository;

import com.judgemesh.submit.domain.Submission;
import com.judgemesh.submit.domain.SubmissionStatus;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SubmissionStore {
    private final AtomicLong ids = new AtomicLong(10000);
    private final ConcurrentHashMap<Long, Submission> submissions = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public SubmissionStore(ObjectProvider<JdbcTemplate> jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
    }

    public Submission save(Submission submission) {
        if (jdbcTemplate != null) {
            return saveJdbc(submission);
        }
        if (submission.getId() == null) {
            submission.setId(ids.incrementAndGet());
            submission.setSubmittedAt(Instant.now());
        }
        submissions.put(submission.getId(), submission);
        return submission;
    }

    public Optional<Submission> findById(Long id) {
        if (jdbcTemplate != null) {
            List<Submission> result = jdbcTemplate.query("select * from submission where id = ?", mapper(), id);
            return result.stream().findFirst();
        }
        return Optional.ofNullable(submissions.get(id));
    }

    public List<Submission> findByUser(Long userId) {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query(
                    "select * from submission where user_id = ? order by submitted_at desc",
                    mapper(),
                    userId);
        }
        return submissions.values().stream()
                .filter(s -> s.getUserId().equals(userId))
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .toList();
    }

    public List<Submission> findAll() {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query("select * from submission order by submitted_at desc", mapper());
        }
        return submissions.values().stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .toList();
    }

    private Submission saveJdbc(Submission submission) {
        Instant now = Instant.now();
        if (submission.getId() == null) {
            if (submission.getSubmittedAt() == null) {
                submission.setSubmittedAt(now);
            }
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        insert into submission(user_id, problem_id, contest_id, language, code, code_length, status,
                                               score, time_used_ms, memory_used_kb, judge_message, judged_by_worker,
                                               submitted_at, judged_at)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                bindSubmission(ps, submission);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("submission insert did not return generated id");
            }
            submission.setId(key.longValue());
        } else {
            jdbcTemplate.update("""
                    update submission
                    set user_id = ?, problem_id = ?, contest_id = ?, language = ?, code = ?, code_length = ?,
                        status = ?, score = ?, time_used_ms = ?, memory_used_kb = ?, judge_message = ?,
                        judged_by_worker = ?, submitted_at = ?, judged_at = ?
                    where id = ?
                    """, ps -> {
                bindSubmission(ps, submission);
                ps.setLong(15, submission.getId());
            });
        }
        return submission;
    }

    private static void bindSubmission(PreparedStatement ps, Submission submission) throws java.sql.SQLException {
        ps.setLong(1, submission.getUserId());
        ps.setLong(2, submission.getProblemId());
        if (submission.getContestId() == null) {
            ps.setObject(3, null);
        } else {
            ps.setLong(3, submission.getContestId());
        }
        ps.setString(4, submission.getLanguage());
        ps.setString(5, submission.getCode());
        ps.setInt(6, submission.getCodeLength());
        ps.setString(7, submission.getStatus().name());
        ps.setInt(8, submission.getScore());
        ps.setObject(9, submission.getTimeUsedMs());
        ps.setObject(10, submission.getMemoryUsedKb());
        ps.setString(11, submission.getJudgeMessage());
        ps.setString(12, submission.getJudgedByWorker());
        ps.setTimestamp(13, Timestamp.from(submission.getSubmittedAt()));
        ps.setTimestamp(14, submission.getJudgedAt() == null ? null : Timestamp.from(submission.getJudgedAt()));
    }

    private RowMapper<Submission> mapper() {
        return (rs, rowNum) -> Submission.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .problemId(rs.getLong("problem_id"))
                .contestId(nullableLong(rs, "contest_id"))
                .language(rs.getString("language"))
                .code(rs.getString("code"))
                .codeLength(rs.getInt("code_length"))
                .status(SubmissionStatus.valueOf(rs.getString("status")))
                .score(rs.getInt("score"))
                .timeUsedMs(nullableInt(rs, "time_used_ms"))
                .memoryUsedKb(nullableInt(rs, "memory_used_kb"))
                .judgeMessage(rs.getString("judge_message"))
                .judgedByWorker(rs.getString("judged_by_worker"))
                .submittedAt(toInstant(rs.getTimestamp("submitted_at")))
                .judgedAt(toInstant(rs.getTimestamp("judged_at")))
                .build();
    }

    private static Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer nullableInt(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC);
    }
}

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
        normalizeTransientFields(submission);
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

    public List<Submission> findTimedOutJudging(Instant cutoff, int limit) {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query("""
                            select * from submission
                            where status = ? and judging_started_at is not null and judging_started_at <= ?
                            order by judging_started_at asc
                            limit ?
                            """,
                    mapper(),
                    SubmissionStatus.JUDGING.name(),
                    Timestamp.from(cutoff),
                    limit);
        }
        return submissions.values().stream()
                .filter(submission -> submission.getStatus() == SubmissionStatus.JUDGING)
                .filter(submission -> submission.getJudgingStartedAt() != null)
                .filter(submission -> !submission.getJudgingStartedAt().isAfter(cutoff))
                .sorted(Comparator.comparing(Submission::getJudgingStartedAt))
                .limit(limit)
                .toList();
    }

    public Optional<Submission> claimTimedOutJudgingForRetry(
            Long id,
            Instant expectedJudgingStartedAt,
            int expectedRetryCount,
            String judgeMessage) {
        Instant now = Instant.now();
        if (jdbcTemplate != null) {
            int updated = jdbcTemplate.update("""
                            update submission
                            set judge_retry_count = ?, judging_started_at = ?, judge_message = ?, judged_by_worker = ?
                            where id = ? and status = ? and judging_started_at = ? and judge_retry_count = ?
                            """,
                    expectedRetryCount + 1,
                    Timestamp.from(now),
                    judgeMessage,
                    null,
                    id,
                    SubmissionStatus.JUDGING.name(),
                    Timestamp.from(expectedJudgingStartedAt),
                    expectedRetryCount);
            return updated == 0 ? Optional.empty() : findById(id);
        }

        Submission submission = submissions.get(id);
        if (submission == null || submission.getStatus() != SubmissionStatus.JUDGING) {
            return Optional.empty();
        }
        if (!expectedJudgingStartedAt.equals(submission.getJudgingStartedAt())) {
            return Optional.empty();
        }
        if (expectedRetryCount != currentRetryCount(submission)) {
            return Optional.empty();
        }
        submission.setJudgeRetryCount(expectedRetryCount + 1);
        submission.setJudgingStartedAt(now);
        submission.setJudgeMessage(judgeMessage);
        submission.setJudgedByWorker(null);
        submissions.put(id, submission);
        return Optional.of(submission);
    }

    public Optional<Submission> markTimedOutJudgingAsSystemError(
            Long id,
            Instant expectedJudgingStartedAt,
            int expectedRetryCount,
            String judgeMessage) {
        Instant now = Instant.now();
        if (jdbcTemplate != null) {
            int updated = jdbcTemplate.update("""
                            update submission
                            set status = ?, score = ?, time_used_ms = ?, memory_used_kb = ?, judge_message = ?,
                                judged_at = ?, judging_started_at = ?, active_attempt_id = ?
                            where id = ? and status = ? and judging_started_at = ? and judge_retry_count = ?
                            """,
                    SubmissionStatus.SE.name(),
                    0,
                    0,
                    0,
                    judgeMessage,
                    Timestamp.from(now),
                    null,
                    null,
                    id,
                    SubmissionStatus.JUDGING.name(),
                    Timestamp.from(expectedJudgingStartedAt),
                    expectedRetryCount);
            return updated == 0 ? Optional.empty() : findById(id);
        }

        Submission submission = submissions.get(id);
        if (submission == null || submission.getStatus() != SubmissionStatus.JUDGING) {
            return Optional.empty();
        }
        if (!expectedJudgingStartedAt.equals(submission.getJudgingStartedAt())) {
            return Optional.empty();
        }
        if (expectedRetryCount != currentRetryCount(submission)) {
            return Optional.empty();
        }
        submission.setStatus(SubmissionStatus.SE);
        submission.setScore(0);
        submission.setTimeUsedMs(0);
        submission.setMemoryUsedKb(0);
        submission.setJudgeMessage(judgeMessage);
        submission.setJudgedAt(now);
        submission.setJudgingStartedAt(null);
        submission.setActiveAttemptId(null);
        submissions.put(id, submission);
        return Optional.of(submission);
    }

    private Submission saveJdbc(Submission submission) {
        Instant now = Instant.now();
        normalizeTransientFields(submission);
        if (submission.getId() == null) {
            if (submission.getSubmittedAt() == null) {
                submission.setSubmittedAt(now);
            }
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        insert into submission(user_id, problem_id, contest_id, language, code, code_length, status,
                                               score, time_used_ms, memory_used_kb, judge_message, judged_by_worker,
                                               active_attempt_id, submitted_at, judging_started_at, judged_at, judge_retry_count)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                        judged_by_worker = ?, active_attempt_id = ?, submitted_at = ?, judging_started_at = ?, judged_at = ?,
                        judge_retry_count = ?
                    where id = ?
                    """, ps -> {
                bindSubmission(ps, submission);
                ps.setLong(18, submission.getId());
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
        ps.setString(13, submission.getActiveAttemptId());
        ps.setTimestamp(14, Timestamp.from(submission.getSubmittedAt()));
        ps.setTimestamp(15, submission.getJudgingStartedAt() == null ? null : Timestamp.from(submission.getJudgingStartedAt()));
        ps.setTimestamp(16, submission.getJudgedAt() == null ? null : Timestamp.from(submission.getJudgedAt()));
        ps.setInt(17, currentRetryCount(submission));
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
                .activeAttemptId(rs.getString("active_attempt_id"))
                .submittedAt(toInstant(rs.getTimestamp("submitted_at")))
                .judgingStartedAt(toInstant(rs.getTimestamp("judging_started_at")))
                .judgedAt(toInstant(rs.getTimestamp("judged_at")))
                .judgeRetryCount(nullableInt(rs, "judge_retry_count"))
                .build();
    }

    private static void normalizeTransientFields(Submission submission) {
        if (submission.getJudgeRetryCount() == null) {
            submission.setJudgeRetryCount(0);
        }
    }

    private static int currentRetryCount(Submission submission) {
        return submission.getJudgeRetryCount() == null ? 0 : submission.getJudgeRetryCount();
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

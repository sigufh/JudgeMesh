package com.judgemesh.submit.repository;

import com.judgemesh.api.enumx.LanguageType;
import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.model.SubmissionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnBean(JdbcTemplate.class)
@RequiredArgsConstructor
public class JdbcSubmitStateRepository implements SubmitStateRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SubmissionRecord> submissionMapper = (rs, rowNum) -> SubmissionRecord.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .problemId(rs.getLong("problem_id"))
            .contestId(nullableLong(rs, "contest_id"))
            .language(LanguageType.fromValue(rs.getString("language")))
            .code(rs.getString("code"))
            .status(SubmitStatus.fromValue(rs.getString("status")))
            .score(rs.getInt("score"))
            .timeUsedMs(nullableInteger(rs, "time_used_ms"))
            .memoryUsedKb(nullableInteger(rs, "memory_used_kb"))
            .judgeMessage(rs.getString("judge_message"))
            .judgedByWorker(rs.getString("judged_by_worker"))
            .submittedAt(nullableInstant(rs, "submitted_at"))
            .judgedAt(nullableInstant(rs, "judged_at"))
            .build();

    private final RowMapper<ContestRecord> contestMapper = (rs, rowNum) -> {
        Long contestId = rs.getLong("id");
        ContestRecord contest = ContestRecord.builder()
                .id(contestId)
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .startTime(nullableInstant(rs, "start_time"))
                .endTime(nullableInstant(rs, "end_time"))
                .freezeBeforeMin(rs.getInt("freeze_min"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(nullableInstant(rs, "created_at"))
                .build();
        contest.getProblemIds().addAll(findProblemIds(contestId));
        contest.setRegisteredUserIds(findRegisteredUserIds(contestId));
        return contest;
    };

    @Override
    public SubmissionRecord saveSubmission(SubmissionRecord submission) {
        if (submission.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO submission (
                            user_id, problem_id, contest_id, language, code, code_length, status, score,
                            time_used_ms, memory_used_kb, judge_message, judged_by_worker, submitted_at, judged_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                bindSubmission(ps, submission);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                submission.setId(key.longValue());
            }
            return submission;
        }

        jdbcTemplate.update("""
                UPDATE submission
                   SET user_id = ?, problem_id = ?, contest_id = ?, language = ?, code = ?, code_length = ?,
                       status = ?, score = ?, time_used_ms = ?, memory_used_kb = ?, judge_message = ?,
                       judged_by_worker = ?, submitted_at = ?, judged_at = ?
                 WHERE id = ?
                """,
                submission.getUserId(),
                submission.getProblemId(),
                submission.getContestId(),
                languageValue(submission.getLanguage()),
                submission.getCode(),
                submission.getCode() == null ? 0 : submission.getCode().length(),
                statusValue(submission.getStatus()),
                submission.getScore() == null ? 0 : submission.getScore(),
                submission.getTimeUsedMs(),
                submission.getMemoryUsedKb(),
                submission.getJudgeMessage(),
                submission.getJudgedByWorker(),
                timestamp(submission.getSubmittedAt()),
                timestamp(submission.getJudgedAt()),
                submission.getId());
        return submission;
    }

    @Override
    public Optional<SubmissionRecord> findSubmission(long id) {
        List<SubmissionRecord> records = jdbcTemplate.query(
                "SELECT * FROM submission WHERE id = ?",
                submissionMapper,
                id);
        return records.stream().findFirst();
    }

    @Override
    public List<SubmissionRecord> findSubmissionsByUser(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM submission WHERE user_id = ? ORDER BY submitted_at DESC",
                submissionMapper,
                userId);
    }

    @Override
    public long countSubmissionsByStatus(SubmitStatus status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM submission WHERE status = ?",
                Long.class,
                statusValue(status));
        return count == null ? 0L : count;
    }

    @Override
    @Transactional
    public ContestRecord saveContest(ContestRecord contest) {
        Instant createdAt = contest.getCreatedAt() == null ? Instant.now() : contest.getCreatedAt();
        contest.setCreatedAt(createdAt);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO contest (title, description, start_time, end_time, freeze_min, created_by, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, contest.getTitle());
            ps.setString(2, contest.getDescription());
            ps.setTimestamp(3, timestamp(contest.getStartTime()));
            ps.setTimestamp(4, timestamp(contest.getEndTime()));
            ps.setInt(5, contest.getFreezeBeforeMin() == null ? 30 : contest.getFreezeBeforeMin());
            ps.setLong(6, contest.getCreatedBy());
            ps.setTimestamp(7, timestamp(createdAt));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            contest.setId(key.longValue());
        }
        replaceContestProblems(contest);
        return findContest(contest.getId()).orElse(contest);
    }

    @Override
    public Optional<ContestRecord> findContest(long id) {
        List<ContestRecord> records = jdbcTemplate.query(
                "SELECT * FROM contest WHERE id = ?",
                contestMapper,
                id);
        return records.stream().findFirst();
    }

    @Override
    public List<ContestRecord> listContests() {
        return jdbcTemplate.query("SELECT * FROM contest ORDER BY created_at DESC", contestMapper);
    }

    @Override
    @Transactional
    public ContestRecord updateContest(ContestRecord contest) {
        jdbcTemplate.update("""
                UPDATE contest
                   SET title = ?, description = ?, start_time = ?, end_time = ?, freeze_min = ?
                 WHERE id = ?
                """,
                contest.getTitle(),
                contest.getDescription(),
                timestamp(contest.getStartTime()),
                timestamp(contest.getEndTime()),
                contest.getFreezeBeforeMin() == null ? 30 : contest.getFreezeBeforeMin(),
                contest.getId());
        replaceContestProblems(contest);
        return findContest(contest.getId()).orElse(contest);
    }

    @Override
    public boolean registerContest(long contestId, long userId) {
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO contest_register (contest_id, user_id, registered_at)
                VALUES (?, ?, ?)
                """, contestId, userId, timestamp(Instant.now()));
        return inserted > 0;
    }

    private void bindSubmission(PreparedStatement ps, SubmissionRecord submission) throws java.sql.SQLException {
        ps.setLong(1, submission.getUserId());
        ps.setLong(2, submission.getProblemId());
        if (submission.getContestId() == null) {
            ps.setObject(3, null);
        } else {
            ps.setLong(3, submission.getContestId());
        }
        ps.setString(4, languageValue(submission.getLanguage()));
        ps.setString(5, submission.getCode());
        ps.setInt(6, submission.getCode() == null ? 0 : submission.getCode().length());
        ps.setString(7, statusValue(submission.getStatus()));
        ps.setInt(8, submission.getScore() == null ? 0 : submission.getScore());
        ps.setObject(9, submission.getTimeUsedMs());
        ps.setObject(10, submission.getMemoryUsedKb());
        ps.setString(11, submission.getJudgeMessage());
        ps.setString(12, submission.getJudgedByWorker());
        ps.setTimestamp(13, timestamp(submission.getSubmittedAt()));
        ps.setTimestamp(14, timestamp(submission.getJudgedAt()));
    }

    private void replaceContestProblems(ContestRecord contest) {
        jdbcTemplate.update("DELETE FROM contest_problem WHERE contest_id = ?", contest.getId());
        for (int i = 0; i < contest.getProblemIds().size(); i++) {
            jdbcTemplate.update(
                    "INSERT INTO contest_problem (contest_id, problem_id, seq) VALUES (?, ?, ?)",
                    contest.getId(),
                    contest.getProblemIds().get(i),
                    i + 1);
        }
    }

    private List<Long> findProblemIds(long contestId) {
        return jdbcTemplate.queryForList(
                "SELECT problem_id FROM contest_problem WHERE contest_id = ? ORDER BY seq ASC",
                Long.class,
                contestId);
    }

    private Set<Long> findRegisteredUserIds(long contestId) {
        Set<Long> users = ConcurrentHashMap.newKeySet();
        users.addAll(jdbcTemplate.queryForList(
                "SELECT user_id FROM contest_register WHERE contest_id = ?",
                Long.class,
                contestId));
        return users;
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant nullableInstant(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Integer nullableInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static String languageValue(LanguageType language) {
        return language == null ? null : language.toWireValue();
    }

    private static String statusValue(SubmitStatus status) {
        return status == null ? null : status.toWireValue();
    }
}

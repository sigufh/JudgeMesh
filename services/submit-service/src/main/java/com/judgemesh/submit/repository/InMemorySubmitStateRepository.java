package com.judgemesh.submit.repository;

import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.model.SubmissionRecord;
import com.judgemesh.api.enumx.SubmitStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnMissingBean(JdbcTemplate.class)
public class InMemorySubmitStateRepository implements SubmitStateRepository {

    private final AtomicLong submissionSeq = new AtomicLong(1_000_000L);
    private final AtomicLong contestSeq = new AtomicLong(100_000L);
    private final ConcurrentMap<Long, SubmissionRecord> submissions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ContestRecord> contests = new ConcurrentHashMap<>();

    @Override
    public SubmissionRecord saveSubmission(SubmissionRecord submission) {
        if (submission.getId() == null) {
            submission.setId(submissionSeq.incrementAndGet());
        }
        submissions.put(submission.getId(), submission);
        return submission;
    }

    @Override
    public Optional<SubmissionRecord> findSubmission(long id) {
        return Optional.ofNullable(submissions.get(id));
    }

    @Override
    public List<SubmissionRecord> findSubmissionsByUser(long userId) {
        return submissions.values().stream()
                .filter(submission -> submission.getUserId() != null && submission.getUserId().equals(userId))
                .sorted(Comparator.comparing(SubmissionRecord::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();
    }

    @Override
    public long countSubmissionsByStatus(SubmitStatus status) {
        return submissions.values().stream()
                .filter(submission -> submission.getStatus() == status)
                .count();
    }

    @Override
    public ContestRecord saveContest(ContestRecord contest) {
        if (contest.getId() == null) {
            contest.setId(contestSeq.incrementAndGet());
            contest.setCreatedAt(Instant.now());
        }
        contests.put(contest.getId(), contest);
        return contest;
    }

    @Override
    public Optional<ContestRecord> findContest(long id) {
        return Optional.ofNullable(contests.get(id));
    }

    @Override
    public List<ContestRecord> listContests() {
        return contests.values().stream()
                .sorted(Comparator.comparing(ContestRecord::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();
    }

    @Override
    public ContestRecord updateContest(ContestRecord contest) {
        contests.put(contest.getId(), contest);
        return contest;
    }

    @Override
    public boolean registerContest(long contestId, long userId) {
        ContestRecord contest = contests.get(contestId);
        if (contest == null) {
            return false;
        }
        return contest.getRegisteredUserIds().add(userId);
    }
}

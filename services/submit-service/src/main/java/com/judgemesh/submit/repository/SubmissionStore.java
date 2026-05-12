package com.judgemesh.submit.repository;

import com.judgemesh.submit.domain.Submission;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SubmissionStore {
    private final AtomicLong ids = new AtomicLong(10000);
    private final ConcurrentHashMap<Long, Submission> submissions = new ConcurrentHashMap<>();

    public Submission save(Submission submission) {
        if (submission.getId() == null) {
            submission.setId(ids.incrementAndGet());
            submission.setSubmittedAt(Instant.now());
        }
        submissions.put(submission.getId(), submission);
        return submission;
    }

    public Optional<Submission> findById(Long id) {
        return Optional.ofNullable(submissions.get(id));
    }

    public List<Submission> findByUser(Long userId) {
        return submissions.values().stream()
                .filter(s -> s.getUserId().equals(userId))
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .toList();
    }

    public List<Submission> findAll() {
        return submissions.values().stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .toList();
    }
}

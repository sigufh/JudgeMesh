package com.judgemesh.submit.repository;

import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.model.SubmissionRecord;

import java.util.List;
import java.util.Optional;

public interface SubmitStateRepository {

    SubmissionRecord saveSubmission(SubmissionRecord submission);

    Optional<SubmissionRecord> findSubmission(long id);

    List<SubmissionRecord> findSubmissionsByUser(long userId);

    ContestRecord saveContest(ContestRecord contest);

    Optional<ContestRecord> findContest(long id);

    List<ContestRecord> listContests();

    ContestRecord updateContest(ContestRecord contest);

    boolean registerContest(long contestId, long userId);
}

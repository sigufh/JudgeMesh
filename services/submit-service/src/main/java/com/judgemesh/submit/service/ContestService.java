package com.judgemesh.submit.service;

import com.judgemesh.api.dto.ContestDTO;
import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.api.dto.ContestRankEntryDTO;
import com.judgemesh.api.dto.ContestUpsertRequest;
import com.judgemesh.api.enumx.ContestStatus;
import com.judgemesh.api.error.ErrorCode;
import com.judgemesh.submit.error.DomainException;
import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.repository.SubmitStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final SubmitStateRepository repository;
    private final LeaderboardService leaderboardService;

    public ContestDTO createContest(long creatorId, ContestUpsertRequest request) {
        ContestRecord contest = ContestRecord.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .freezeBeforeMin(request.getFreezeBeforeMin() == null ? 30 : request.getFreezeBeforeMin())
                .createdBy(creatorId)
                .build();
        contest.getProblemIds().addAll(safeProblemIds(request.getProblemIds()));
        return toDto(repository.saveContest(contest));
    }

    public ContestDTO updateContest(long contestId, ContestUpsertRequest request) {
        ContestRecord contest = getContestRecord(contestId);
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        contest.setFreezeBeforeMin(request.getFreezeBeforeMin() == null ? 30 : request.getFreezeBeforeMin());
        contest.getProblemIds().clear();
        contest.getProblemIds().addAll(safeProblemIds(request.getProblemIds()));
        return toDto(repository.updateContest(contest));
    }

    public ContestDTO getContest(long contestId) {
        return toDto(getContestRecord(contestId));
    }

    public List<ContestDTO> listContests() {
        return repository.listContests().stream().map(this::toDto).toList();
    }

    public ContestDTO registerContest(long contestId, long userId) {
        ContestRecord contest = getContestRecord(contestId);
        if (isEnded(contest, Instant.now())) {
            throw new DomainException(ErrorCode.CONTEST_ENDED);
        }
        repository.registerContest(contestId, userId);
        return toDto(contest);
    }

    public ContestRankDTO contestRank(long contestId) {
        ContestRecord contest = getContestRecord(contestId);
        Instant now = Instant.now();
        List<ContestRankEntryDTO> entries = new ArrayList<>(leaderboardService.contestRank(contestId));
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank((long) i + 1);
        }
        return ContestRankDTO.builder()
                .contestId(contestId)
                .status(contestStatus(contest, now))
                .frozen(isFrozen(contest, now))
                .frozenAt(frozenAt(contest))
                .entries(entries)
                .build();
    }

    public void assertSubmissionAllowed(long contestId, long userId, long problemId) {
        ContestRecord contest = getContestRecord(contestId);
        Instant now = Instant.now();
        if (now.isBefore(contest.getStartTime())) {
            throw new DomainException(ErrorCode.CONTEST_NOT_STARTED);
        }
        if (now.isAfter(contest.getEndTime())) {
            throw new DomainException(ErrorCode.CONTEST_ENDED);
        }
        if (!contest.getRegisteredUserIds().contains(userId)) {
            throw new DomainException(ErrorCode.CONTEST_NOT_REGISTERED);
        }
        if (!contest.getProblemIds().contains(problemId)) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "problem is not in this contest");
        }
    }

    public ContestRecord getContestRecord(long contestId) {
        return repository.findContest(contestId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONTEST_NOT_FOUND, "contestId=" + contestId));
    }

    public boolean isContestFrozen(ContestRecord contest) {
        return isFrozen(contest, Instant.now());
    }

    private ContestDTO toDto(ContestRecord contest) {
        Instant now = Instant.now();
        return ContestDTO.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .freezeBeforeMin(contest.getFreezeBeforeMin())
                .createdBy(contest.getCreatedBy())
                .createdAt(contest.getCreatedAt())
                .problemIds(List.copyOf(contest.getProblemIds()))
                .registeredCount((long) contest.getRegisteredUserIds().size())
                .status(contestStatus(contest, now))
                .build();
    }

    private ContestStatus contestStatus(ContestRecord contest, Instant now) {
        if (now.isBefore(contest.getStartTime())) {
            return ContestStatus.UPCOMING;
        }
        if (now.isAfter(contest.getEndTime())) {
            return ContestStatus.ENDED;
        }
        if (isFrozen(contest, now)) {
            return ContestStatus.FROZEN;
        }
        return ContestStatus.RUNNING;
    }

    private boolean isEnded(ContestRecord contest, Instant now) {
        return now.isAfter(contest.getEndTime());
    }

    private boolean isFrozen(ContestRecord contest, Instant now) {
        Instant frozenAt = frozenAt(contest);
        return frozenAt != null && !now.isBefore(frozenAt) && now.isBefore(contest.getEndTime());
    }

    private Instant frozenAt(ContestRecord contest) {
        Integer freezeBeforeMin = contest.getFreezeBeforeMin();
        if (freezeBeforeMin == null || freezeBeforeMin <= 0 || contest.getEndTime() == null) {
            return null;
        }
        return contest.getEndTime().minus(Duration.ofMinutes(freezeBeforeMin));
    }

    private List<Long> safeProblemIds(List<Long> problemIds) {
        return problemIds == null ? List.of() : problemIds;
    }
}

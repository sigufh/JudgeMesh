package com.judgemesh.submit.service;

import com.judgemesh.api.dto.ContestDTO;
import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.api.dto.ContestRankEntryDTO;
import com.judgemesh.api.dto.ContestUpsertRequest;
import com.judgemesh.api.dto.RankEntryDTO;
import com.judgemesh.api.enumx.ContestStatus;
import com.judgemesh.submit.domain.Contest;
import com.judgemesh.submit.repository.ContestStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ContestService {
    private final ContestStore contestStore;
    private final RankingService rankingService;

    public ContestService(ContestStore contestStore, RankingService rankingService) {
        this.contestStore = contestStore;
        this.rankingService = rankingService;
    }

    public ContestDTO createContest(Long creatorId, ContestUpsertRequest request) {
        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .freezeBeforeMin(request.getFreezeBeforeMin() == null ? 30 : request.getFreezeBeforeMin())
                .createdBy(creatorId)
                .problemIds(safeProblemIds(request.getProblemIds()))
                .build();
        return toDto(contestStore.save(contest), creatorId);
    }

    public ContestDTO updateContest(Long contestId, ContestUpsertRequest request) {
        Contest contest = get(contestId);
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        contest.setFreezeBeforeMin(request.getFreezeBeforeMin() == null ? 30 : request.getFreezeBeforeMin());
        contest.setProblemIds(safeProblemIds(request.getProblemIds()));
        return toDto(contestStore.save(contest), null);
    }

    public ContestDTO getContest(Long contestId) {
        return toDto(get(contestId), null);
    }

    public List<ContestDTO> listContests() {
        return list(null);
    }

    public ContestDTO registerContest(Long contestId, Long userId) {
        return register(contestId, userId);
    }

    public ContestRankDTO contestRank(Long contestId) {
        Contest contest = get(contestId);
        Instant now = Instant.now();
        List<ContestRankEntryDTO> entries = rankingService.contestRank(contestId).stream()
                .map(this::toContestRankEntry)
                .toList();
        return ContestRankDTO.builder()
                .contestId(contestId)
                .status(contestStatus(contest, now))
                .frozen(isFrozen(contest, now))
                .frozenAt(frozenAt(contest))
                .entries(entries)
                .build();
    }

    public List<ContestDTO> list(Long userId) {
        return contestStore.findAll().stream().map(contest -> toDto(contest, userId)).toList();
    }

    public Contest get(Long id) {
        return contestStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contest not found"));
    }

    public ContestDTO register(Long contestId, Long userId) {
        Contest contest = get(contestId);
        Instant now = Instant.now();
        if (now.isAfter(contest.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contest ended");
        }
        contest.getRegisteredUserIds().add(userId);
        contestStore.save(contest);
        return toDto(contest, userId);
    }

    public List<RankEntryDTO> rank(Long contestId) {
        get(contestId);
        return rankingService.contestRank(contestId);
    }

    public void assertSubmissionAllowed(Long contestId, Long userId, Long problemId) {
        Contest contest = get(contestId);
        Instant now = Instant.now();
        if (now.isBefore(contest.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contest not started");
        }
        if (now.isAfter(contest.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contest ended");
        }
        if (!contest.getRegisteredUserIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "contest not registered");
        }
        if (!contest.getProblemIds().contains(problemId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "problem is not in this contest");
        }
    }

    public boolean isContestFrozen(Contest contest) {
        return isFrozen(contest, Instant.now());
    }

    public ContestDTO toDto(Contest contest, Long userId) {
        Instant now = Instant.now();
        return ContestDTO.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .freezeBeforeMin(contest.getFreezeBeforeMin())
                .createdBy(contest.getCreatedBy())
                .problemIds(List.copyOf(contest.getProblemIds()))
                .registeredCount((long) contest.getRegisteredUserIds().size())
                .status(contestStatus(contest, now))
                .frozen(isFrozen(contest, now))
                .registered(userId != null && contest.getRegisteredUserIds().contains(userId))
                .build();
    }

    private ContestRankEntryDTO toContestRankEntry(RankEntryDTO entry) {
        return ContestRankEntryDTO.builder()
                .rank(entry.getRank() == null ? null : entry.getRank().longValue())
                .userId(entry.getUserId())
                .solved(entry.getSolved())
                .penaltyMinutes(entry.getPenaltyMinutes())
                .score(entry.getScore() == null ? null : entry.getScore().intValue())
                .build();
    }

    private ContestStatus contestStatus(Contest contest, Instant now) {
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

    private boolean isFrozen(Contest contest, Instant now) {
        Instant frozenAt = frozenAt(contest);
        return frozenAt != null && !now.isBefore(frozenAt) && now.isBefore(contest.getEndTime());
    }

    private Instant frozenAt(Contest contest) {
        Integer freezeBeforeMin = contest.getFreezeBeforeMin();
        if (freezeBeforeMin == null || freezeBeforeMin <= 0 || contest.getEndTime() == null) {
            return null;
        }
        return contest.getEndTime().minus(Duration.ofMinutes(freezeBeforeMin));
    }

    private List<Long> safeProblemIds(List<Long> problemIds) {
        return problemIds == null ? List.of() : List.copyOf(problemIds);
    }
}

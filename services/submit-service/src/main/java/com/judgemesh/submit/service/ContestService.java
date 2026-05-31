package com.judgemesh.submit.service;

import com.judgemesh.api.dto.ContestDTO;
import com.judgemesh.api.dto.RankEntryDTO;
import com.judgemesh.submit.domain.Contest;
import com.judgemesh.submit.repository.ContestStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<ContestDTO> list(Long userId) {
        return contestStore.findAll().stream().map(contest -> toDto(contest, userId)).toList();
    }

    public ContestDTO create(ContestCommand command) {
        Contest contest = Contest.builder()
                .title(required(command.title(), "title"))
                .description(command.description() == null ? "" : command.description())
                .startTime(required(command.startTime(), "startTime"))
                .endTime(required(command.endTime(), "endTime"))
                .freezeBeforeMin(command.freezeBeforeMin() == null ? 0 : command.freezeBeforeMin())
                .createdBy(command.createdBy() == null ? 1001L : command.createdBy())
                .problemIds(command.problemIds() == null ? List.of() : command.problemIds())
                .build();
        validateTime(contest);
        return toDto(contestStore.save(contest), null);
    }

    public ContestDTO update(Long id, ContestCommand command) {
        Contest contest = get(id);
        if (command.title() != null) {
            contest.setTitle(command.title());
        }
        if (command.description() != null) {
            contest.setDescription(command.description());
        }
        if (command.startTime() != null) {
            contest.setStartTime(command.startTime());
        }
        if (command.endTime() != null) {
            contest.setEndTime(command.endTime());
        }
        if (command.freezeBeforeMin() != null) {
            contest.setFreezeBeforeMin(command.freezeBeforeMin());
        }
        if (command.createdBy() != null) {
            contest.setCreatedBy(command.createdBy());
        }
        if (command.problemIds() != null) {
            contest.setProblemIds(command.problemIds());
        }
        validateTime(contest);
        return toDto(contestStore.save(contest), null);
    }

    public void delete(Long id) {
        if (!contestStore.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "contest not found");
        }
    }

    public Contest get(Long id) {
        return contestStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contest not found"));
    }

    public ContestDTO register(Long contestId, Long userId) {
        Contest contest = get(contestId);
        contest.getRegisteredUserIds().add(userId);
        contestStore.save(contest);
        return toDto(contest, userId);
    }

    public List<RankEntryDTO> rank(Long contestId) {
        get(contestId);
        return rankingService.contestRank(contestId);
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
                .problemIds(contest.getProblemIds())
                .frozen(contest.frozen(now))
                .registered(userId != null && contest.getRegisteredUserIds().contains(userId))
                .build();
    }

    private static <T> T required(T value, String field) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private static void validateTime(Contest contest) {
        if (contest.getStartTime() == null || contest.getEndTime() == null
                || !contest.getStartTime().isBefore(contest.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
        if (contest.getFreezeBeforeMin() == null || contest.getFreezeBeforeMin() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "freezeBeforeMin must be non-negative");
        }
    }

    public record ContestCommand(
            String title,
            String description,
            Instant startTime,
            Instant endTime,
            Integer freezeBeforeMin,
            Long createdBy,
            List<Long> problemIds) {
    }
}

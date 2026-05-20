package com.judgemesh.submit.service;

  import com.judgemesh.api.dto.ContestDTO;
  import com.judgemesh.api.dto.RankEntryDTO;
  import com.judgemesh.api.enumx.ContestStatus;
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

      public Contest get(Long id) {
          return contestStore.findById(id)
                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contest not found"));
      }

      public ContestDTO register(Long contestId, Long userId) {
          Contest contest = get(contestId);
          if (Instant.now().isAfter(contest.getEndTime())) {
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
                  .registeredCount((long) contest.getRegisteredUserIds().size())
                  .status(contestStatus(contest, now))
                  .frozen(contest.frozen(now))
                  .registered(userId != null && contest.getRegisteredUserIds().contains(userId))
                  .build();
      }

      private ContestStatus contestStatus(Contest contest, Instant now) {
          if (now.isBefore(contest.getStartTime())) {
              return ContestStatus.UPCOMING;
          }
          if (now.isAfter(contest.getEndTime())) {
              return ContestStatus.ENDED;
          }
          if (contest.frozen(now)) {
              return ContestStatus.FROZEN;
          }
          return ContestStatus.RUNNING;
      }
  }
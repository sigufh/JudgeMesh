package com.judgemesh.problem.service;

import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.problem.domain.Problem;
import com.judgemesh.problem.domain.TestCase;
import com.judgemesh.problem.repository.ProblemStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ProblemService {
    private final ProblemStore store;

    public ProblemService(ProblemStore store) {
        this.store = store;
    }

    public List<ProblemDTO> list(String q, String tag, String difficulty, Boolean includeDraft, int page, int size) {
        String keyword = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String tagFilter = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
        String difficultyFilter = difficulty == null ? "" : difficulty.trim().toUpperCase(Locale.ROOT);
        int skip = Math.max(page, 0) * Math.max(size, 1);
        return store.findAll().stream()
                .filter(problem -> Boolean.TRUE.equals(includeDraft) || Boolean.TRUE.equals(problem.getPublished()))
                .filter(problem -> keyword.isEmpty()
                        || problem.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
                        || problem.getDescription().toLowerCase(Locale.ROOT).contains(keyword))
                .filter(problem -> tagFilter.isEmpty()
                        || problem.getTags().stream().anyMatch(t -> t.toLowerCase(Locale.ROOT).equals(tagFilter)))
                .filter(problem -> difficultyFilter.isEmpty() || problem.getDifficulty().equalsIgnoreCase(difficultyFilter))
                .sorted(Comparator.comparing(Problem::getId))
                .skip(skip)
                .limit(Math.max(size, 1))
                .map(this::toDto)
                .toList();
    }

    public Problem get(Long id) {
        return store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "problem not found"));
    }

    public Problem create(CreateProblemCommand command) {
        Problem problem = Problem.builder()
                .title(command.title())
                .description(command.description())
                .difficulty(defaultString(command.difficulty(), "EASY").toUpperCase(Locale.ROOT))
                .timeLimitMs(command.timeLimitMs() == null ? 1000 : command.timeLimitMs())
                .memoryLimitMb(command.memoryLimitMb() == null ? 256 : command.memoryLimitMb())
                .setterId(command.setterId() == null ? 1001L : command.setterId())
                .published(Boolean.TRUE.equals(command.published()))
                .tags(command.tags() == null ? List.of() : command.tags())
                .testCases(command.testCases() == null ? List.of() : command.testCases().stream()
                        .map(this::toCase)
                        .toList())
                .build();
        return store.save(problem);
    }

    public Problem update(Long id, CreateProblemCommand command) {
        Problem problem = get(id);
        if (command.title() != null) {
            problem.setTitle(command.title());
        }
        if (command.description() != null) {
            problem.setDescription(command.description());
        }
        if (command.difficulty() != null) {
            problem.setDifficulty(command.difficulty().toUpperCase(Locale.ROOT));
        }
        if (command.timeLimitMs() != null) {
            problem.setTimeLimitMs(command.timeLimitMs());
        }
        if (command.memoryLimitMb() != null) {
            problem.setMemoryLimitMb(command.memoryLimitMb());
        }
        if (command.published() != null) {
            problem.setPublished(command.published());
        }
        if (command.tags() != null) {
            problem.setTags(command.tags());
        }
        if (command.testCases() != null) {
            problem.setTestCases(command.testCases().stream().map(this::toCase).toList());
        }
        return store.save(problem);
    }

    public List<JudgeTask.TestCaseRef> manifest(Long id) {
        Problem problem = get(id);
        return problem.getTestCases().stream()
                .sorted(Comparator.comparing(TestCase::getCaseIndex))
                .map(tc -> JudgeTask.TestCaseRef.builder()
                        .name("case-" + tc.getCaseIndex())
                        .inputUrl(dataUrl(tc.getInput()))
                        .expectedOutputUrl(dataUrl(tc.getExpectedOutput()))
                        .build())
                .toList();
    }

    public ProblemDTO toDto(Problem problem) {
        return ProblemDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .tags(problem.getTags())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .setterId(problem.getSetterId())
                .published(problem.getPublished())
                .totalSubmit(problem.getTotalSubmit())
                .totalAc(problem.getTotalAc())
                .status(Boolean.TRUE.equals(problem.getPublished()) ? "PUBLISHED" : "DRAFT")
                .build();
    }

    private TestCase toCase(TestCaseInput input) {
        return TestCase.builder()
                .caseIndex(input.caseIndex())
                .input(input.input())
                .expectedOutput(input.expectedOutput())
                .score(input.score() == null ? 10 : input.score())
                .build();
    }

    private static String dataUrl(String value) {
        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return "data:text/plain;base64," + encoded;
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record CreateProblemCommand(
            String title,
            String description,
            Integer timeLimitMs,
            Integer memoryLimitMb,
            String difficulty,
            Long setterId,
            Boolean published,
            List<String> tags,
            List<TestCaseInput> testCases) {
    }

    public record TestCaseInput(Integer caseIndex, String input, String expectedOutput, Integer score) {
    }
}

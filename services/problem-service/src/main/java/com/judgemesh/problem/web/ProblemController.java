package com.judgemesh.problem.web;

import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.problem.service.ProblemService;
import com.judgemesh.problem.service.ProblemService.CreateProblemCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class ProblemController {
    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping({"/api/problem/list", "/api/problems"})
    public ApiResponse<List<ProblemDTO>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "false") Boolean includeDraft,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(problemService.list(q, tag, difficulty, includeDraft, page, size));
    }

    @GetMapping({"/api/problem/{id}", "/api/problems/{id}"})
    public ApiResponse<ProblemDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(problemService.toDto(problemService.get(id)));
    }

    @PostMapping("/api/problem")
    public ApiResponse<ProblemDTO> create(@RequestBody CreateProblemCommand command) {
        return ApiResponse.ok(problemService.toDto(problemService.create(command)));
    }

    @PutMapping("/api/problem/{id}")
    public ApiResponse<ProblemDTO> update(@PathVariable Long id, @RequestBody CreateProblemCommand command) {
        return ApiResponse.ok(problemService.toDto(problemService.update(id, command)));
    }

    @PostMapping("/api/problem/{id}/testcase")
    public ApiResponse<List<JudgeTask.TestCaseRef>> replaceTestcases(
            @PathVariable Long id,
            @RequestBody CreateProblemCommand command) {
        problemService.update(id, command);
        return ApiResponse.ok(problemService.manifest(id));
    }

    @GetMapping("/api/problem/{id}/testcase/manifest")
    public ApiResponse<List<JudgeTask.TestCaseRef>> manifest(@PathVariable Long id) {
        return ApiResponse.ok(problemService.manifest(id));
    }

    @GetMapping("/api/problem/internal/{id}")
    public ProblemDTO internalGet(@PathVariable Long id) {
        return problemService.toDto(problemService.get(id));
    }

    @GetMapping("/api/problem/internal/{id}/testcase/manifest")
    public List<JudgeTask.TestCaseRef> internalManifest(@PathVariable Long id) {
        return problemService.manifest(id);
    }
}

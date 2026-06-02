package com.judgemesh.problem.controller;

import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.problem.service.ProblemService;
import com.judgemesh.problem.vo.TestcaseManifestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/problem/internal")
@RequiredArgsConstructor
public class ProblemInternalController {

    private final ProblemService problemService;

    @GetMapping("/{id}")
    public ProblemDTO getById(@PathVariable Long id) {
        return problemService.getProblemDetail(id);
    }

    @GetMapping("/{id}/testcase/manifest")
    public List<JudgeTask.TestCaseRef> getManifest(@PathVariable Long id) {
        return problemService.getTestcaseManifest(id).stream()
                .map(this::toRef)
                .toList();
    }

    private JudgeTask.TestCaseRef toRef(TestcaseManifestVO testcase) {
        return JudgeTask.TestCaseRef.builder()
                .name(testcase.getName())
                .inputUrl(testcase.getInputUrl())
                .expectedOutputUrl(testcase.getExpectedOutputUrl())
                .build();
    }
}

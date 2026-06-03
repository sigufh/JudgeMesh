package com.judgemesh.problem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.error.ErrorCode;
import com.judgemesh.problem.service.ProblemService;
import com.judgemesh.problem.vo.ProblemCreateReq;
import com.judgemesh.problem.vo.ProblemUpdateReq;
import com.judgemesh.problem.vo.TestcaseManifestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/problem")
@RequiredArgsConstructor
public class ProblemCompatController {

    private final ProblemService problemService;

    @GetMapping("/list")
    public ApiResponse<List<ProblemDTO>> listProblems(
        @RequestParam(value = "current", defaultValue = "1") int current,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "tag", required = false) String tag,
        @RequestParam(value = "difficulty", required = false) String difficulty,
        @RequestParam(value = "includeDraft", required = false) Boolean includeDraft) {

        String effectiveKeyword = q != null ? q : keyword;
        Page<ProblemDTO> page = problemService.listProblems(current, size, effectiveKeyword, tag, difficulty);
        return ApiResponse.ok(page.getRecords());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProblemDTO> getProblem(@PathVariable("id") Long id) {
        ProblemDTO dto = problemService.getProblemDetail(id);
        if (dto == null) {
            return ApiResponse.fail(ErrorCode.PROBLEM_NOT_FOUND);
        }
        return ApiResponse.ok(dto);
    }

    @PostMapping
    public ApiResponse<Long> createProblem(
        @Validated @RequestBody ProblemCreateReq req,
        @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long setterId) {

        return ApiResponse.ok(problemService.createProblem(req, setterId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateProblem(@PathVariable("id") Long id, @RequestBody ProblemUpdateReq req) {
        problemService.updateProblem(id, req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/testcase")
    public ApiResponse<Void> uploadTestcaseCompat(
        @PathVariable("id") Long id,
        @RequestParam("caseIndex") Integer caseIndex,
        @RequestParam("inputFile") MultipartFile inputFile,
        @RequestParam("outputFile") MultipartFile outputFile,
        @RequestParam(value = "score", defaultValue = "10") Integer score) {
        try {
            problemService.uploadTestcase(id, caseIndex, inputFile, outputFile, score);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.TESTCASE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @GetMapping("/{id}/testcase/manifest")
    public ApiResponse<List<TestcaseManifestVO>> getManifestCompat(@PathVariable("id") Long id) {
        return ApiResponse.ok(problemService.getTestcaseManifest(id));
    }
}

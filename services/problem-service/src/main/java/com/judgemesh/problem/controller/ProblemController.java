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
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ApiResponse<Page<ProblemDTO>> listProblems(
        @RequestParam(value = "current", defaultValue = "1") int current,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "tag", required = false) String tag,
        @RequestParam(value = "difficulty", required = false) String difficulty) {

        return ApiResponse.ok(problemService.listProblems(current, size, keyword, tag, difficulty));
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

        Long id = problemService.createProblem(req, setterId);
        return ApiResponse.ok(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateProblem(
        @PathVariable("id") Long id,
        @RequestBody ProblemUpdateReq req) {

        problemService.updateProblem(id, req);
        return ApiResponse.ok(null);
    }

    /**
     * 上传测试用例接口 (由前端/出题人调用)
     */
    @PostMapping("/{id}/testcases")
    public ApiResponse<Void> uploadTestcase(
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

    /**
     * 获取测试用例清单接口 (仅供 Go Worker 调用)
     */
    @GetMapping("/{id}/testcases/manifest")
    public ApiResponse<List<TestcaseManifestVO>> getManifest(@PathVariable("id") Long id) {
        return ApiResponse.ok(problemService.getTestcaseManifest(id));
    }
}


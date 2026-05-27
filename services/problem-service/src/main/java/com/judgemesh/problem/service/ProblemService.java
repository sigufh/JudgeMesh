package com.judgemesh.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.problem.vo.ProblemCreateReq;
import com.judgemesh.problem.vo.ProblemUpdateReq;
import org.springframework.web.multipart.MultipartFile;
import com.judgemesh.problem.vo.TestcaseManifestVO;
import java.util.List;

public interface ProblemService {
    Long createProblem(ProblemCreateReq req, Long setterId);
    void updateProblem(Long id, ProblemUpdateReq req);
    ProblemDTO getProblemDetail(Long id);
    Page<ProblemDTO> listProblems(int current, int size, String keyword, String tag, String difficulty);
    void uploadTestcase(Long problemId, int caseIndex, MultipartFile inputFile, MultipartFile outputFile, Integer score);
    List<TestcaseManifestVO> getTestcaseManifest(Long problemId);
}

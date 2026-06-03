package com.judgemesh.api.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Dispatcher → Worker 派发协议体。
 *
 * <p>对应 docs/design/05-判题流水线.md。Go worker 端需按 JSON tag 实现镜像 struct。
 *
 * <p>⚠️ 这是 A↔D 共同维护的关键耦合点,任何字段改动必须双方 PR 双签。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeTask {

    /** 提交 ID,worker 回写时携带 */
    @JsonProperty("submit_id")
    private Long submitId;

    /** 题目 ID */
    @JsonProperty("problem_id")
    private Long problemId;

    /** 用户提交的源代码 */
    @JsonProperty("source")
    private String source;

    /** 编程语言:c / cpp / java / python */
    @JsonProperty("language")
    private String language;

    /** 时间限制(毫秒) */
    @JsonProperty("time_limit_ms")
    private Integer timeLimitMs;

    /** 内存限制(MB) */
    @JsonProperty("memory_limit_mb")
    private Integer memoryLimitMb;

    /** 测试用例 manifest URL(MinIO 预签名) */
    @JsonProperty("testcase_manifest_url")
    private String testcaseManifestUrl;

    /** 用例文件清单 */
    @JsonProperty("testcases")
    private List<TestCaseRef> testcases;

    /** 回调 URL,worker 完成后 POST 结果到这里 */
    @JsonProperty("callback_url")
    private String callbackUrl;

    /** 单次派发尝试 ID,用于回调幂等校验 */
    @JsonProperty("attempt_id")
    private String attemptId;

    /** 重试次数(由 dispatcher 维护) */
    @JsonProperty("retry_count")
    private Integer retryCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseRef {
        @JsonProperty("name")
        private String name;

        @JsonProperty("input_url")
        private String inputUrl;

        @JsonProperty("expected_output_url")
        private String expectedOutputUrl;
    }
}

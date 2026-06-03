package com.judgemesh.api.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Worker → submit-service 回调协议体。
 *
 * <p>对应 docs/design/05-判题流水线.md。
 *
 * <p>⚠️ A↔D 共同维护,改动需双签。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {

    @JsonProperty("submit_id")
    private Long submitId;

    /** AC / WA / TLE / MLE / RE / CE / SE(System Error) */
    @JsonProperty("status")
    private String status;

    /** 编译输出 / 运行错误信息(CE / RE 时填) */
    @JsonProperty("message")
    private String message;

    /** 单用例结果(RE 时只有失败的那条) */
    @JsonProperty("cases")
    private List<CaseResult> cases;

    /** 最大耗时(毫秒) */
    @JsonProperty("time_used_ms")
    private Integer timeUsedMs;

    /** 最大内存(KB) */
    @JsonProperty("memory_used_kb")
    private Integer memoryUsedKb;

    /** worker 实例标识(用于诊断) */
    @JsonProperty("worker_id")
    private String workerId;

    /** worker 版本(用于诊断) */
    @JsonProperty("worker_version")
    private String workerVersion;

    /** 单次派发尝试 ID,用于回调幂等校验 */
    @JsonProperty("attempt_id")
    private String attemptId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseResult {
        @JsonProperty("name")
        private String name;

        @JsonProperty("status")
        private String status;

        @JsonProperty("time_ms")
        private Integer timeMs;

        @JsonProperty("memory_kb")
        private Integer memoryKb;

        @JsonProperty("stderr")
        private String stderr;
    }
}

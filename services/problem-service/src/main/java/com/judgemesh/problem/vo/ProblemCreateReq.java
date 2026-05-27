package com.judgemesh.problem.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

//创建请求参数 VO
@Data
public class ProblemCreateReq {
    @NotBlank(message = "题目标题不能为空")
    private String title;

    @NotBlank(message = "题目描述不能为空")
    private String description;

    @NotNull(message = "时间限制不能为空")
    @Min(100)
    private Integer timeLimitMs;

    @NotNull(message = "内存限制不能为空")
    @Min(16)
    private Integer memoryLimitMb;

    @NotBlank(message = "难度不能为空")
    private String difficulty; // EASY, MEDIUM, HARD

    private List<String> tags;
}

package com.judgemesh.problem.vo;

import lombok.Data;
import java.util.List;

@Data
public class ProblemUpdateReq {
    private String title;
    private String description;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private String difficulty;
    private Boolean published;
    private List<String> tags;
}

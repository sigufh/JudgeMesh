package com.judgemesh.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("problem")
public class Problem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private String difficulty;
    private Long setterId;
    private Boolean published;
    private Integer totalSubmit;
    private Integer totalAc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.judgemesh.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("testcase_manifest")
public class TestcaseManifest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long problemId;
    private Integer caseIndex;
    private String inputObject;
    private String outputObject;
    private Integer score;
}

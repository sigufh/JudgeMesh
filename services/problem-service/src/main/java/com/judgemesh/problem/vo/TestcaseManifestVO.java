package com.judgemesh.problem.vo;

import lombok.Data;

@Data
public class TestcaseManifestVO {
    private String name; // 对应第几个测试点，比如 "1"
    private String inputUrl; // 给 Worker 的 in 文件预签名下载链接
    private String expectedOutputUrl; // 给 Worker 的 ans 文件预签名下载链接
    private Integer score;
}

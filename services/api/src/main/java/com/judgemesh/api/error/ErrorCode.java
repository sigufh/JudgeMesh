package com.judgemesh.api.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 全局错误码。命名约定:模块前缀 + 4 位数字。 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用 1xxx
    SUCCESS("0", "OK"),
    BAD_REQUEST("1400", "请求参数错误"),
    UNAUTHORIZED("1401", "未登录或登录过期"),
    FORBIDDEN("1403", "无权限"),
    NOT_FOUND("1404", "资源不存在"),
    INTERNAL_ERROR("1500", "系统内部错误"),

    // user 2xxx
    USER_NOT_FOUND("2001", "用户不存在"),
    USER_PASSWORD_WRONG("2002", "密码错误"),
    USER_BALANCE_INSUFFICIENT("2010", "余额不足"),

    // problem 3xxx
    PROBLEM_NOT_FOUND("3001", "题目不存在"),
    PROBLEM_OFFLINE("3002", "题目已下线"),
    TESTCASE_UPLOAD_FAILED("3010", "测试用例上传失败"),

    // submit / judge 4xxx
    SUBMIT_NOT_FOUND("4001", "提交不存在"),
    SUBMIT_RATE_LIMITED("4002", "提交过于频繁"),
    JUDGE_TIMEOUT("4010", "判题超时"),
    JUDGE_WORKER_UNAVAILABLE("4011", "无可用 worker"),

    // contest 5xxx
    CONTEST_NOT_FOUND("5000", "比赛不存在"),
    CONTEST_NOT_STARTED("5001", "比赛未开始"),
    CONTEST_ENDED("5002", "比赛已结束"),
    CONTEST_NOT_REGISTERED("5003", "未报名");

    private final String code;
    private final String message;
}

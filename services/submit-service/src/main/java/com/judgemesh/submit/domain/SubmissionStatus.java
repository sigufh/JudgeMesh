package com.judgemesh.submit.domain;

public enum SubmissionStatus {
    PENDING,
    JUDGING,
    AC,
    WA,
    TLE,
    MLE,
    RE,
    CE,
    SE;

    public boolean terminal() {
        return this == AC || this == WA || this == TLE || this == MLE || this == RE || this == CE || this == SE;
    }
}

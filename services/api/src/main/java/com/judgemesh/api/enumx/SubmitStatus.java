package com.judgemesh.api.enumx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum SubmitStatus {
    PENDING("pending"),
    JUDGING("judging"),
    AC("ac"),
    WA("wa"),
    TLE("tle"),
    MLE("mle"),
    RE("re"),
    CE("ce"),
    SE("se");

    private final String wireValue;

    SubmitStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String toWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static SubmitStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (SubmitStatus status : values()) {
            if (status.wireValue.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported submit status: " + value);
    }
}

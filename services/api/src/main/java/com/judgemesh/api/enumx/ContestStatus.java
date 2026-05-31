package com.judgemesh.api.enumx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum ContestStatus {
    UPCOMING("upcoming"),
    RUNNING("running"),
    FROZEN("frozen"),
    ENDED("ended");

    private final String wireValue;

    ContestStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String toWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static ContestStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (ContestStatus status : values()) {
            if (status.wireValue.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported contest status: " + value);
    }
}

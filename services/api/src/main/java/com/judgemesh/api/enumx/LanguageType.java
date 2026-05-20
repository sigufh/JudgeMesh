package com.judgemesh.api.enumx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum LanguageType {
    C("c"),
    CPP("cpp"),
    JAVA("java"),
    PYTHON("python");

    private final String wireValue;

    LanguageType(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String toWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static LanguageType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (LanguageType type : values()) {
            if (type.wireValue.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + value);
    }
}

package com.mlprograms.justmath.graphfx;

import java.util.Objects;

public enum ReservedVariables {

    X("x"),
    Y("y");

    private final String value;

    ReservedVariables(final String value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public String getValue() {
        return value;
    }

}

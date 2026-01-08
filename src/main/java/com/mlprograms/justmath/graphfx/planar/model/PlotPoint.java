package com.mlprograms.justmath.graphfx.planar.model;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Objects;

public record PlotPoint(BigNumber x, BigNumber y) {

    public PlotPoint {
        Objects.requireNonNull(x, "x must not be null");
        Objects.requireNonNull(y, "y must not be null");
    }

}

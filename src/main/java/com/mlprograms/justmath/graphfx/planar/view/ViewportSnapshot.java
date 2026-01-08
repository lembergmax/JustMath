package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Objects;

public record ViewportSnapshot(BigNumber minX, BigNumber maxX, BigNumber minY, BigNumber maxY, BigNumber cellSize) {

    public ViewportSnapshot {
        Objects.requireNonNull(minX, "minX must not be null");
        Objects.requireNonNull(maxX, "maxX must not be null");
        Objects.requireNonNull(minY, "minY must not be null");
        Objects.requireNonNull(maxY, "maxY must not be null");
        Objects.requireNonNull(cellSize, "cellSize must not be null");
    }

}

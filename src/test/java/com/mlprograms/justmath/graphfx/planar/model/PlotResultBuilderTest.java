package com.mlprograms.justmath.graphfx.planar.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlotResultBuilderTest {

    @Test
    void builderAddsPointsAndLines() {
        PlotResult result = PlotResult.builder()
                .addPoint(1.5, 2.5)
                .addPoint(PlotPoint.of(3, 4))
                .addLine(PlotPoint.of(0, 0), PlotPoint.of(1, 1), PlotPoint.of(2, 1))
                .build();

        assertEquals(2, result.plotPoints().size());
        assertEquals(1, result.plotLines().size());
        assertEquals(3, result.plotLines().get(0).plotPoints().size());
    }
}

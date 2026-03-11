/*
 * Copyright (c) 2025-2026 Max Lemberg
 */
package com.mlprograms.justmath.graphfx.planar.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlotModelFactoriesTest {

    @Test
    void plotPointFactoriesCreateExpectedPoints() {
        assertEquals("1.5", PlotPoint.of(1.5, 2.5).x().toString());
        assertEquals("2.5", PlotPoint.of(1.5, 2.5).y().toString());

        final PlotPoint point = PlotPoint.of("3", "4");
        assertEquals("3", point.x().toString());
        assertEquals("4", point.y().toString());
    }

    @Test
    void plotLineFromDoublesCreatesPolyline() {
        final PlotLine line = PlotLine.fromDoubles(0, 0, 1, 1, 2, 3);
        assertEquals(3, line.plotPoints().size());
        assertEquals("2.0", line.plotPoints().get(2).x().toString());
        assertEquals("3.0", line.plotPoints().get(2).y().toString());
    }

    @Test
    void plotResultWithMethodsAppendWithoutMutating() {
        final PlotResult base = new PlotResult();
        final PlotResult withPoint = base.withPoint(PlotPoint.of(1, 2));
        final PlotResult withLine = withPoint.withLine(PlotLine.between(PlotPoint.of(0, 0), PlotPoint.of(1, 1)));

        assertTrue(base.plotPoints().isEmpty());
        assertTrue(base.plotLines().isEmpty());
        assertEquals(1, withLine.plotPoints().size());
        assertEquals(1, withLine.plotLines().size());
    }

    @Test
    void plotLineAndResultDefensivelyCopyInputLists() {
        final List<PlotPoint> mutablePoints = new ArrayList<>();
        mutablePoints.add(PlotPoint.of(0, 0));

        final PlotLine line = PlotLine.of(mutablePoints);
        final PlotResult result = new PlotResult(mutablePoints, List.of(line));

        mutablePoints.add(PlotPoint.of(10, 10));

        assertEquals(1, line.plotPoints().size());
        assertEquals(1, result.plotPoints().size());
        assertThrows(UnsupportedOperationException.class, () -> line.plotPoints().add(PlotPoint.of(1, 1)));
    }
}

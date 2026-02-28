package com.mlprograms.justmath.graphing;

import com.mlprograms.justmath.graphing.api.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultGraphingCalculatorTest {

    @Test
    void shouldSampleQuadraticFunction() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();

        PlotResponse response = calculator.plot("x^2", new Domain(-2.0d, 2.0d), Resolution.fixed(5));

        PlotSeries series = response.series().getFirst();
        assertEquals(5, series.xValues().length);
        assertEquals(-2.0d, series.xValues()[0], 1e-10);
        assertEquals(4.0d, series.yValues()[0], 1e-10);
        assertEquals(0.0d, series.yValues()[2], 1e-10);
        assertEquals(4.0d, series.yValues()[4], 1e-10);
    }

    @Test
    void shouldRejectInvalidDomain() {
        assertThrows(IllegalArgumentException.class, () -> new Domain(1.0d, 1.0d));
    }
}

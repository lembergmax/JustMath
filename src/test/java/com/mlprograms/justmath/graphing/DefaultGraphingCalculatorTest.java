package com.mlprograms.justmath.graphing;

import com.mlprograms.justmath.graphing.api.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGraphingCalculatorTest {

    @Test
    void shouldSampleQuadraticWithFixedResolution() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();

        PlotResponse response = calculator.plot("x^2", new Domain(-2.0, 2.0), Resolution.fixed(5));

        assertEquals(1, response.series().size());
        PlotSeries series = response.series().getFirst();
        assertArrayEquals(new double[]{-2.0, -1.0, 0.0, 1.0, 2.0}, series.xValues(), 1e-12);
        assertArrayEquals(new double[]{4.0, 1.0, 0.0, 1.0, 4.0}, series.yValues(), 1e-12);
    }

    @Test
    void shouldSupportAdditionalVariablesFromRequest() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();

        PlotRequest request = new PlotRequest.Builder("a*x")
                .domain(new Domain(0.0, 2.0))
                .resolution(Resolution.fixed(3))
                .variables(Map.of("a", 3.0))
                .build();

        PlotSeries series = calculator.plot(request).series().getFirst();

        assertArrayEquals(new double[]{0.0, 1.0, 2.0}, series.xValues(), 1e-12);
        assertArrayEquals(new double[]{0.0, 3.0, 6.0}, series.yValues(), 1e-12);
    }

    @Test
    void shouldRejectInvalidDomain() {
        assertThrows(IllegalArgumentException.class, () -> new Domain(1.0, 1.0));
    }
}

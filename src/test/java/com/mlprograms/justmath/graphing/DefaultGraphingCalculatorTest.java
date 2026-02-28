package com.mlprograms.justmath.graphing;

import com.mlprograms.justmath.graphing.api.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGraphingCalculatorTest {

    @Test
    void shouldPlotUniformFunction() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();

        PlotResponse response = calculator.plot("x^2", new Domain(-2, 2), Resolution.fixed(5));

        assertEquals(1, response.series().size());
        PlotSeries series = response.series().getFirst();
        assertEquals(5, series.size());
        assertEquals(4.0, series.yValues()[0], 1e-6);
        assertEquals(0.0, series.yValues()[2], 1e-6);
        assertEquals(4.0, series.yValues()[4], 1e-6);
    }

    @Test
    void shouldCompileAndReuseExpression() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();

        CompiledPlotExpression first = calculator.compile("sin(x)");
        CompiledPlotExpression second = calculator.compile("sin(x)");

        assertSame(first, second);
    }

    @Test
    void shouldRejectInvalidDomain() {
        assertThrows(IllegalArgumentException.class, () -> new Domain(3, 3));
    }

    @Test
    void shouldSupportAdaptiveSampling() {
        GraphingCalculator calculator = GraphingCalculators.createDefault();
        PlotRequest request = new PlotRequest.Builder("x^3")
                .domain(new Domain(-2, 2))
                .resolution(Resolution.fixed(32))
                .samplingMode(SamplingMode.ADAPTIVE)
                .build();

        PlotResponse response = calculator.plot(request);

        assertFalse(response.series().isEmpty());
        assertTrue(response.series().getFirst().size() >= 32);
    }
}

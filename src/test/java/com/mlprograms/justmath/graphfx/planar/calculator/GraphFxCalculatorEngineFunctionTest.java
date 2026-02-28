package com.mlprograms.justmath.graphfx.planar.calculator;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GraphFxCalculatorEngineFunctionTest {

    @Test
    void shouldEvaluateFunctionViaGraphingCoreAdapter() {
        GraphFxCalculatorEngine engine = new GraphFxCalculatorEngine();
        ViewportSnapshot viewport = new ViewportSnapshot(
                new BigNumber("-2"),
                new BigNumber("2"),
                new BigNumber("-10"),
                new BigNumber("10"),
                new BigNumber("1")
        );

        PlotResult result = engine.evaluateFunction("x^2", viewport);

        assertFalse(result.plotLines().isEmpty());
        assertFalse(result.plotLines().getFirst().plotPoints().isEmpty());
    }
}

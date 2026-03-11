package com.mlprograms.justmath.calculator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorEnginePerformanceRegressionTest {

    @Test
    void evaluateCleansUpThreadLocalVariablesAfterCall() {
        final CalculatorEngine engine = new CalculatorEngine();

        engine.evaluate("a+1", Map.of("a", "41"));

        assertTrue(CalculatorEngine.getCurrentVariables().isEmpty());
    }

    @Test
    void nestedVariableEvaluationStillWorksWithCleanup() {
        final CalculatorEngine engine = new CalculatorEngine();

        final String result = engine.evaluate("a+b", Map.of("a", "2", "b", "a+3")).toString();

        assertEquals("7", result);
        assertTrue(CalculatorEngine.getCurrentVariables().isEmpty());
    }
}

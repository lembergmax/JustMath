package com.mlprograms.justmath.calculator;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class CalculatorEnginePerformanceSmokeTest {

    @Test
    void repeatedVariableReferencesAreEvaluatedWithinReasonableTime() {
        CalculatorEngine engine = new CalculatorEngine();
        Map<String, String> variables = Map.of(
                "a", "sqrt(2)+sqrt(3)+sqrt(5)",
                "b", "a+a+a+a+a+a+a+a+a+a"
        );

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int i = 0; i < 100; i++) {
                engine.evaluate("b+b+b+b+b+b+b+b+b+b", variables);
            }
        });
    }
}

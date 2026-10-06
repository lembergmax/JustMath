/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Properties of the expression engine. The {@code evaluateSafe} family promises to never throw and never
 * return {@code null}, whatever the input. Evaluation must not depend on the expression cache, and the
 * engine must agree with the direct {@code BigNumber} operations on valid input.
 *
 * <p>The properties run with a fixed seed so that the build is deterministic. A failing property prints
 * its shrunk sample and the seed.</p>
 */
class CalculatorEnginePropertyTest {

    private static final String SEED = "20261006";

    private static final int TEXT_TRIES = 400;

    private static final int ARITHMETIC_TRIES = 300;

    private static final Duration EVALUATION_TIMEOUT = Duration.ofSeconds(20);

    private static final int MAX_TOKENS = 25;

    private static final int MAX_OPERAND_PRECISION = 45;

    private static final List<String> TOKENS = List.of(
            "0", "1", "2", "3", "7", "10", "99", "123", "0.5", ".", "+", "-", "*", "/", "^", "%", "!", "(", ")", ";",
            " ", "sin(", "cos(", "tan(", "sqrt(", "ln(", "log10(", "abs(", "pi", "π", "e", "x", "y", "|", "comb(",
            "perm(", "gcd(", "summation(", "k", "=", ",", "√", "²", "1e5", "--", "((", "))", "()", "\t", "0.0.0");

    @Provide
    Arbitrary<String> arbitraryText() {
        return Arbitraries.strings().ofMaxLength(60);
    }

    @Provide
    Arbitrary<String> tokenSoup() {
        return Arbitraries.of(TOKENS).list().ofMaxSize(MAX_TOKENS).map(tokens -> String.join("", tokens));
    }

    @Provide
    Arbitrary<BigDecimal> operands() {
        return DecimalArbitraries.decimals().filter(value -> value.precision() <= MAX_OPERAND_PRECISION);
    }

    @Provide
    Arbitrary<Character> operators() {
        return Arbitraries.of('+', '-', '*');
    }

    @Property(tries = TEXT_TRIES, seed = SEED)
    void safeEvaluationOfArbitraryTextNeverThrows(@ForAll("arbitraryText") final String text) {
        assertSafeEvaluationHoldsItsPromise(text);
    }

    @Property(tries = TEXT_TRIES, seed = SEED)
    void safeEvaluationOfTokenSoupNeverThrows(@ForAll("tokenSoup") final String text) {
        assertSafeEvaluationHoldsItsPromise(text);
    }

    @Property(tries = TEXT_TRIES, seed = SEED)
    void cachedAndFreshEvaluationAgree(@ForAll("tokenSoup") final String text) {
        final CalculatorEngine cachedEngine = new CalculatorEngine();
        final String first = cachedEngine.evaluateSafeToString(text);
        final String second = cachedEngine.evaluateSafeToString(text);
        final String fresh = new CalculatorEngine().evaluateSafeToString(text);

        assertEquals(fresh, first, "first evaluation of '" + text + "'");
        assertEquals(fresh, second, "cached evaluation of '" + text + "'");
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void engineMatchesBigDecimalForAddSubtractMultiply(
            @ForAll("operands") final BigDecimal left,
            @ForAll("operators") final char operator,
            @ForAll("operands") final BigDecimal right
    ) {
        final String expression = "(" + left.toPlainString() + ")" + operator + "(" + right.toPlainString() + ")";
        final BigDecimal expected = switch (operator) {
            case '+' -> left.add(right);
            case '-' -> left.subtract(right);
            case '*' -> left.multiply(right);
            default -> throw new IllegalStateException("Unexpected operator: " + operator);
        };

        final BigNumber actual = new CalculatorEngine().evaluate(expression);

        assertEquals(0, expected.compareTo(actual.toBigDecimal()),
                () -> expression + ": expected " + expected.toPlainString() + " but was " + actual.toBigDecimal().toPlainString());
    }

    private static void assertSafeEvaluationHoldsItsPromise(final String text) {
        final CalculatorEngine engine = new CalculatorEngine();
        final Map<String, String> variables = Map.of("x", "2", "y", "x+1");

        assertTimeoutPreemptively(EVALUATION_TIMEOUT, () -> {
            assertValidResult(engine.evaluateSafe(text));
            assertValidResult(engine.evaluateSafe(text, variables));
            assertValidResult(engine.evaluateToStringResult(text));
            assertValidResult(engine.evaluateToPrettyStringResult(text));
            assertNotNull(engine.evaluateSafeToString(text));
            assertNotNull(engine.evaluateSafeToPrettyString(text));
        }, () -> "evaluation of '" + text + "' did not finish in " + EVALUATION_TIMEOUT);
    }

    private static void assertValidResult(final CalculatorResult<?> result) {
        assertNotNull(result);
        assertTrue(result.value().isPresent() ^ result.error().isPresent(),
                "a result holds either a value or an error");
    }

}

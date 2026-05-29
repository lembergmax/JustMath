/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.exceptions.CyclicVariableReferenceException;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorEngineTest {

    private final CalculatorEngine calculatorEngineRad = new CalculatorEngine(TrigonometricMode.RAD);
    private final CalculatorEngine calculatorEngineDeg = new CalculatorEngine(TrigonometricMode.DEG);

    @ParameterizedTest
    @CsvSource(value = {
            // --- Custom ---
            "abs(-5)+sqrt(16)+cbrt(27)+log2(8)+ln(e)+sin(pi/2)^2+cos(0)+tan(pi/4)+3!+5^2+10%3+gcd(54;24)+lcm(6;8)+summation(1;5;k^2)+product(1;4;k)+rootn(32;5)#162",
            "3!+3#9",
            "|-5|+3#8",
            "|-5|3#15",
            // --- Grundoperationen kombiniert ---
            "2+3*4#14",
            "(2+3)*4#20",
            "100/5+7*2#34",
            "(50-5)/(3+2)#9",
            "10-3!-2#2",
            // --- Potenzen mit Operatoren ---
            "2^3+4*2#16",
            "(2^3)^2#64",
            "5^2+sqrt(81)#34",
            "2^(3+2)#32",
            "sqrt(16)+2^4#20",
            // --- Trigonometrie + Operatoren ---
            "sin(pi/2)+cos(0)#2",
            "tan(pi/4)^2+1#2",
            "cot(pi/4)+sin(pi/2)#2",
            "sin(pi/6)*cos(pi/3)#0.25",
            "(sin(pi/2)+cos(pi))^2#0",
            // --- Verschachtelte Trigonometrie ---
            "sin(cos(0))#0.8414709848",
            "cos(sin(1))#0.6663667454",
            "tan(sin(pi/4))#0.854510432",
            // --- Logarithmen kombiniert ---
            "log2(8)+log10(100)#5",
            "ln(e^2)+sqrt(16)#6",
            "log10(100*sqrt(25))#2.6989700043",
            "log2(2^10)#10",
            // --- summationmation / productukt + Operatoren ---
            "summation(1;3;k)+product(1;3;k)#12",
            "summation(1;4;k^2)-product(1;3;k)#24",
            "product(1;4;k)+summation(1;4;k)#34",
            "summation(1;5;2*k)#30",
            // --- Fakultät + Modulo + Potenzen ---
            "5!%7#1",
            "10%3+3!#7",
            "4!+2^3#32",
            "(6!/5!)+1#7",
            // --- Verschachtelte abs/sqrt Kombinationen ---
            "sqrt(abs(-16))#4",
            "abs(sqrt(49)-10)#3",
            "sqrt(abs(-3*3*3*3))#9",
            // --- gcd/lcm kombiniert ---
            "gcd(54;24)+lcm(6;8)#30",
            "lcm(3;5)-gcd(21;14)#8",
            "lcm(12;18)/gcd(12;18)#6",
            // --- Konstanten kombiniert ---
            "pi*2#6.2831853072",
            "e^1#2.7182818285",
            "cos(pi)+sin(pi)#-1",
            "tan(pi)+e-e#0",
            // --- Tiefe Verschachtelung ---
            "sqrt((3+5*2)^(2)-(4^2))#12.3693168769",
            "sin(cos(tan(pi/4)))#0.5143952585",
            "ln(sqrt((e^3)*(e^2)))#2.5",
            // --- Kombination mehrerer Kategorien ---
            "summation(1;3;k^2)+product(1;3;k!)+gcd(24;54)#32",
            "lcm(4;6)+abs(-10)+sqrt(49)#29",
            "cos(pi/2)^2+sin(pi/2)^2#1",
            "(sin(pi/2)+cos(0))*log2(8)#6",
            // --- Hardcore Ausdruck ---
            "sqrt((abs(-5)+3!)^2+(log2(8)+sin(pi/2))^2)#11.7046999107"
    }, delimiter = '#')
    void evaluationResultLongTest(String calculationString, String expectedResult) {
        BigNumber actualResult = calculatorEngineRad.evaluate(calculationString);
        assertEquals(expectedResult, actualResult.roundAfterDecimals(new MathContext(10, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "x+2#7",
            "y*3#15",
            "z^2#49",
            "a+b#9",
            "a^a#256",
            "summation(1;3;k)+x#11",
            "product(1;3;k)+y#11",
            "abs(x)+sqrt(z)#7.645751311"
    }, delimiter = '#')
    void evaluationResultWithVariablesTest(String calculationString, String expectedResult) {
        Map<String, String> variables =
                Map.of("x", new BigNumber("5").toString(),
                        "y", new BigNumber("5").toString(),
                        "z", new BigNumber("7").toString(),
                        "a", new BigNumber("4").toString(),
                        "b", new BigNumber("5").toString()
                );

        BigNumber actualResult = calculatorEngineRad.evaluate(calculationString, variables);
        assertEquals(expectedResult, actualResult.roundAfterDecimals(9).toString());
    }

    /**
     * Regression: binary '+' / '-' directly after a pre-expanded three-argument
     * function (summation/product). The tokenizer must emit a binary operator
     * there, not a prefix unary-sign sentinel; otherwise arity validation
     * rejected the expression with a spurious SYNTAX_UNEXPECTED_END.
     * summation(1;3;k)=6, product(1;3;k)=6, product(1;2;k)=2.
     */
    @ParameterizedTest
    @CsvSource(value = {
            "summation(1;3;k)+product(1;3;k)#12",
            "summation(1;3;k)-product(1;3;k)#0",
            "summation(1;3;k)-product(1;2;k)#4",
            "product(1;3;k)+summation(1;3;k)-2#10",
            "summation(1;3;k)+5#11",
            "summation(1;4;k^2)-product(1;3;k)#24"
    }, delimiter = '#')
    void binarySignAfterThreeArgumentFunctionTest(String calculationString, String expectedResult) {
        BigNumber actualResult = calculatorEngineRad.evaluate(calculationString);
        assertEquals(expectedResult, actualResult.roundAfterDecimals(new MathContext(10, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "--5#5",
            "++5#5",
            "+-5#-5",
            "-+5#-5",
            "5--3#8",
            "5---3#2",
            "5+-3#2",
            "5-+3#2",
            "-(3+4)#-7",
            "--(3+4)#7",
            "-3^2#-9",
            "2*-3^2#-18",
            "2*(-3)^2#18",
            "-------5+3#-2"
    }, delimiter = '#')
    void signMergeEvalTest(String calculationString, String expectedResult) {
        BigNumber actualResult = calculatorEngineRad.evaluate(calculationString);
        assertEquals(expectedResult, actualResult.roundAfterDecimals(new MathContext(10, RoundingMode.HALF_UP)).toString());
    }

    @Test
    void whitespaceSeparatesSignRunsInEval() {
        BigNumber actualResult = calculatorEngineRad.evaluate("5 - -3");
        assertEquals("8", actualResult.roundAfterDecimals(new MathContext(10, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-",
            "+",
            "5-",
            "sin(-)"
    })
    void signMergeErrorCases(String calculationString) {
        assertThrows(Exception.class, () -> calculatorEngineRad.evaluate(calculationString));
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- DEG Modus ---
            "sin(90)#1",
            "cos(180)#-1",
            "tan(45)#1",
            "cot(45)#1",
            "sin(30)+cos(60)#1",
            "tan(60)^2#3",
            "cos(90)+sin(0)#0",
            "sin(30)^2+cos(30)^2#1",
            "sin(90)*2+sqrt(16)#6"
    }, delimiter = '#')
    void evaluationResultDegModeTest(String calculationString, String expectedResult) {
        BigNumber actualResult = calculatorEngineDeg.evaluate(calculationString);
        assertEquals(expectedResult, actualResult.roundAfterDecimals(new MathContext(10, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10^20#100000000000000000000",
            "1/3#0.3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333",
            "2/3#0.6666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666667",
            "1/7#0.1428571428571428571428571428571428571428571428571428571428571428571428571428571428571428571428571429",
            "1/9#0.1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
    }, delimiter = '#')
    void evaluateToStringTest(String expression, String expectedResult) {
        String actualResult = calculatorEngineRad.evaluateToString(expression);
        assertEquals(expectedResult, actualResult);

    }

    @ParameterizedTest
    @CsvSource(value = {
            "(2+4",
            "|2",
            "5/0",
            "log(-10)",
            "sqrt(-4)",
            "unknownFunc(5)",
            "5++2",
            "summation(5;2;3)",
            "gcd(5)",
    })
    void evaluateToStringExceptionTest(String expression) {
        String actualResult = calculatorEngineRad.evaluateToString(expression);
        assertFalse(actualResult.toLowerCase().contains("exception"));

        actualResult = calculatorEngineRad.evaluateToPrettyString(expression);
        assertFalse(actualResult.toLowerCase().contains("exception"));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10^6#1,000,000",
            "10^9#1,000,000,000",
            "10^12#1,000,000,000,000",
            "1/3#0.3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333",
            "2/3#0.6666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666667",
            "22/7#3.142857142857142857142857142857142857142857142857142857142857142857142857142857142857142857142857143",
            "sqrt(1000000)#1,000",
            "sqrt(2)#1.414213562373095048801688724209698078569671875376948073176679737990732478462107038850387534327641573",
            "2^10#1,024",
            "5^8#390,625",
            "summation(1;100;k)#5,050",
            "1+3*summation(1;100;k)#15,151",
            "product(1;5;k)#120",
            "3*1+product(1;5;k)#123",
            "10!#3,628,800",
            "15!#1,307,674,368,000",
            "x*1000#5,000",
            "y^3#125",
            "a+b#9"
    }, delimiter = '#')
    void evaluateToPrettyStringTest(String expression, String expectedResult) {
        Map<String, String> variables =
                Map.of("x", new BigNumber("5").toString(),
                        "y", new BigNumber("5").toString(),
                        "a", new BigNumber("4").toString(),
                        "b", new BigNumber("5").toString());

        String actualResult = calculatorEngineRad.evaluateToPrettyString(expression, variables);
        assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Einfache Durchschnittsberechnungen ---
            "average(5)#5",
            "average(0;10)#5",
            "average(25;50;75)#50",
            "average(1;2;3;4;5)#3",
            "average(-5;5)#0",
            "average(-1;-2;-3;-4)#-2.5",
            "average(1.5;2.5;3.5)#2.5",
            // Große Werte (BigNumber-Fähigkeit)
            "average(100000000000000000000;200000000000000000000)#150000000000000000000"
    }, delimiter = '#')
    void averageBasicEvaluationTest(String expression, String expectedResult) {
        BigNumber actualResult = calculatorEngineRad.evaluate(expression);
        assertEquals(
                expectedResult,
                actualResult.roundAfterDecimals(new MathContext(20, RoundingMode.HALF_UP)).toString()
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- average kombiniert mit Operatoren ---
            "3*average(25;50;75)#150",
            "average(10;20)+5#20",
            "2^average(2;4)#8",
            "average(2;4)*average(3;9)#18",
            // Verschachtelte Ausdrücke als Argumente
            "average(1+1;2+2;3+3)#4",
            "average(summation(1;3;k);product(1;3;k))#6",
            "average(gcd(54;24);lcm(6;8))#15"
    }, delimiter = '#')
    void averageCombinedWithOperatorsTest(String expression, String expectedResult) {
        BigNumber actualResult = calculatorEngineRad.evaluate(expression);
        assertEquals(
                expectedResult,
                actualResult.roundAfterDecimals(new MathContext(20, RoundingMode.HALF_UP)).toString()
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- average mit Variablen ---
            "average(x;y;z)#4",
            "average(a;b)#4.5",
            "3*average(x;y;z)#12",
            "average(x^2;y^2;z^2)#18.66666666666666666667",
            "average(x;average(y;z))#3.5"
    }, delimiter = '#')
    void averageWithVariablesTest(String expression, String expectedResult) {
        Map<String, String> variables =
                Map.of("x", new BigNumber("2").toString(),
                        "y", new BigNumber("4").toString(),
                        "z", new BigNumber("6").toString(),
                        "a", new BigNumber("4").toString(),
                        "b", new BigNumber("5").toString());

        BigNumber actualResult = calculatorEngineRad.evaluate(expression, variables);
        assertEquals(
                expectedResult,
                actualResult.roundAfterDecimals(new MathContext(20, RoundingMode.HALF_UP)).toString()
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Pretty-String-Ausgabe für average ---
            "average(25;50;75)#50",
            "3*average(25;50;75)#150",
            "average(1000;2000;3000)#2,000",
            "average(1000000;2000000)#1,500,000"
    }, delimiter = '#')
    void averagePrettyStringTest(String expression, String expectedResult) {
        String actualResult = calculatorEngineRad.evaluateToPrettyString(expression);
        assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Grundfunktionalität ---
            "sum(5)#5",
            "sum(5;10)#15",
            "sum(1;2;3;4;5)#15",
            "sum(-5;5)#0",
            "sum(-1;-2;-3)#-6",
            "sum(1.5;2.5;3.5)#7.5",

            // Große Zahlen
            "sum(100000000000000000000;200000000000000000000)#300000000000000000000"
    }, delimiter = '#')
    void sumBasicTest(String expression, String expectedResult) {
        BigNumber actual = calculatorEngineRad.evaluate(expression);
        assertEquals(expectedResult,
                actual.roundAfterDecimals(new MathContext(30, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Kombination mit Operatoren ---
            "3*sum(1;2;3)#18",
            "sum(10;20)+5#35",
            "2^sum(1;1)#4",
            "sum(2;4)*sum(3;9)#72",

            // Verschachtelte Argumente
            "sum(1+1;2+2;3+3)#12",
            "sum(summation(1;3;k);product(1;3;k))#12",
            "sum(gcd(54;24);lcm(6;8))#30"
    }, delimiter = '#')
    void sumCombinedTest(String expression, String expectedResult) {
        BigNumber actual = calculatorEngineRad.evaluate(expression);
        assertEquals(expectedResult,
                actual.roundAfterDecimals(new MathContext(30, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Sum mit Variablen ---
            "sum(x;y;z)#12",
            "sum(a;b)#9",
            "3*sum(x;y;z)#36",
            "sum(x^2;y^2;z^2)#56",
            "sum(x;sum(y;z))#12"
    }, delimiter = '#')
    void sumVariablesTest(String expression, String expectedResult) {
        Map<String, String> variables =
                Map.of("x", "2", "y", "4", "z", "6",
                        "a", "4", "b", "5");

        BigNumber actual = calculatorEngineRad.evaluate(expression, variables);
        assertEquals(expectedResult,
                actual.roundAfterDecimals(new MathContext(30, RoundingMode.HALF_UP)).toString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // --- Pretty String Ausgabe ---
            "sum(25;50;75)#150",
            "3*sum(25;50;75)#450",
            "sum(1000;2000;3000)#6,000",
            "sum(1000000;2000000)#3,000,000"
    }, delimiter = '#')
    void sumPrettyStringTest(String expression, String expected) {
        String actual = calculatorEngineRad.evaluateToPrettyString(expression);
        assertEquals(expected, actual);
    }

    @Nested
    class VariableRecursionTest {

        private final CalculatorEngine calculatorEngine = new CalculatorEngine();

        @Test
        void checkVariablesForRecursion_noCycle_shouldPass() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "b+1");
            variables.put("b", "c+2");
            variables.put("c", "3");

            assertDoesNotThrow(() -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

        @Test
        void checkVariablesForRecursion_directCycle_shouldThrow() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "b+1");
            variables.put("b", "a+2");

            assertThrows(CyclicVariableReferenceException.class,
                    () -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

        @Test
        void checkVariablesForRecursion_indirectCycle_shouldThrow() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "b+1");
            variables.put("b", "c+2");
            variables.put("c", "a+3");

            assertThrows(CyclicVariableReferenceException.class,
                    () -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

        @Test
        void checkVariablesForRecursion_sharedSubexpression_shouldPass() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "b+c");
            variables.put("b", "d+1");
            variables.put("c", "d+2");
            variables.put("d", "5");

            assertDoesNotThrow(() -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

        @Test
        void checkVariablesForRecursion_emptyAndConstantVariables_shouldPass() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "");
            variables.put("b", "5");

            assertDoesNotThrow(() -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

        @Test
        void checkVariablesForRecursion_referenceToUndefinedVariable_shouldThrowOnReplaceButNotOnCheck() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "b+1");
            assertDoesNotThrow(() -> CalculatorEngineUtils.checkVariablesForRecursion(calculatorEngine, variables));
        }

    }

    @Nested
    class EvaluateSafeStringTest {

        private final CalculatorEngine calculatorEngine = new CalculatorEngine();

        @Test
        void evaluateSafeToString_validExpression_returnsResult() {
            assertEquals("3", calculatorEngine.evaluateSafeToString("1+2"));
        }

        @Test
        void evaluateSafeToPrettyString_validExpression_returnsResult() {
            String pretty = calculatorEngine.evaluateSafeToPrettyString("1+2");
            assertNotNull(pretty);
            assertFalse(pretty.startsWith("Error"));
            assertFalse(pretty.startsWith("Fehler"));
        }

        @Test
        void evaluateSafeToString_emptyExpression_returnsZero() {
            assertEquals("0", calculatorEngine.evaluateSafeToString(""));
        }

        @Test
        void evaluateSafeToPrettyString_emptyExpression_returnsZero() {
            assertEquals("0", calculatorEngine.evaluateSafeToPrettyString(""));
        }

        @Test
        void evaluateSafeToString_null_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToString(null);
            assertNotNull(result);
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToPrettyString_null_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToPrettyString(null);
            assertNotNull(result);
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToString_incompleteExpression_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToString("1+");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToPrettyString_incompleteExpression_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToPrettyString("1+");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToString_unknownFunction_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToString("unknownFunction(5)");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToPrettyString_unknownFunction_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToPrettyString("unknownFunction(5)");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToString_divisionByZero_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToString("1/0");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToPrettyString_divisionByZero_returnsErrorPrefix() {
            String result = calculatorEngine.evaluateSafeToPrettyString("1/0");
            assertTrue(result.startsWith("Error:"), result);
        }

        @Test
        void evaluateSafeToString_germanLocale_usesFehlerPrefix() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMAN);
            String result = engine.evaluateSafeToString("1+");
            assertTrue(result.startsWith("Fehler:"), result);
        }

        @Test
        void evaluateSafeToString_nullVariables_doesNotThrow() {
            assertEquals("3", calculatorEngine.evaluateSafeToString("1+2", null));
        }

        @Test
        void evaluateSafeToPrettyString_nullVariables_doesNotThrow() {
            String pretty = calculatorEngine.evaluateSafeToPrettyString("1+2", null);
            assertFalse(pretty.startsWith("Error"));
        }

        @Test
        void evaluateSafeToString_neverReturnsStacktrace() {
            String result = calculatorEngine.evaluateSafeToString("1/0");
            assertFalse(result.contains("at com.mlprograms"), "should not contain stacktrace");
            assertFalse(result.contains("\tat "), "should not contain stacktrace");
        }

        @Test
        void evaluateSafeToString_neverReturnsNull() {
            assertNotNull(calculatorEngine.evaluateSafeToString(null));
            assertNotNull(calculatorEngine.evaluateSafeToString(""));
            assertNotNull(calculatorEngine.evaluateSafeToString("1+"));
            assertNotNull(calculatorEngine.evaluateSafeToString("unknownFunction(5)"));
            assertNotNull(calculatorEngine.evaluateSafeToString("1/0"));
            assertNotNull(calculatorEngine.evaluateSafeToPrettyString(null));
            assertNotNull(calculatorEngine.evaluateSafeToPrettyString(""));
            assertNotNull(calculatorEngine.evaluateSafeToPrettyString("1+"));
            assertNotNull(calculatorEngine.evaluateSafeToPrettyString("unknownFunction(5)"));
            assertNotNull(calculatorEngine.evaluateSafeToPrettyString("1/0"));
        }

        @Test
        void existingEvaluateToString_stillReturnsRawErrorWithoutPrefix() {
            String result = calculatorEngine.evaluateToString("1+");
            assertNotNull(result);
            assertFalse(result.startsWith("Error:"), "existing API must not have Error: prefix");
            assertFalse(result.startsWith("Fehler:"), "existing API must not have Fehler: prefix");
        }

    }

    @Nested
    class LocaleAwareResultFormattingTest {

        @Test
        void englishLocale_decimalUsesPoint() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.ENGLISH);
            assertEquals("0.5", engine.evaluateToString("1/2"));
        }

        @Test
        void usLocale_prettyUsesCommaGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("1,234.56", engine.evaluateToPrettyString("1234.56"));
        }

        @Test
        void usLocale_safeMethodsBehaveConsistently() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("0.5", engine.evaluateSafeToString("1/2"));
            assertEquals("1,234.56", engine.evaluateSafeToPrettyString("1234.56"));
        }

        @Test
        void germanLocale_decimalUsesComma() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("0,5", engine.evaluateToString("1/2"));
        }

        @Test
        void germanLocale_prettyUsesDotGroupingCommaDecimal() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1.234,56", engine.evaluateToPrettyString("1234.56"));
        }

        @Test
        void germanLocale_safeMethodsAreLocaleAware() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("0,5", engine.evaluateSafeToString("1/2"));
            assertEquals("1.234,56", engine.evaluateSafeToPrettyString("1234.56"));
        }

        @Test
        void germanLocale_negativeNumbers() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("-1234,56", engine.evaluateToString("-1234.56"));
            assertEquals("-1.234,56", engine.evaluateToPrettyString("-1234.56"));
        }

        @Test
        void germanLocale_integerWithoutDecimalSeparator() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1000", engine.evaluateToString("1000"));
            assertEquals("1.000", engine.evaluateToPrettyString("1000"));
        }

        @Test
        void germanLocale_largeNumberPrettyFormatting() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1.234.567.890,123456", engine.evaluateToPrettyString("1234567890.123456"));
        }

        @Test
        void frenchLocale_decimalUsesComma() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.FRANCE);
            assertEquals("0,5", engine.evaluateToString("1/2"));
        }

        @Test
        void inputParsingNotAffectedByLocale() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            // Input uses '.' as decimal separator regardless of result locale.
            assertEquals("1235", engine.evaluateToString("1234.56 + 0.44"));
        }

        @Test
        void germanLocale_errorMessagesStillUseFehlerPrefix() {
            CalculatorEngine engine = new CalculatorEngine()
                    .setLocale(java.util.Locale.GERMANY)
                    .setErrorMode(com.mlprograms.justmath.calculator.errors.ErrorMode.USER_FRIENDLY);
            String result = engine.evaluateSafeToString("1+");
            assertTrue(result.startsWith("Fehler:"), result);
        }

        @Test
        void defaultLocaleIsBackwardCompatibleEnglish() {
            CalculatorEngine engine = new CalculatorEngine();
            assertEquals("0.5", engine.evaluateToString("1/2"));
            assertEquals("1,234.56", engine.evaluateToPrettyString("1234.56"));
        }

        @Test
        void switchingLocaleAtRuntimeReflectsInNextEvaluation() {
            CalculatorEngine engine = new CalculatorEngine();
            assertEquals("0.5", engine.evaluateToString("1/2"));
            engine.setLocale(java.util.Locale.GERMANY);
            assertEquals("0,5", engine.evaluateToString("1/2"));
            engine.setLocale(java.util.Locale.US);
            assertEquals("0.5", engine.evaluateToString("1/2"));
        }

    }

    @Nested
    class EvaluateStringResultTest {

        private final CalculatorEngine calculatorEngine = new CalculatorEngine();

        @Test
        void evaluateToStringResult_validExpression_returnsSuccess() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("1+2");
            assertTrue(result.isSuccess());
            assertEquals("3", result.value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_validExpression_returnsSuccess() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToPrettyStringResult("1+2");
            assertTrue(result.isSuccess());
            assertEquals("3", result.value().orElseThrow());
        }

        @Test
        void evaluateToStringResult_emptyExpression_returnsSuccessZero() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("");
            assertTrue(result.isSuccess());
            assertEquals("0", result.value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_emptyExpression_returnsSuccessZero() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToPrettyStringResult("");
            assertTrue(result.isSuccess());
            assertEquals("0", result.value().orElseThrow());
        }

        @Test
        void evaluateToStringResult_incompleteExpression_returnsFailure() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("1+");
            assertTrue(result.isFailure());
            assertTrue(result.value().isEmpty());
            assertTrue(result.error().isPresent());
        }

        @Test
        void evaluateToPrettyStringResult_incompleteExpression_returnsFailure() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToPrettyStringResult("1+");
            assertTrue(result.isFailure());
        }

        @Test
        void evaluateToStringResult_divisionByZero_returnsFailure() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("1/0");
            assertTrue(result.isFailure());
            com.mlprograms.justmath.calculator.errors.CalculatorError error = result.error().orElseThrow();
            assertEquals(
                    com.mlprograms.justmath.calculator.errors.CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO,
                    error.code());
        }

        @Test
        void evaluateToPrettyStringResult_divisionByZero_returnsFailure() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToPrettyStringResult("1/0");
            assertTrue(result.isFailure());
            assertEquals(
                    com.mlprograms.justmath.calculator.errors.CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO,
                    result.error().orElseThrow().code());
        }

        @Test
        void evaluateToStringResult_unknownVariable_returnsFailureWithSyntaxCode() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("x+1");
            assertTrue(result.isFailure());
            assertEquals(
                    com.mlprograms.justmath.calculator.errors.CalculatorErrorCode.SYNTAX_UNKNOWN_VARIABLE,
                    result.error().orElseThrow().code());
        }

        @Test
        void evaluateToStringResult_withVariables_returnsSuccess() {
            Map<String, String> variables = new HashMap<>();
            variables.put("x", "10");
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("x*2", variables);
            assertTrue(result.isSuccess());
            assertEquals("20", result.value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_withVariables_returnsSuccess() {
            Map<String, String> variables = new HashMap<>();
            variables.put("a", "1000");
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    new CalculatorEngine().setLocale(java.util.Locale.US)
                            .evaluateToPrettyStringResult("a*2", variables);
            assertTrue(result.isSuccess());
            assertEquals("2,000", result.value().orElseThrow());
        }

        @Test
        void evaluateToStringResult_usLocale_decimalUsesPoint() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("0.5", engine.evaluateToStringResult("1/2").value().orElseThrow());
        }

        @Test
        void evaluateToStringResult_germanLocale_decimalUsesComma() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("0,5", engine.evaluateToStringResult("1/2").value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_usLocale_usesCommaGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("1,234.56", engine.evaluateToPrettyStringResult("1234.56").value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_germanLocale_usesDotGroupingCommaDecimal() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1.234,56", engine.evaluateToPrettyStringResult("1234.56").value().orElseThrow());
        }

        @Test
        void evaluateToStringResult_failurePayloadHasNoErrorPrefix() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("1/0");
            assertTrue(result.isFailure());
            // No string parsing required — the Result carries the structured error directly.
            String technical = result.error().orElseThrow().technicalDetail();
            assertNotNull(technical);
            assertFalse(technical.startsWith("Error:"));
            assertFalse(technical.startsWith("Fehler:"));
        }

        @Test
        void evaluateToStringResult_neverReturnsNull() {
            assertNotNull(calculatorEngine.evaluateToStringResult("1+2"));
            assertNotNull(calculatorEngine.evaluateToStringResult(""));
            assertNotNull(calculatorEngine.evaluateToStringResult("1+"));
            assertNotNull(calculatorEngine.evaluateToStringResult("1/0"));
            assertNotNull(calculatorEngine.evaluateToPrettyStringResult("1+2"));
            assertNotNull(calculatorEngine.evaluateToPrettyStringResult(""));
            assertNotNull(calculatorEngine.evaluateToPrettyStringResult("1+"));
            assertNotNull(calculatorEngine.evaluateToPrettyStringResult("1/0"));
        }

        @Test
        void evaluateToStringResult_nullExpression_throwsNpe() {
            // Typed Result variants are @NonNull on input (consistent with evaluateSafe(...)).
            // Callers that need null-tolerance use the evaluateSafeTo... methods instead.
            assertThrows(NullPointerException.class,
                    () -> calculatorEngine.evaluateToStringResult(null));
            assertThrows(NullPointerException.class,
                    () -> calculatorEngine.evaluateToPrettyStringResult(null));
        }

        @Test
        void evaluateToStringResult_failureValueMappedThroughMap_remainsFailure() {
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    calculatorEngine.evaluateToStringResult("1+");
            com.mlprograms.justmath.calculator.errors.CalculatorResult<Integer> mapped =
                    result.map(String::length);
            assertTrue(mapped.isFailure());
            assertEquals(result.error().orElseThrow().code(), mapped.error().orElseThrow().code());
        }

    }

    /**
     * Pinpoint tests for the documented locale contract: {@code evaluateToString} uses the
     * locale's decimal separator with no thousands grouping; {@code evaluateToPrettyString}
     * uses both decimal separator and grouping. Mirrored across the Safe UI String API and
     * the Typed Result API. Also asserts that {@code setLocale(...)} does not change the
     * input grammar — expressions are always parsed with {@code .} as the decimal separator.
     */
    @Nested
    class LocaleContractTest {

        @Test
        void evaluateToString_germany_noGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1234,56", engine.evaluateToString("1234.56"));
        }

        @Test
        void evaluateToPrettyString_germany_withGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            assertEquals("1.234,56", engine.evaluateToPrettyString("1234.56"));
        }

        @Test
        void evaluateToString_us_noGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("1234.56", engine.evaluateToString("1234.56"));
        }

        @Test
        void evaluateToPrettyString_us_withGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.US);
            assertEquals("1,234.56", engine.evaluateToPrettyString("1234.56"));
        }

        @Test
        void evaluateSafeToString_germany_nullExpression_usesFehlerPrefix() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            String result = engine.evaluateSafeToString(null);
            assertNotNull(result);
            assertTrue(result.startsWith("Fehler: "), result);
        }

        @Test
        void evaluateSafeToPrettyString_germany_nullExpression_usesFehlerPrefix() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            String result = engine.evaluateSafeToPrettyString(null);
            assertNotNull(result);
            assertTrue(result.startsWith("Fehler: "), result);
        }

        @Test
        void evaluateToStringResult_incompleteExpression_isFailure() {
            CalculatorEngine engine = new CalculatorEngine();
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    engine.evaluateToStringResult("1+");
            assertTrue(result.isFailure());
            assertTrue(result.error().isPresent());
        }

        @Test
        void evaluateToPrettyStringResult_incompleteExpression_isFailure() {
            CalculatorEngine engine = new CalculatorEngine();
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    engine.evaluateToPrettyStringResult("1+");
            assertTrue(result.isFailure());
            assertTrue(result.error().isPresent());
        }

        @Test
        void evaluateToStringResult_germany_isSuccessWithLocaleDecimalNoGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    engine.evaluateToStringResult("1234.56");
            assertTrue(result.isSuccess());
            assertEquals("1234,56", result.value().orElseThrow());
        }

        @Test
        void evaluateToPrettyStringResult_germany_isSuccessWithLocaleDecimalAndGrouping() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            com.mlprograms.justmath.calculator.errors.CalculatorResult<String> result =
                    engine.evaluateToPrettyStringResult("1234.56");
            assertTrue(result.isSuccess());
            assertEquals("1.234,56", result.value().orElseThrow());
        }

        @Test
        void setLocale_doesNotChangeInputDecimalSeparator_dotInputAlwaysWorks() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            // Parser contract: '.' is the decimal separator for input, regardless of locale.
            assertEquals("3", engine.evaluateToString("1.5+1.5"));
        }

        @Test
        void setLocale_doesNotEnableCommaAsInputDecimalSeparator() {
            CalculatorEngine engine = new CalculatorEngine().setLocale(java.util.Locale.GERMANY);
            // Parser contract: ',' is an argument separator, not a decimal separator.
            // "1,5+1,5" is therefore a syntax error under any locale — the Text Output API
            // catches the exception and folds it into the returned string.
            String result = engine.evaluateToString("1,5+1,5");
            assertNotEquals("3", result);
        }

    }

}

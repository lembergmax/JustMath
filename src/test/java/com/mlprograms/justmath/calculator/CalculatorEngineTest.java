/*
 * Copyright (c) 2025 Max Lemberg
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
    class CalculatorEngineUtilTest {

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

}

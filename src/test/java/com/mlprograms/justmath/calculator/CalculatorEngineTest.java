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
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorEngineTest {

    private final CalculatorEngine calculatorEngineRad = new CalculatorEngine(TrigonometricMode.RAD);
    private final CalculatorEngine calculatorEngineDeg = new CalculatorEngine(TrigonometricMode.DEG);

    @ParameterizedTest
    @CsvSource(value = {
            // --- Custom ---
            "abs(-5)+sqrt(16)+cbrt(27)+log2(8)+ln(e)+sin(pi/2)^2+cos(0)+tan(pi/4)+3!+5^2+10%3+gcd(54;24)+lcm(6;8)+sum(1;5;k^2)+prod(1;4;k)+rootn(32;5)#162",
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
            // --- Summation / Produkt + Operatoren ---
            "sum(1;3;k)+prod(1;3;k)#12",
            "sum(1;4;k^2)-prod(1;3;k)#24",
            "prod(1;4;k)+sum(1;4;k)#34",
            "sum(1;5;2*k)#30",
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
            "sum(1;3;k^2)+prod(1;3;k!)+gcd(24;54)#32",
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
            "sum(1;3;k)+x#11",
            "prod(1;3;k)+y#11",
            "abs(x)+sqrt(z)#7.645751311"
    }, delimiter = '#')
    void evaluationResultWithVariablesTest(String calculationString, String expectedResult) {
        Map<String, BigNumber> variables =
                Map.of("x", new BigNumber("5"),
                        "y", new BigNumber("5"),
                        "z", new BigNumber("7"),
                        "a", new BigNumber("4"),
                        "b", new BigNumber("5")
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

}

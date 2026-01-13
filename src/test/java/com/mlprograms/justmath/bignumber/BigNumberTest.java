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

package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BigNumberTest {

    @Nested
    public class BasicMath {

        @ParameterizedTest
        @CsvSource({
                "25.5,30.2,55.7",
                "0.000000000000000000000000000000000000000000000001,0.00000000000000000003,0.000000000000000000030000000000000000000000000001",
                "8736519650165165946166562572365809265462671456,143153651451954101155145145169254155145,8736519793318817398120663727510954434716826601",
                "123,456,579"
        })
        void additionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);

            BigNumber actualResult = num1.add(num2, Locale.US);

            assertEquals(inputExpectedResult, actualResult.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "100,30,70",
                "0.0000000001,0.00000000009,0.00000000001",
                "500,1000,-500"
        })
        void subtractionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.subtract(num2);
            assertEquals(inputExpectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "12,3,36",
                "0.00001,100000,1",
                "123456789,987654321,121932631112635269"
        })
        void multiplicationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.multiply(num2);
            assertEquals(inputExpectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "100,4,25",
                "1,3,0.33333333333333333333",
                "123456789123456789,1,123456789123456789"
        })
        void divisionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.divide(num2, new MathContext(20, RoundingMode.HALF_UP));
            assertEquals(inputExpectedResult, result.trim().toString());
        }

        @ParameterizedTest
        @CsvSource({
                "2,10,1024",
                "5,0,1",
                "1.1,2,1.21",
                "-1.2,-2.99,-0.57976",
                "20,2,400"
        })
        void powerTest(String inputNum1, String inputNum2, String inputExpectedResultPrefix) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber exponent = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.power(exponent);
            assertEquals(inputExpectedResultPrefix, result.roundAfterDecimals(5).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,1",
                "1,1",
                "5,120",
                "20,2432902008176640000"
        })
        void factorialTest(String inputNum, String inputExpectedResult) {
            BigNumber num = new BigNumber(inputNum, Locale.US);
            BigNumber result = num.factorial();
            assertEquals(inputExpectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,1",
                "1,2.7182818285",
                "-1,0.3678794412",
                "2,7.3890560989",
                "0.5,1.6487212707"
        })
        void expTest(String inputX, String expectedResult) {
            BigNumber x = new BigNumber(inputX, Locale.US);
            BigNumber result = x.exp().roundAfterDecimals(10);
            assertEquals(expectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "10,3,1",
                "25,7,4",
                "100,10,0",
                "123456789,10000,6789",
                "-15,4,1",
                "5,2,1"
        })
        void moduloTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.modulo(num2);
            assertEquals(inputExpectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "10,3,1",
                "25,7,4",
                "100,10,0",
                "123456789,10000,6789",
                "-15,4,-3",
                "5,2,1",
                "-5,2,-1",
                "5,-2,1",
                "-5,-2,-1"
        })
        void remainderTest(final String inputNum1, final String inputNum2, final String inputExpectedResult) {
            final BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            final BigNumber num2 = new BigNumber(inputNum2, Locale.US);

            final BigNumber result = num1.remainder(num2);

            assertEquals(inputExpectedResult, result.toString());
        }

    }

    @Nested
    public class CombinatoricsMath {

        @ParameterizedTest
        @CsvSource({
                "0,0,1",
                "1,1,1",
                "12,7,792"
        })
        void combinationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.combination(num2);

            assertEquals(inputExpectedResult, result.toString());
        }

        @Test
        void combinationInvalidInputTest() {
            BigNumber num1 = new BigNumber("123", Locale.US);
            BigNumber num2 = new BigNumber("345", Locale.US);

            assertThrows(IllegalArgumentException.class, () -> num1.combination(num2));
        }

        @ParameterizedTest
        @CsvSource({
                "0,0,1",
                "1,1,1",
                "12,7,3991680"
        })
        void permutationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumber result = num1.permutation(num2);

            assertEquals(inputExpectedResult, result.toString());
        }

        @Test
        void permutationInvalidInputTest() {
            BigNumber num1 = new BigNumber("123", Locale.US);
            BigNumber num2 = new BigNumber("345", Locale.US);

            assertThrows(IllegalArgumentException.class, () -> num1.permutation(num2));
        }

    }

    @Nested
    public class CoordinateConversionMath {

        @ParameterizedTest
        @CsvSource({
                "0,0,0,0",
                "12.874,7.000032,12.77804,1.56895"
        })
        void polarToCartesianCoordinateTest(String inputNum1, String inputNum2, String expectedX, String expectedY) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumberCoordinate result = num1.polarToCartesianCoordinates(num2);

            assertEquals(expectedX, result.getX().roundAfterDecimals(5).toString());
            assertEquals(expectedY, result.getY().roundAfterDecimals(5).toString());
        }

        @Test
        void polarToCartesianCoordinateInvalidTest() {
            BigNumber num1 = new BigNumber("-1", Locale.US);
            BigNumber num2 = new BigNumber("0", Locale.US);

            assertThrows(IllegalArgumentException.class, () -> num1.polarToCartesianCoordinates(num2));
        }

        @ParameterizedTest
        @CsvSource({
                "1,1,1.414214,45",
                "12.874,7.000032,14.654021,28.534431"
        })
        void cartesianToPolarCoordinateTest(String inputNum1, String inputNum2, String expectedX, String expectedY) {
            BigNumber num1 = new BigNumber(inputNum1, Locale.US);
            BigNumber num2 = new BigNumber(inputNum2, Locale.US);
            BigNumberCoordinate result = num1.cartesianToPolarCoordinates(num2, BigNumbers.DEFAULT_MATH_CONTEXT, Locale.US);

            assertEquals(expectedX, result.getX().roundAfterDecimals(6).toString());
            assertEquals(expectedY, result.getY().roundAfterDecimals(6).toString());
        }

        @Test
        void cartesianToPolarCoordinateInvalidTest() {
            BigNumber num1 = new BigNumber("-1", Locale.US);
            BigNumber num2 = new BigNumber("0", Locale.US);

            assertThrows(IllegalArgumentException.class, () -> num1.cartesianToPolarCoordinates(num2));
        }

    }

    @Nested
    public class HyperbolicTrigonometricMath {

        @ParameterizedTest
        @CsvSource({
                "0,0",
                "1,1.17520119",
                "-1,-1.17520119",
                "1.2541,1.6096751"
        })
        void sinhTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.sinh().round(new MathContext(expectedResult.length(), RoundingMode.HALF_UP)).trim();

            assertEquals(expectedResult, result.roundAfterDecimals(8).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,1",
                "1,1.543081",
                "-1,1.543081",
                "1.2541,1.895008"
        })
        void coshTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.cosh().roundAfterDecimals(6).trim();

            assertEquals(expectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,0",
                "1,0.761594",
                "-1,-0.761594",
                "1.2541,0.849429"
        })
        void tanhTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.tanh();

            assertEquals(expectedResult, result.roundAfterDecimals(6).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1,1.3130352855",
                "-1,-1.3130352855"
        })
        void cothTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.coth();

            assertEquals(expectedResult, result.roundAfterDecimals(10).toString());
        }

        @Test
        void cothInvalidTest() {
            BigNumber zero = new BigNumber("0", Locale.US);
            assertThrows(IllegalArgumentException.class, zero::coth, "coth(0) should throw ArithmeticException (division by zero)");
        }

    }

    @Nested
    public class InverseHyperbolicTrigonometricMath {

        @ParameterizedTest
        @CsvSource({
                "0,0",
                "1,0.88137359",
                "-1,-0.88137359",
                "2.5,1.64723115"
        })
        void asinhTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.asinh();

            assertEquals(expectedResult, result.roundAfterDecimals(8).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1,0",
                "2,1.3169579",
                "10,2.99322285"
        })
        void acoshTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.acosh();

            assertEquals(expectedResult, result.roundAfterDecimals(8).toString());
        }

        @Test
        void acoshInvalidInputTest() {
            BigNumber invalid = new BigNumber("0.5", Locale.US);
            assertThrows(IllegalArgumentException.class, invalid::acosh, "acosh(x < 1) should throw exception");
        }

        @ParameterizedTest
        @CsvSource({
                "0.5,0.549306"
        })
        void atanhTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.atanh();

            assertEquals(expectedResult, result.roundAfterDecimals(6).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "-1.01",
                "1"
        })
        void atanhInvalidInputTest(String input) {
            BigNumber num = new BigNumber(input, Locale.US);
            assertThrows(IllegalArgumentException.class, num::atanh, "atanh(|x| >= 1) should throw exception");
        }

        @ParameterizedTest
        @CsvSource({
                "2,0.54930614",
                "-2,-0.54930614"
        })
        void acothTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.acoth();

            assertEquals(expectedResult, result.roundAfterDecimals(8).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0.5",
                "1",
                "-1" // throws ArithmeticException
        })
        void acothInvalidInputTest(String input) {
            BigNumber num = new BigNumber(input, Locale.US);
            assertThrows(Exception.class, num::acoth, "acoth(|x| <= 1) should throw exception");
        }

    }

    @Nested
    public class InverseTrigonometricMath {

        @ParameterizedTest
        @CsvSource({
                "0,RAD,0",
                "0,DEG,0",
                "1,RAD,1.570796",
                "1,DEG,90",
                "-1,RAD,-1.570796",
                "-1,DEG,-90"
        })
        void asinTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
            BigNumber num = new BigNumber(input, trigonometricMode, Locale.US);
            assertEquals(expectedResult, num.asin(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US).round(new MathContext(7)).trim().toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1,RAD,0",
                "1,DEG,0",
                "0,RAD,1.570796",
                "0,DEG,90",
                "-1,RAD,3.141593",
                "-1,DEG,180"
        })
        void acosTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
            BigNumber num = new BigNumber(input, trigonometricMode, Locale.US);
            assertEquals(expectedResult, num.acos().round(new MathContext(7)).trim().toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,RAD,0",
                "0,DEG,0",
                "1,RAD,0.785",
                "1,DEG,45",
                "-1,RAD,-0.785",
                "-1,DEG,-45",
                "1000,RAD,1.57",
                "1000,DEG,89.943"
        })
        void atanTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
            BigNumber num = new BigNumber(input, trigonometricMode, Locale.US);
            assertEquals(expectedResult, num.atan().roundAfterDecimals(expectedResult.contains(".") ? 3 : 0).toString());
        }

        @ParameterizedTest
        @CsvSource(value = {
                "1,RAD,0.785398",
                "1,DEG,45",
                "2,RAD,0.463648",
                "2,DEG,26.565051",
                "-2,RAD,-0.463648",
                "-2,DEG,-26.565051"
        })
        void acotTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            String actual = num.acot(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US)
                    .roundAfterDecimals(6)
                    .toString();
            assertEquals(expectedResult, actual);
        }

        @ParameterizedTest
        @CsvSource({
                "2,RAD",
                "2,DEG"
        })
        void asinInvalidTest(String input, TrigonometricMode trigonometricMode) {
            BigNumber num = new BigNumber(input, trigonometricMode);
            assertThrows(ArithmeticException.class, num::asin);
        }

        @ParameterizedTest
        @CsvSource({
                "1.5,RAD",
                "1.5,DEG"
        })
        void acosInvalidTest(String input, TrigonometricMode trigonometricMode) {
            BigNumber num = new BigNumber(input, trigonometricMode);
            assertThrows(ArithmeticException.class, num::acos);
        }

        @ParameterizedTest
        @CsvSource({
                "0,RAD,1,5707964",
                "0,DEG,90"
        })
        void acotInvalidTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
            BigNumber num = new BigNumber(input, trigonometricMode);
            // assertEquals(expectedResult, num.acot(trigonometricMode).toString());
            assertThrows(ArithmeticException.class, () -> num.acot(trigonometricMode));
        }

    }

    @Nested
    public class LogarithmicMath {

        @ParameterizedTest
        @CsvSource({
                "1,0",
                "2,1",
                "8,3",
                "10,3.32193"
        })
        void log2Test(String input, String expectedPrefix) {
            BigNumber arg = new BigNumber(input);
            BigNumber result = arg.log2(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
            assertEquals(expectedPrefix, result.roundAfterDecimals(5).toString());
        }

        @Test
        void log2InvalidTest() {
            BigNumber zero = new BigNumber("0");
            assertThrows(IllegalArgumentException.class, () -> zero.log2(new MathContext(10, RoundingMode.HALF_UP), Locale.US));
        }

        @ParameterizedTest
        @CsvSource({
                "1,0",
                "10,1",
                "100,2",
                "2,0.30103"
        })
        void log10Test(String input, String expectedPrefix) {
            BigNumber arg = new BigNumber(input);
            BigNumber result = arg.log10(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
            assertEquals(expectedPrefix, result.roundAfterDecimals(5).toString());
        }

        @Test
        void log10InvalidTest() {
            BigNumber neg = new BigNumber("-5");
            assertThrows(IllegalArgumentException.class, () -> neg.log10(new MathContext(10, RoundingMode.HALF_UP), Locale.GERMAN));
        }

        @ParameterizedTest
        @CsvSource({
                "1,0",
                "2.71828182845904523536,1",
                "10,2.302585093"
        })
        void lnTest(String input, String expectedPrefix) {
            BigNumber arg = new BigNumber(input);
            BigNumber result = arg.ln(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
            assertEquals(expectedPrefix, result.toString());
        }

        @Test
        void lnInvalidTest() {
            BigNumber neg = new BigNumber("0");
            assertThrows(ArithmeticException.class, () -> neg.ln(new MathContext(10, RoundingMode.HALF_UP), Locale.US));
        }

        @ParameterizedTest
        @CsvSource({
                "8,2,3",
                "27,3,3",
                "16,2,4",
                "10,10,1"
        })
        void logBaseTest(String number, String base, String expectedPrefix) {
            BigNumber arg = new BigNumber(number);
            BigNumber b = new BigNumber(base);
            BigNumber result = arg.logBase(b, new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
            assertEquals(expectedPrefix, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,2",
                "5,1",
                "1,0",
                "-3,2"
        })
        void logBaseInvalidTest(String number, String base) {
            BigNumber arg = new BigNumber(number);
            BigNumber b = new BigNumber(base);
            assertThrows(IllegalArgumentException.class,
                    () -> arg.logBase(b, new MathContext(10, RoundingMode.HALF_UP), Locale.US));
        }

    }

    @Nested
    public class NumberTheoryMath {

        @ParameterizedTest
        @CsvSource({
                "12, 8, 4",
                "100, 25, 25",
                "7, 3, 1",
                "-18, 24, 6",
                "0, 5, 5",
                "5, 0, 5",
                "0, 0, 0"
        })
        void gcdTest(String input1, String input2, String expected) {
            BigNumber a = new BigNumber(input1);
            BigNumber b = new BigNumber(input2);
            BigNumber result = a.gcd(b);
            assertEquals(expected, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "4, 6, 12",
                "5, 3, 15",
                "0, 7, 0",
                "7, 0, 0",
                "-3, 5, 15",
                "-2, -4, 4"
        })
        void lcmTest(String input1, String input2, String expected) {
            BigNumber a = new BigNumber(input1);
            BigNumber b = new BigNumber(input2);
            BigNumber result = a.lcm(b);
            assertEquals(expected, result.toString());
        }

    }

    @Nested
    public class PercentageMath {

        @ParameterizedTest
        @CsvSource({
                "20, 50, 10",
                "50, 200, 100",
                "0, 1000, 0",
                "100, 42, 42",
                "12.5, 80, 10",
                "-25, 200, -50",
                "150, 10, 15"
        })
        void nPercentFromMTest(String n, String m, String expectedResult) {
            BigNumber percent = new BigNumber(n, Locale.US);
            BigNumber base = new BigNumber(m, Locale.US);

            BigNumber result = base.percentFromM(percent);
            assertEquals(expectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "25, 50, 50",
                "100, 200, 50",
                "42, 42, 100",
                "10, 80, 12.5",
                "0, 1000, 0",
                "-50, 200, -25",
                "15, 10, 150"
        })
        void xIsNPercentOfMTest(String part, String total, String expectedResult) {
            BigNumber partVal = new BigNumber(part, Locale.US);
            BigNumber totalVal = new BigNumber(total, Locale.US);

            BigNumber result = partVal.isXPercentOfN(totalVal);
            assertEquals(expectedResult, result.toString());
        }

    }

    @Nested
    public class RadicalMath {

        @ParameterizedTest
        @CsvSource({
                "9,3",
                "2,1.41421",
                "0,0"
        })
        void squareRootTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.squareRoot();
            assertEquals(expectedResult, result.roundAfterDecimals(5).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "27,3",
                "0,0",
                "8,2",
                "-27,-3"
        })
        void cubicRootTest(String input, String expectedResult) {
            BigNumber num = new BigNumber(input, Locale.US);
            BigNumber result = num.cubicRoot();
            assertEquals(expectedResult, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "81,4,3",
                "32,5,2",
                "1,100,1",
                "8,-12,0.8409"
        })
        void nthRootTest(String inputNum, String rootNum, String expectedResult) {
            BigNumber num = new BigNumber(inputNum, Locale.US);
            BigNumber root = new BigNumber(rootNum, Locale.US);
            BigNumber result = num.nthRoot(root);
            assertEquals(expectedResult, result.roundAfterDecimals(5).toString());
        }

        @Test
        void nthRootNegativeEvenRootTest() {
            BigNumber finalNum = new BigNumber("-16");
            BigNumber finalRoot = new BigNumber("4");
            assertThrows(IllegalArgumentException.class, () -> finalNum.nthRoot(finalRoot),
                    "Even root of negative number should throw exception");
        }

    }

    @Nested
    public class TrigonometricMath {

        @ParameterizedTest
        @CsvSource({
                "0, RAD, 0",
                "0, DEG, 0",
                "1, RAD, 0.8414709848",
                "1, DEG, 0.01745240643",
                "1.57079632679, RAD, 1",
                "90, DEG, 1",
                "3.14159265359, RAD, 0",
                "180, DEG, 0",
                "-1.57079632679, RAD, -1",
                "-90, DEG, -1"
        })
        void sinTest(String input, TrigonometricMode mode, String expectedStr) {
            BigNumber angle = new BigNumber(input, Locale.US);
            BigNumber result = angle.sin(BigNumbers.DEFAULT_MATH_CONTEXT, mode, Locale.US);
            BigDecimal actual = result.toBigDecimal();
            BigDecimal expected = new BigDecimal(expectedStr);

            BigDecimal tolerance = new BigDecimal("1E-9");

            BigDecimal diff = actual.subtract(expected).abs();
            assertTrue(diff.compareTo(tolerance) <= 0,
                    () -> String.format("Expected approx: %s, but was: %s (diff = %s)", expected, actual, diff));
        }

        @ParameterizedTest
        @CsvSource({
                "0, RAD, 1",
                "0, DEG, 1",
                "1, RAD, 0.5403023059",
                "1, DEG, 0.9998476952",
                "3.14159265359, RAD, -1",
                "180, DEG, -1",
                "-1, RAD, 0.5403023059",
                "-1, DEG, 0.9998476952"
        })
        void cosTest(String input, TrigonometricMode trigonometricMode, String expectedStr) {
            BigNumber angle = new BigNumber(input, Locale.US);
            BigNumber result = angle.cos(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US);
            BigDecimal actual = result.toBigDecimal();
            BigDecimal expected = new BigDecimal(expectedStr);
            BigDecimal tolerance = new BigDecimal("1E-9");
            BigDecimal diff = actual.subtract(expected).abs();
            assertTrue(diff.compareTo(tolerance) <= 0,
                    () -> String.format("Expected approx: %s, but was: %s (diff = %s)", expected, actual, diff));
        }

        @ParameterizedTest
        @CsvSource({
                "0, RAD, 0",
                "0, DEG, 0",
                "0.7853981633974483, RAD, 1",
                "45, DEG, 1",
                "1, RAD, 1.55740772465",
                "1, DEG, 0.017455064928",
                "-1, RAD, -1.55740772465",
                "-1, DEG, -0.01745506492"
        })
        void tanTest(String input, TrigonometricMode trigonometricMode, String expectedStr) {
            BigNumber angle = new BigNumber(input, Locale.US);
            BigNumber result = angle.tan(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US);
            BigDecimal actual = result.toBigDecimal();
            BigDecimal expected = new BigDecimal(expectedStr);
            BigDecimal tolerance = new BigDecimal("1E-8");
            BigDecimal diff = actual.subtract(expected).abs();
            assertTrue(diff.compareTo(tolerance) <= 0,
                    () -> String.format("Expected approx: %s, but was: %s (diff = %s)", expected, actual, diff));
        }

        @ParameterizedTest
        @CsvSource({
                "1, RAD, 0.6420926159",
                "1, DEG, 57.28996163",
                "0.7853981633974483, RAD, 1",
                "45, DEG, 1",
                "-1, RAD, -0.6420926159",
                "-1, DEG, -57.28996163",
        })
        void cotTest(String input, TrigonometricMode trigonometricMode, String expectedStr) {
            BigNumber angle = new BigNumber(input, Locale.US);
            if ("Infinity".equals(expectedStr)) {
                assertThrows(ArithmeticException.class, () -> angle.cot(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US));
            } else {
                BigNumber result = angle.cot(BigNumbers.DEFAULT_MATH_CONTEXT, trigonometricMode, Locale.US);
                BigDecimal actual = result.toBigDecimal();
                BigDecimal expected = new BigDecimal(expectedStr);
                BigDecimal tolerance = new BigDecimal("1E-8");
                BigDecimal diff = actual.subtract(expected).abs();
                assertTrue(diff.compareTo(tolerance) <= 0,
                        () -> String.format("Expected approx: %s, but was: %s (diff = %s)", expected, actual, diff));
            }
        }

    }

    @Nested
    public class TwoDimensionalMath {

        @ParameterizedTest
        @CsvSource({
                "1,1,0.785398163397448309615660845819875721049292349843776455243736148076954101571552249657008706335529267",
                "1,-1,2.356194490192344928846982537459627163147877049531329365731208444230862304714656748971026119006587801",
                "-1,-1,-2.356194490192344928846982537459627163147877049531329365731208444230862304714656748971026119006587801",
                "-1,1,-0.785398163397448309615660845819875721049292349843776455243736148076954101571552249657008706335529267",
        })
        void atan2Test(String inputY, String inputX, String expected) {
            BigNumber y = new BigNumber(inputY, Locale.US);
            BigNumber x = new BigNumber(inputX, Locale.US);

            BigNumber result = y.atan2(x);
            assertEquals(expected, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "0,0",
                "0,1",
                "1,0"
        })
        void atan2InvalidTest(String inputY, String inputX) {
            BigNumber y = new BigNumber(inputY, Locale.US);
            BigNumber x = new BigNumber(inputX, Locale.US);

            assertThrows(IllegalArgumentException.class, () -> y.atan2(x));
        }

    }

    @Nested
    public class SeriesMath {

        @Test
        void summationSingleValue_noVariables() {
            BigNumber start = new BigNumber("3");
            BigNumber result = start.summation("k");
            assertEquals("3", result.toString());
        }

        @Test
        void summationRange_noVariables() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("4");
            BigNumber result = start.summation(end, "k");
            assertEquals("10", result.toString());
        }

        @Test
        void summationRange_withMathContext() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("5");
            BigNumber result = start.summation(end, "1/k", BigNumbers.DEFAULT_MATH_CONTEXT);
            assertEquals("2.2833", result.roundAfterDecimals(4).toString());
        }

        @Test
        void summationRange_withTrigonometricMode_DEG() {
            BigNumber start = new BigNumber("0");
            BigNumber end = new BigNumber("2");
            BigNumber result = start.summation(end, "sin(k)", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.DEG);
            assertEquals("0.05235", result.roundAfterDecimals(5).toString());
        }

        @Test
        void summationRange_withLocale() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("3");
            BigNumber result = start.summation(end, "k^2", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.RAD, Locale.GERMAN);
            assertEquals("14", result.toString());
        }

        @Test
        void summation_withExternalVariables_linearFunction() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("3");
            Map<String, String> vars = Map.of("a", new BigNumber("2").toString());
            BigNumber result = start.summation(end, "a*k", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.RAD, Locale.US, vars);
            assertEquals("12", result.toString());
        }

        @Test
        void summation_withExternalVariables_polynomial() {
            BigNumber start = new BigNumber("0");
            BigNumber end = new BigNumber("2");
            Map<String, String> vars = Map.of("b", new BigNumber("1.5").toString(), "c", new BigNumber("0.5").toString());
            BigNumber result = start.summation(end, "b*k^2 + c", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.RAD, Locale.US, vars);
            assertEquals("9", result.toString());
        }

        @Test
        void summation_endLessThanStart_returnsZero() {
            BigNumber start = new BigNumber("5");
            BigNumber end = new BigNumber("3");
            assertThrows(IllegalArgumentException.class, () -> start.summation(end, "k"));
        }

        @Test
        void summation_emptyExpression_throws() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("5");
            assertThrows(IllegalArgumentException.class, () -> start.summation(end, ""));
        }

        @Test
        void summation_invalidExpression_throws() {
            BigNumber start = new BigNumber("1");
            BigNumber end = new BigNumber("3");
            assertThrows(RuntimeException.class, () -> start.summation(end, "invalidExpr(k)"));
        }

    }

    @Nested
    public class SpecialFunctionMath {

        @ParameterizedTest
        @CsvSource({
                "1, 1",
                "2, 1",
                "3, 2",
                "4, 6",
                "5, 24",
                "0.5, 1.772454",
                "1.5, 0.886227"
        })
        void testGamma(String input, String expected) {
            BigNumber x = new BigNumber(input, Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            BigNumber result = x.gamma(MathContext.DECIMAL128);
            assertEquals(expected, result.roundAfterDecimals(6).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1, 1, 1",
                "2, 2, 0.166667",
                "0.5, 0.5, 3.141593",
                "5, 2, 0.033333"
        })
        void testBeta(String xVal, String yVal, String expected) {
            BigNumber x = new BigNumber(xVal, Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            BigNumber y = new BigNumber(yVal, Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            BigNumber result = x.beta(y, MathContext.DECIMAL128);
            assertEquals(expected, result.roundAfterDecimals(6).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1, 2, 0.5",
                "0.5, 1.5, 1.570796",
        })
        void testBetaSymmetry(String a, String b, String expected) {
            BigNumber x = new BigNumber(a, Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            BigNumber y = new BigNumber(b, Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);

            String betaXY = x.beta(y, MathContext.DECIMAL128).roundAfterDecimals(6).toString();
            String betaYX = y.beta(x, MathContext.DECIMAL128).roundAfterDecimals(6).toString();

            assertEquals(expected, betaXY);
            assertEquals(expected, betaYX);
        }

        @Test
        void testGammaUndefined() {
            BigNumber zero = new BigNumber("0", Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            BigNumber minusOne = new BigNumber("-1", Locale.US, MathContext.DECIMAL128, TrigonometricMode.RAD);
            assertThrows(ArithmeticException.class, () -> zero.gamma(MathContext.DECIMAL128));
            assertThrows(ArithmeticException.class, () -> minusOne.gamma(MathContext.DECIMAL128));
        }

        @ParameterizedTest
        @CsvSource({
                "|-5|,5",
                "|sin(pi/2)|,0.02741213359204429351778848819645260450178515252721841044049369475521833086990385942156241331617943135",
                "3+|-2|,5"
        })
        void absoluteValueWithPipes(String expr, String expected) {
            BigNumber result = new CalculatorEngine().evaluate(expr);
            assertEquals(expected, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "abs(-5),5",
                "abs(sin(pi/2)),0.02741213359204429351778848819645260450178515252721841044049369475521833086990385942156241331617943135",
                "3+abs(-2),5"
        })
        void absoluteValue(String expr, String expected) {
            BigNumber result = new CalculatorEngine().evaluate(expr);
            assertEquals(expected, result.toString());
        }

    }

    @Nested
    public class StatisticMath {

        @ParameterizedTest
        @CsvSource({
                "'1,2,3,4,5',3",
                "'1,2',1.5",
                "'1.5,2.5,3.5',2.5",
                "'-1,-2,-3',-2",
                "'1,-1,2,-2',0",
                "'42',42",
                "'0,0,0',0",
                "'5,5,5,5',5",
                "'2,3,4',3"
        })
        void testAverage(String numbersCsv, String result) {
            List<BigNumber> bigNumbers = stringListToBigNumberList(List.of(numbersCsv.split(",")));
            BigNumber average = bigNumbers.getFirst().average(bigNumbers.subList(1, bigNumbers.size()));
            assertEquals(result, average.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "'1,2,3,4,5',15",
                "'1,2',3",
                "'1.5,2.5,3.5',7.5",
                "'-1,-2,-3',-6",
                "'1,-1,2,-2',0",
                "'42',42",
                "'0,0,0',0",
                "'5,5,5,5',20",
                "'2,3,4',9"
        })
        void testSum(String numbersCsv, String result) {
            List<BigNumber> bigNumbers = stringListToBigNumberList(List.of(numbersCsv.split(",")));
            BigNumber sum = bigNumbers.getFirst().sum(bigNumbers.subList(1, bigNumbers.size()));
            assertEquals(result, sum.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "'1,2,3', 2",
                "'3,1,2', 2",
                "'10,2,8,4,6', 6",
                "'-5,-1,-3', -3",
                "'1,2,3,4', 2.5",
                "'4,1,3,2', 2.5",
                "'10,20,30,40', 25",
                "'-2,-4,-6,-8', -5",
                "'1.5, 3.5, 2.5', 2.5",
                "'2.2, 1.1, 4.4, 3.3', 2.75",
                "'2, 2, 2', 2",
                "'1, 1, 2, 2', 1.5"
        })
        void testMedian(String numbersCsv, String expectedValue) {
            List<BigNumber> bigNumbers = stringListToBigNumberList(List.of(numbersCsv.split(",")));
            BigNumber median = bigNumbers.getFirst().median(bigNumbers.subList(1, bigNumbers.size()));
            assertEquals(expectedValue, median.toString());
        }

        private List<BigNumber> stringListToBigNumberList(List<String> numbers) {
            return numbers.stream().map(BigNumber::new).toList();
        }

    }

    @Nested
    public class Methods {

        @ParameterizedTest
        @CsvSource({
                "3.9, 3",
                "-3.9, -3",
                "5.0, 5"
        })
        void floorTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            assertEquals(expected, number.floor().toString());
        }

        @ParameterizedTest
        @CsvSource({
                "3.1, 4",
                "-3.1, -3",
                "5.0, 5"
        })
        void ceilTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            assertEquals(expected, number.ceil().toString());
        }

        @ParameterizedTest
        @CsvSource({
                "3.7, 3",
                "3.1, 3",
                "5.0, 5",
                "0.99, 0",
                "123456789.987654321, 123456789",
                "-3.7, -3",
                "-3.1, -3",
                "-5.0, -5",
                "-0.99, 0",
                "-123456789.987654321, -123456789",
                "0, 0",
                "7, 7",
                "-7, -7"
        })
        void truncateTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            assertEquals(expected, number.truncate().toString());
        }

        @Test
        void formatToLocaleTest() {
            BigNumber number = new BigNumber("1234.56", Locale.US);
            assertEquals("1.234,56", number.toPrettyString(Locale.GERMANY));
        }

        @Test
        void formatWithGroupingSeparatorsTest() {
            BigNumber number = new BigNumber("1234567.89", Locale.US);
            assertEquals("1,234,567.89", number.toPrettyString());
        }

        @Test
        void toBigDecimalTest() {
            BigNumber number = new BigNumber("123.45");
            assertEquals(new BigDecimal("123.45"), number.toBigDecimal());
        }

        @ParameterizedTest
        @CsvSource({
                "3.1415926535, 180",
                "1.5707963267, 90"
        })
        void toDegreesTest(String rad, String expectedDeg) {
            BigNumber radians = new BigNumber(rad);
            assertEquals(expectedDeg, radians.toDegrees(MathContext.DECIMAL64).roundAfterDecimals(0).toString());
        }

        @ParameterizedTest
        @CsvSource({
                "180, 3.141592653589793",
                "90, 1.570796326794896"
        })
        void toRadiansTest(String deg, String expectedRad) {
            BigNumber degrees = new BigNumber(deg);
            BigNumber result = degrees.toRadians(MathContext.DECIMAL64);
            assertEquals(new BigDecimal(expectedRad).round(MathContext.DECIMAL64), result.toBigDecimal().round(MathContext.DECIMAL64));
        }

        @ParameterizedTest
        @CsvSource({
                "5, 5, true",
                "5, 4, false"
        })
        void isEqualToTest(String a, String b, boolean expected) {
            assertEquals(expected, new BigNumber(a).isEqualTo(new BigNumber(b)));
        }

        @ParameterizedTest
        @CsvSource({
                "3, 5, true",
                "5, 3, false"
        })
        void isLessThanTest(String a, String b, boolean expected) {
            assertEquals(expected, new BigNumber(a).isLessThan(new BigNumber(b)));
        }

        @ParameterizedTest
        @CsvSource({
                "3, 5, true",
                "5, 5, true",
                "6, 5, false"
        })
        void isLessThanOrEqualToTest(String a, String b, boolean expected) {
            assertEquals(expected, new BigNumber(a).isLessThanOrEqualTo(new BigNumber(b)));
        }

        @ParameterizedTest
        @CsvSource({
                "5, 3, true",
                "3, 5, false"
        })
        void isGreaterThanTest(String a, String b, boolean expected) {
            assertEquals(expected, new BigNumber(a).isGreaterThan(new BigNumber(b)));
        }

        @ParameterizedTest
        @CsvSource({
                "5, 3, true",
                "5, 5, true",
                "3, 5, false"
        })
        void isGreaterThanOrEqualToTest(String a, String b, boolean expected) {
            assertEquals(expected, new BigNumber(a).isGreaterThanOrEqualTo(new BigNumber(b)));
        }

        @Test
        void negateTest() {
            BigNumber number = new BigNumber("5");
            BigNumber negated = number.negate();
            assertEquals("-5", negated.toString());
            assertEquals("5", number.toString(), "Original must not be changed");
        }

        @Test
        void negateThisTest() {
            BigNumber number = new BigNumber("5");
            number.negateThis();
            assertEquals("-5", number.toString());
        }

        @Test
        void roundAfterDecimalsTest() {
            BigNumber number = new BigNumber("3.14159");
            assertEquals("3.14", number.roundAfterDecimals(2).toString());
        }

        @Test
        void roundTest() {
            BigNumber number = new BigNumber("3.14159");
            assertEquals("3.142", number.round(new MathContext(4, RoundingMode.HALF_UP)).toString());
        }

        @Test
        void trimTest() {
            BigNumber number = new BigNumber("000123.4500");
            assertEquals("123.45", number.trim().toString());
        }

        @Test
        void intValueTest() {
            assertEquals(123, new BigNumber("123.45").intValue());
        }

        @Test
        void longValueTest() {
            assertEquals(1234567890123L, new BigNumber("1234567890123").longValue());
        }

        @Test
        void floatValueTest() {
            assertEquals(123.45f, new BigNumber("123.45").floatValue());
        }

        @Test
        void doubleValueTest() {
            assertEquals(123.45, new BigNumber("123.45").doubleValue());
        }

        @Test
        void minTest() {
            assertEquals("3", new BigNumber("3").min(new BigNumber("5")).toString());
        }

        @Test
        void maxTest() {
            assertEquals("5", new BigNumber("3").max(new BigNumber("5")).toString());
        }

        @Test
        void hasDecimalsTest() {
            assertTrue(new BigNumber("3.14").hasDecimals());
            assertFalse(new BigNumber("3.00").hasDecimals());
        }

        @Test
        void isIntegerTest() {
            assertTrue(new BigNumber("3.00").isInteger());
            assertFalse(new BigNumber("3.14").isInteger());
        }

        @Test
        void isPositiveTest() {
            assertTrue(new BigNumber("5").isPositive());
            assertFalse(new BigNumber("-5").isPositive());
        }

        @Test
        void toStringVariantsTest() {
            BigNumber number = new BigNumber("1234.56", Locale.US);
            assertEquals("1234.56", number.toString());
            assertEquals("1,234.56", number.toPrettyString(Locale.US));
            assertEquals("1.234,56", number.toPrettyString(Locale.GERMANY));
        }

        @Test
        void toStringWithGroupingTest() {
            BigNumber number = new BigNumber("1234.56", Locale.US);
            assertEquals("1,234.56", number.toPrettyString());
        }

        @Test
        void compareToTest() {
            assertTrue(new BigNumber("5").compareTo(new BigNumber("3")) > 0);
            assertTrue(new BigNumber("3").compareTo(new BigNumber("5")) < 0);
            assertEquals(0, new BigNumber("5").compareTo(new BigNumber("5")));
        }

        @Test
        void cloneTest() {
            BigNumber original = new BigNumber("123.45");
            BigNumber copy = original.clone();
            assertEquals(original.toString(), copy.toString());
            assertNotSame(original, copy);
        }

        @Test
        void isNegativeTest() {
            assertTrue(new BigNumber("-5").isNegative());
            assertFalse(new BigNumber("5").isNegative());
        }

        @ParameterizedTest
        @CsvSource({
                "000, 0",
                "0, 0",
                "00012, 12",
                "00012.34, 12.34",
                "00123.00045, 123.00045"
        })
        void trimLeadingZerosBeforeDecimalPointTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            BigNumber result = number.trimLeadingZerosBeforeDecimalPoint();
            assertEquals(expected, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "12.00123, 12.123",
                "0.0007, 0.7",
                "999.00001, 999.1"
        })
        void trimLeadingZerosAfterDecimalPointTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            BigNumber result = number.trimLeadingZerosAfterDecimalPoint();
            assertEquals(expected, result.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "1200, 12",
                "1200.34, 12.34",
                "1000.0001, 1.0001",
                "0, 0"
        })
        void trimTrailingZerosBeforeDecimalPointTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            BigNumber result = number.trimTrailingZerosBeforeDecimalPoint();

            if (expected.equals("''")) {
                assertTrue(result.toString().isEmpty() || result.toString().equals("0"));
            } else {
                assertEquals(expected, result.toString());
            }
        }

        @ParameterizedTest
        @CsvSource({
                "12.3400, 12.34",
                "0.1200, 0.12",
                "999.00001, 999.00001"
        })
        void trimTrailingZerosAfterDecimalPointTest(String input, String expected) {
            BigNumber number = new BigNumber(input);
            BigNumber result = number.trimTrailingZerosAfterDecimalPoint();
            assertEquals(expected, result.toString());
        }

        @Test
        void trimmingMethods_returnSameInstance_forChaining() {
            BigNumber number = new BigNumber("00012.3400");

            assertSame(number, number.trimLeadingZerosBeforeDecimalPoint());
            assertSame(number, number.trimTrailingZerosAfterDecimalPoint());

            assertEquals("12.34", number.toString());
        }

        @Test
        void signumTest() {
            assertEquals(0, new BigNumber("0").signum());
            assertEquals(1, new BigNumber("1").signum());
            assertEquals(1, new BigNumber("0.123").signum());
            assertEquals(1, new BigNumber("1.123").signum());
            assertEquals(-1, new BigNumber("-0.123").signum());
            assertEquals(-1, new BigNumber("-1").signum());
            assertEquals(-1, new BigNumber("-1..123").signum());
        }

    }

}

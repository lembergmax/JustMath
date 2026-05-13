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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class BigNumberMatrixTest {

    private static final Locale locale = Locale.US;

    private static void assertMatrixEquals(BigNumberMatrix expected, BigNumberMatrix actual) {
        assertEquals(expected.toPlainDataString(), actual.toPlainDataString());
    }

    private static BigNumberMatrix matrix(String str) {
        return new BigNumberMatrix(str, locale);
    }

    @ParameterizedTest(name = "[{index}] Add {0} + {1} = {2}")
    @CsvSource({
            "'1,2;3,4', '5,6;7,8', '6,8;10,12'",
            "'0,0;0,0', '0,0;0,0', '0,0;0,0'",
            "'1', '2', '3'",
            "'1,2,3;4,5,6', '6,5,4;3,2,1', '7,7,7;7,7,7'"
    })
    void testAdd(String a, String b, String expected) {
        BigNumberMatrix m1 = new BigNumberMatrix(a, locale);
        BigNumberMatrix m2 = new BigNumberMatrix(b, locale);
        BigNumberMatrix expectedMatrix = new BigNumberMatrix(expected, locale);

        BigNumberMatrix result = m1.add(m2);
        assertMatrixEquals(expectedMatrix, result);
    }

    @ParameterizedTest(name = "[{index}] Subtract {0} - {1} = {2}")
    @CsvSource({
            "'5,6;7,8', '1,2;3,4', '4,4;4,4'",
            "'1', '2', '-1'",
            "'0,0,0', '1,2,3', '-1,-2,-3'"
    })
    void testSubtract(String a, String b, String expected) {
        BigNumberMatrix m1 = new BigNumberMatrix(a, locale);
        BigNumberMatrix m2 = new BigNumberMatrix(b, locale);
        BigNumberMatrix expectedMatrix = new BigNumberMatrix(expected, locale);

        BigNumberMatrix result = m1.subtract(m2);
        assertMatrixEquals(expectedMatrix, result);
    }

    @ParameterizedTest(name = "[{index}] Multiply {0} * {1} = {2}")
    @CsvSource({
            "'1,2;3,4', '2,0;1,2', '4,4;10,8'",
            "'5', '6', '30'",
            "'1,0;0,1', '9,8;7,6', '9,8;7,6'",
            "'1,2,3', '1;2;3', '14'"
    })
    void testMultiply(String a, String b, String expected) {
        BigNumberMatrix m1 = new BigNumberMatrix(a, locale);
        BigNumberMatrix m2 = new BigNumberMatrix(b, locale);
        BigNumberMatrix expectedMatrix = new BigNumberMatrix(expected, locale);

        BigNumberMatrix result = m1.multiply(m2);
        assertMatrixEquals(expectedMatrix, result);
    }

    @ParameterizedTest(name = "[{index}] Divide {0} / {1} = {2}")
    @CsvSource({
            "'6,4;2,8', '2,2;2,4', '3,2;1,2'",
            "'10', '2', '5'",
            "'0', '1', '0'"
    })
    void testDivide(String a, String b, String expected) {
        BigNumberMatrix m1 = new BigNumberMatrix(a, locale);
        BigNumberMatrix m2 = new BigNumberMatrix(b, locale);
        BigNumberMatrix expectedMatrix = new BigNumberMatrix(expected, locale);

        BigNumberMatrix result = m1.divide(m2);
        assertMatrixEquals(expectedMatrix, result);
    }

    @ParameterizedTest(name = "[{index}] Division by zero should fail")
    @CsvSource({
            "'1', '0'",
            "'5,10', '0,0'",
            "'1,2;3,4', '0,1;2,0'"
    })
    void testDivisionByZero(String a, String b) {
        BigNumberMatrix m1 = new BigNumberMatrix(a, locale);
        BigNumberMatrix m2 = new BigNumberMatrix(b, locale);
        assertThrows(ArithmeticException.class, () -> m1.divide(m2));
    }

    @ParameterizedTest(name = "[{index}] Transpose {0} = {1}")
    @CsvSource({
            "'1,2,3;4,5,6', '1,4;2,5;3,6'",
            "'9,8', '9;8'",
            "'7', '7'"
    })
    void testTranspose(String input, String expected) {
        BigNumberMatrix matrix = new BigNumberMatrix(input, locale);
        BigNumberMatrix expectedMatrix = new BigNumberMatrix(expected, locale);

        BigNumberMatrix result = matrix.transpose();
        assertMatrixEquals(expectedMatrix, result);
    }

    @ParameterizedTest(name = "[{index}] Scalar multiply {0} * {1} = {2}")
    @CsvSource({
            "'1,2;3,4', '2', '2,4;6,8'",
            "'5', '-3', '-15'",
            "'0,0;0,0', '100', '0,0;0,0'",
            "'1.5,2.5;3.5,4.5', '2', '3.0,5.0;7.0,9.0'",
            "'1,2,3;4,5,6', '0', '0,0,0;0,0,0'"
    })
    void testScalarMultiply(String matrixStr, String scalarStr, String expectedStr) {
        BigNumberMatrix matrix = new BigNumberMatrix(matrixStr, locale);
        BigNumber scalar = new BigNumber(scalarStr, locale);
        BigNumberMatrix expected = new BigNumberMatrix(expectedStr, locale);

        BigNumberMatrix result = matrix.scalarMultiply(scalar);
        assertMatrixEquals(expected, result);
    }

    @ParameterizedTest(name = "[{index}] Negate {0} = {1}")
    @CsvSource({
            "'1,2;3,4', '-1,-2;-3,-4'",
            "'0', '0'",
            "'-1,-2,-3;4,5,6', '1,2,3;-4,-5,-6'",
            "'0,0;0,0', '0,0;0,0'",
            "'7.7,-8.8;9.9,-10.1', '-7.7,8.8;-9.9,10.1'"
    })
    void testNegate(String input, String expectedStr) {
        BigNumberMatrix matrix = new BigNumberMatrix(input, locale);
        BigNumberMatrix expected = new BigNumberMatrix(expectedStr, locale);

        BigNumberMatrix result = matrix.negate();
        assertMatrixEquals(expected, result);
    }

    @ParameterizedTest(name = "[{index}] Invalid Matrix Constructor input: {0}")
    @CsvSource({
            "'1,2;3'",
            "'a,b;c,d'",
            "'  '",
            "'1,,2;3,4'",
            "'1,2;3,4,5'",
            "';1;2;3'",
            "'1;2;3;'",
            "'1,2;3,abc'",
            "'1,2;3,'",
            "''",
    })
    void testInvalidMatrixConstructor(String input) {
        assertThrows(IllegalArgumentException.class, () -> new BigNumberMatrix(input, locale));
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2;3,4', true",
            "'1,2,3;4,5,6', false",
            "'1,0;0,1', true"
    })
    void testIsSquare(String matrixStr, boolean expected) {
        assertEquals(expected, matrix(matrixStr).isSquare());
    }

    @ParameterizedTest
    @CsvSource({
            "'0,0;0,0', true",
            "'1,0;0,1', false",
            "'0', true",
            "'0,1', false"
    })
    void testIsZeroMatrix(String matrixStr, boolean expected) {
        assertEquals(expected, matrix(matrixStr).isZeroMatrix());
    }

    @ParameterizedTest
    @CsvSource({
            "'1,0;0,1', true",
            "'1,2;2,1', true",
            "'1,2;3,1', false",
            "'1,2,3;2,4,5;3,5,6', true"
    })
    void testIsSymmetric(String matrixStr, boolean expected) {
        BigNumberMatrix m = matrix(matrixStr);
        assertEquals(expected, m.isSymmetric());
    }

    @ParameterizedTest
    @CsvSource({
            "'1,0;0,1', true",
            "'1,2;3,4', false",
            "'1', true",
            "'1,0,0;0,1,0;0,0,1', true",
            "'0,0;0,0', false"
    })
    void testIsIdentityMatrix(String matrixStr, boolean expected) {
        assertEquals(expected, matrix(matrixStr).isIdentityMatrix());
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2;3,4', '10'",
            "'0,0;0,0', '0'",
            "'1', '1'",
            "'2,3,4', '9'"
    })
    void testSumElements(String matrixStr, String expectedSum) {
        BigNumberMatrix m = matrix(matrixStr);
        assertEquals(expectedSum, m.sumElements().toString());
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2;3,4', '4'",
            "'-5,-6;-7,-8', '-5'",
            "'0,100;200,3', '200'",
            "'3.14,2.71;0.99,4.01', '4.01'"
    })
    void testMax(String matrixStr, String expectedMax) {
        assertEquals(expectedMax, matrix(matrixStr).max().toString());
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2;3,4', '1,2,3,4'",
            "'5', '5'",
            "'1,0,0;0,1,0;0,0,1', '1,0,0,0,1,0,0,0,1'"
    })
    void testFlatten(String matrixStr, String expectedFlat) {
        BigNumberMatrix m = matrix(matrixStr);
        String result = String.join(",", m.flatten().stream().map(BigNumber::toString).toList());
        assertEquals(expectedFlat, result);
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2;3,4', '1,2;3,4', true",
            "'1,2;3,4', '4,3;2,1', false",
            "'1,2;3,4', '1,2;3,5', false"
    })
    void testEqualsMatrix(String a, String b, boolean expected) {
        BigNumberMatrix m1 = matrix(a);
        BigNumberMatrix m2 = matrix(b);
        assertEquals(expected, m1.equalsMatrix(m2));
    }

    @Test
    void testCloneCreatesEqualMatrix() {
        BigNumberMatrix m1 = matrix("1,2;3,4");
        BigNumberMatrix m2 = m1.clone();
        assertNotSame(m1, m2);
        assertMatrixEquals(m1, m2);
    }

    @ParameterizedTest(name = "[{index}] Zero-sized Matrix {0}")
    @CsvSource({
            "0, 0",
            "1, 1"
    })
    void testZeroSizedMatrix(BigNumber rows, BigNumber cols) {
        BigNumberMatrix matrix = new BigNumberMatrix(rows, cols, locale);
        assertEquals(rows, matrix.getRows());
        assertEquals(cols, matrix.getColumns());
    }

    @Test
    void testDeterminant() {
        BigNumberMatrix m1 = new BigNumberMatrix("1,2;3,4", locale);
        assertEquals(new BigNumber("-2", locale).toPrettyString(), m1.determinant().toPrettyString());

        BigNumberMatrix m2 = new BigNumberMatrix("5", locale);
        assertEquals(new BigNumber("5", locale).toPrettyString(), m2.determinant().toPrettyString());
    }

    @Test
    void testInverse() {
        BigNumberMatrix m = new BigNumberMatrix("4,7;2,6", locale);
        BigNumberMatrix inv = m.inverse();
        BigNumberMatrix expected = new BigNumberMatrix("0.6,-0.7;-0.2,0.4", locale);
        assertMatrixEquals(expected, inv);
    }

    @Test
    void testPower() {
        BigNumberMatrix m = new BigNumberMatrix("2,0;0,2", locale);
        BigNumberMatrix squared = m.power(new BigNumber("2", locale));
        BigNumberMatrix expected = new BigNumberMatrix("4,0;0,4", locale);
        assertMatrixEquals(expected, squared);
    }

    @Test
    void testTrace() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", locale);
        assertEquals(new BigNumber("5", locale).toPrettyString(), m.trace().toPrettyString());
    }

    @Test
    void testIsSymmetric() {
        assertTrue(new BigNumberMatrix("1,2;2,1", locale).isSymmetric());
        assertFalse(new BigNumberMatrix("1,2;3,4", locale).isSymmetric());
    }

    @Test
    void testSumElements() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", locale);
        assertEquals(new BigNumber("10", locale).toPrettyString(), m.sumElements().toPrettyString());
    }

    @Test
    void testMax() {
        BigNumberMatrix m = new BigNumberMatrix("1,99;3,4", locale);
        assertEquals(new BigNumber("99", locale).toPrettyString(), m.max().toPrettyString());
    }

    @Test
    void testIsSquare() {
        assertTrue(new BigNumberMatrix("1,2;3,4", locale).isSquare());
        assertFalse(new BigNumberMatrix("1,2,3;4,5,6", locale).isSquare());
    }

    @Test
    void testIsZeroMatrix() {
        assertTrue(new BigNumberMatrix("0,0;0,0", locale).isZeroMatrix());
        assertFalse(new BigNumberMatrix("0,1;0,0", locale).isZeroMatrix());
    }

    @Test
    void testIsIdentityMatrix() {
        assertTrue(new BigNumberMatrix("1,0;0,1", locale).isIdentityMatrix());
        assertFalse(new BigNumberMatrix("1,1;0,1", locale).isIdentityMatrix());
    }

    @Test
    void testFlatten() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", locale);
        assertEquals(
                List.of(new BigNumber("1", locale), new BigNumber("2", locale),
                        new BigNumber("3", locale), new BigNumber("4", locale)).toString(),
                m.flatten().toString()
        );
    }

    @Test
    void tst() {
        // Create a 2x2 matrix from a string
        BigNumberMatrix a = new BigNumberMatrix("1,2;3,4", Locale.US);

        // Compute the determinant
        BigNumber det = a.determinant();
        System.out.println(det);
        // -2

        // Compute the inverse
        BigNumberMatrix inv = a.inverse();
        System.out.println(inv.toPlainDataString());
        // [[-2.0, 1.0], [1.5, -0.5]]

        // Multiply matrices
        BigNumberMatrix b = new BigNumberMatrix("5,6;7,8", Locale.US);
        BigNumberMatrix c = a.multiply(b);
        System.out.println(c.toPlainDataString());
        // [[19, 22], [43, 50]]

        // Check identity matrix
        BigNumberMatrix i = new BigNumberMatrix("1,0;0,1", Locale.US);
        System.out.println(i.isIdentityMatrix());
    }

    @Test
    void testEqualsMatrix() {
        BigNumberMatrix m1 = new BigNumberMatrix("1,2;3,4", locale);
        BigNumberMatrix m2 = new BigNumberMatrix("1,2;3,4", locale);
        BigNumberMatrix m3 = new BigNumberMatrix("1,2;3,5", locale);

        assertTrue(m1.equalsMatrix(m2));
        assertFalse(m1.equalsMatrix(m3));
    }

    @Test
    void testForEachElementAndIndex() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", locale);

        // check that all elements are visited
        List<String> visited = new ArrayList<>();
        m.forEachElement((i, j, value) -> visited.add(i + "," + j + "=" + value.toString()));
        assertTrue(visited.contains("0,0=1"));
        assertTrue(visited.contains("1,1=4"));

        // check forEachIndex
        List<String> indices = new ArrayList<>();
        m.forEachIndex((i, j) -> indices.add(i + "," + j));
        assertTrue(indices.contains("0,1"));
        assertTrue(indices.contains("1,0"));
    }

    @Test
    void testToStringAndPlainDataString() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", locale);
        String plain = m.toPlainDataString();
        assertTrue(plain.contains("["));
        assertTrue(plain.contains("1"));
        assertTrue(plain.contains("4"));

        String debug = m.toString();
        assertTrue(debug.contains("BigNumberMatrix"));
        assertTrue(debug.contains("2x2"));
    }

    @Test
    void testClone() {
        BigNumberMatrix original = new BigNumberMatrix("1,2;3,4", locale);
        BigNumberMatrix copy = original.clone();

        assertMatrixEquals(original, copy);
        assertNotSame(original, copy); // deep copy
    }

    // ---------------------------------------------------------------------------------------
    // Matrix product: dimension matrix
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] Valid matrix product {0} * {1} = {2}")
    @CsvSource({
            // 2x2 * 2x1 -> 2x1 (matrix times column vector)
            "'2,5;1,3', '1;2', '12;7'",
            // 1x2 * 2x2 -> 1x2 (row vector times matrix)
            "'1,2', '2,5;1,3', '4,11'",
            // 2x3 * 3x2 -> 2x2
            "'1,2,3;4,5,6', '7,8;9,10;11,12', '58,64;139,154'",
            // 3x2 * 2x3 -> 3x3
            "'1,2;3,4;5,6', '7,8,9;10,11,12', '27,30,33;61,68,75;95,106,117'",
            // 2x2 * 2x2 -> 2x2
            "'1,2;3,4', '5,6;7,8', '19,22;43,50'"
    })
    void testMatrixMultiplyValidShapes(String a, String b, String expected) {
        BigNumberMatrix result = matrix(a).multiply(matrix(b));
        assertMatrixEquals(matrix(expected), result);
    }

    @ParameterizedTest(name = "[{index}] Invalid matrix product {0} * {1} must throw")
    @CsvSource({
            // 2x1 * 2x2 — column vector cannot be left of 2x2 (no auto-transpose)
            "'1;2', '2,5;1,3'",
            // 2x3 * 2x3 — inner dimensions mismatch
            "'1,2,3;4,5,6', '1,2,3;4,5,6'",
            // 3x2 * 3x2 — inner dimensions mismatch
            "'1,2;3,4;5,6', '1,2;3,4;5,6'",
            // 1x3 * 2x1 — inner dimensions mismatch
            "'1,2,3', '4;5'"
    })
    void testMatrixMultiplyInvalidShapesThrow(String a, String b) {
        BigNumberMatrix left = matrix(a);
        BigNumberMatrix right = matrix(b);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> left.multiply(right));
        // Message must include both dimensions and explain the rule.
        String msg = ex.getMessage();
        assertTrue(msg.contains("dimension mismatch"),
                () -> "Error message must mention 'dimension mismatch'. Got: " + msg);
        assertTrue(msg.contains("Left columns must equal right rows"),
                () -> "Error message must explain the rule. Got: " + msg);
        assertTrue(msg.contains(left.getRows() + "x" + left.getColumns()),
                () -> "Error message must include left shape. Got: " + msg);
        assertTrue(msg.contains(right.getRows() + "x" + right.getColumns()),
                () -> "Error message must include right shape. Got: " + msg);
    }

    @Test
    void testColumnVectorTimesRowMatrixIsRejected_2x1_times_2x2() {
        // 2x1 * 2x2 is NOT a valid matrix product. The engine must reject it (no silent auto-transpose).
        BigNumberMatrix col = matrix("1;2");
        BigNumberMatrix m = matrix("2,5;1,3");
        assertThrows(IllegalArgumentException.class, () -> col.multiply(m));
    }

    @Test
    void testRowVectorTimesMatrixIsValid_1x2_times_2x2() {
        // The intended shape: a row vector left of a matrix.
        BigNumberMatrix row = matrix("1,2");
        BigNumberMatrix m = matrix("2,5;1,3");
        BigNumberMatrix expected = matrix("4,11");
        assertMatrixEquals(expected, row.multiply(m));
    }

    @Test
    void testMatrixTimesColumnVectorIsValid_2x2_times_2x1() {
        BigNumberMatrix m = matrix("2,5;1,3");
        BigNumberMatrix col = matrix("1;2");
        BigNumberMatrix expected = matrix("12;7");
        assertMatrixEquals(expected, m.multiply(col));
    }

    // ---------------------------------------------------------------------------------------
    // Element-wise dimension mismatch
    // ---------------------------------------------------------------------------------------

    @Test
    void testElementWiseAddDimensionMismatchThrows() {
        BigNumberMatrix a = matrix("1,2;3,4");
        BigNumberMatrix b = matrix("1,2,3;4,5,6");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> a.add(b));
        assertTrue(ex.getMessage().contains("dimension mismatch"), ex.getMessage());
    }

    @Test
    void testElementWiseSubtractDimensionMismatchThrows() {
        BigNumberMatrix a = matrix("1,2;3,4");
        BigNumberMatrix b = matrix("1,2,3;4,5,6");
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }

    @Test
    void testElementWiseDivideDimensionMismatchThrows() {
        BigNumberMatrix a = matrix("1,2;3,4");
        BigNumberMatrix b = matrix("1,2,3;4,5,6");
        assertThrows(IllegalArgumentException.class, () -> a.divide(b));
    }

    // ---------------------------------------------------------------------------------------
    // Non-square preconditions
    // ---------------------------------------------------------------------------------------

    @Test
    void testDeterminantOfNonSquareThrows() {
        assertThrows(IllegalArgumentException.class, () -> matrix("1,2,3;4,5,6").determinant());
    }

    @Test
    void testInverseOfNonSquareThrows() {
        assertThrows(IllegalArgumentException.class, () -> matrix("1,2,3;4,5,6").inverse());
    }

    @Test
    void testPowerOfNonSquareThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> matrix("1,2,3;4,5,6").power(new BigNumber("2", locale)));
    }

    @Test
    void testTraceOfNonSquareThrows() {
        assertThrows(IllegalArgumentException.class, () -> matrix("1,2,3;4,5,6").trace());
    }

    @Test
    void testInverseOfSingularThrows() {
        // [[1,2],[2,4]] is singular (rows are linearly dependent).
        assertThrows(IllegalArgumentException.class, () -> matrix("1,2;2,4").inverse());
    }

    @Test
    void testPowerWithNegativeExponentThrows() {
        BigNumberMatrix m = matrix("1,2;3,4");
        assertThrows(IllegalArgumentException.class,
                () -> m.power(new BigNumber("-1", locale)));
    }

    @Test
    void testPowerWithNonIntegerExponentThrows() {
        BigNumberMatrix m = matrix("1,2;3,4");
        assertThrows(IllegalArgumentException.class,
                () -> m.power(new BigNumber("1.5", locale)));
    }

    @Test
    void testPowerZeroIsIdentity() {
        BigNumberMatrix m = matrix("1,2;3,4");
        BigNumberMatrix expected = matrix("1,0;0,1");
        assertMatrixEquals(expected, m.power(new BigNumber("0", locale)));
    }

    @Test
    void testInverseOf1x1Matrix() {
        // Direct adjugate path used to mis-compute the inverse because det of the empty 0x0 minor
        // was 0 instead of 1. Verify that [[5]] inverts to [[1/5]] = [[0.2]].
        BigNumberMatrix m = matrix("5");
        BigNumberMatrix inv = m.inverse();
        // Multiplying by the original must recover the 1x1 identity.
        BigNumberMatrix product = m.multiply(inv);
        assertMatrixEquals(matrix("1"), product);
    }

    // ---------------------------------------------------------------------------------------
    // Determinant: 3x3
    // ---------------------------------------------------------------------------------------

    @Test
    void testDeterminant3x3() {
        // det([[6,1,1],[4,-2,5],[2,8,7]]) = -306
        BigNumberMatrix m = matrix("6,1,1;4,-2,5;2,8,7");
        assertEquals(new BigNumber("-306", locale).toPrettyString(),
                m.determinant().toPrettyString());
    }

    // ---------------------------------------------------------------------------------------
    // Scalar operations
    // ---------------------------------------------------------------------------------------

    @Test
    void testScalarMultiplyDoesNotMutateOriginal() {
        BigNumberMatrix m = matrix("1,2;3,4");
        String before = m.toPlainDataString();
        m.scalarMultiply(new BigNumber("10", locale));
        assertEquals(before, m.toPlainDataString(), "scalarMultiply must not mutate the original");
    }

    @Test
    void testTransposeDoesNotMutateOriginal() {
        BigNumberMatrix m = matrix("1,2;3,4");
        String before = m.toPlainDataString();
        m.transpose();
        assertEquals(before, m.toPlainDataString(), "transpose must not mutate the original");
    }

    @Test
    void testAddDoesNotMutateOriginals() {
        BigNumberMatrix a = matrix("1,2;3,4");
        BigNumberMatrix b = matrix("5,6;7,8");
        String beforeA = a.toPlainDataString();
        String beforeB = b.toPlainDataString();
        a.add(b);
        assertEquals(beforeA, a.toPlainDataString());
        assertEquals(beforeB, b.toPlainDataString());
    }

    @Test
    void testMultiplyDoesNotMutateOriginals() {
        BigNumberMatrix a = matrix("1,2;3,4");
        BigNumberMatrix b = matrix("5,6;7,8");
        String beforeA = a.toPlainDataString();
        String beforeB = b.toPlainDataString();
        a.multiply(b);
        assertEquals(beforeA, a.toPlainDataString());
        assertEquals(beforeB, b.toPlainDataString());
    }

    // ---------------------------------------------------------------------------------------
    // Parser edge cases
    // ---------------------------------------------------------------------------------------

    @Test
    void testParserUsesGivenLocaleForDecimalPoint() {
        // US locale uses '.' as decimal separator. Matrix entries must be parsed with the locale.
        BigNumberMatrix m = new BigNumberMatrix("1.5,2.5;3.5,4.5", Locale.US);
        BigNumber sum = m.sumElements();
        assertEquals(new BigNumber("12", Locale.US).toPrettyString(), sum.toPrettyString());
    }

    @Test
    void testParserRejectsLeadingSeparator() {
        assertThrows(IllegalArgumentException.class, () -> new BigNumberMatrix(";1,2;3,4", locale));
    }

    @Test
    void testListConstructorRejectsRaggedRows() {
        List<List<BigNumber>> ragged = new ArrayList<>();
        ragged.add(List.of(new BigNumber("1", locale), new BigNumber("2", locale)));
        ragged.add(List.of(new BigNumber("3", locale))); // shorter row
        assertThrows(IllegalArgumentException.class, () -> new BigNumberMatrix(ragged, locale));
    }

    @Test
    void testListConstructorRejectsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new BigNumberMatrix(new ArrayList<List<BigNumber>>(), locale));
    }

    // ---------------------------------------------------------------------------------------
    // Localized error messages (English + German)
    // ---------------------------------------------------------------------------------------

    @Test
    void testMultiplyErrorIsEnglishForUSLocale() {
        BigNumberMatrix left = new BigNumberMatrix("1;2", Locale.US);
        BigNumberMatrix right = new BigNumberMatrix("2,5;1,3", Locale.US);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> left.multiply(right));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Matrix multiplication dimension mismatch"), msg);
        assertTrue(msg.contains("Left columns must equal right rows"), msg);
        assertTrue(msg.contains("2x1"), msg);
        assertTrue(msg.contains("2x2"), msg);
    }

    @Test
    void testMultiplyErrorIsGermanForGermanLocale() {
        BigNumberMatrix left = new BigNumberMatrix("1;2", Locale.GERMANY);
        BigNumberMatrix right = new BigNumberMatrix("2,5;1,3", Locale.GERMANY);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> left.multiply(right));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Matrixmultiplikation"), msg);
        assertTrue(msg.contains("Dimensionsfehler"), msg);
        assertTrue(msg.contains("linke Matrix") || msg.contains("Spaltenanzahl"), msg);
    }

    @Test
    void testElementWiseErrorIsGermanForGermanLocale() {
        BigNumberMatrix a = new BigNumberMatrix("1;2;3;4", Locale.GERMANY); // 4x1
        BigNumberMatrix b = new BigNumberMatrix("1;2;3", Locale.GERMANY);   // 3x1
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> a.add(b));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Elementweise") || msg.contains("Dimensionsfehler"), msg);
    }

    @Test
    void testNotSquareErrorIsGermanForGermanLocale() {
        BigNumberMatrix m = new BigNumberMatrix("1;2;3", Locale.GERMANY); // 3x1, not square
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, m::determinant);
        assertTrue(ex.getMessage().contains("quadratisch"), ex.getMessage());
    }

    @Test
    void testSingularInverseErrorIsGermanForGermanLocale() {
        // 2x2 singular matrix; rows linearly dependent.
        BigNumberMatrix m = new BigNumberMatrix("1;2;2;4", Locale.GERMANY); // 4x1, not square -> wrong test
        // Build a real singular matrix via the data string in US format then re-wrap with German locale.
        BigNumberMatrix singular = new BigNumberMatrix("1,2;2,4", Locale.US);
        // Use list ctor to rebuild with German locale so the localized error message is selected.
        BigNumberMatrix german = new BigNumberMatrix(singular.getData(), Locale.GERMANY);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, german::inverse);
        assertTrue(ex.getMessage().contains("nicht invertierbar"), ex.getMessage());
    }

    @Test
    void testIndexOutOfBoundsLocalizedGerman() {
        BigNumberMatrix m = new BigNumberMatrix("1,2;3,4", Locale.GERMANY);
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                () -> m.get(new BigNumber("5", Locale.GERMANY), new BigNumber("0", Locale.GERMANY)));
        assertTrue(ex.getMessage().contains("Zeilenindex"), ex.getMessage());
    }

}

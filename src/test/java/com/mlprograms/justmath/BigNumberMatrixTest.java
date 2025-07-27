package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

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
		"'1;2;3;'",
		"'1,2;3,abc'",
		"'1,2;3,'",
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
		boolean result;
		try {
			result = m.isSymmetric();
		} catch (IllegalStateException e) {
			result = false;
		}
		assertEquals(expected, result);
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
		assertEquals(new BigNumber(expectedSum, locale), m.sumElements());
	}

	@ParameterizedTest
	@CsvSource({
		"'1,2;3,4', '4'",
		"'-5,-6;-7,-8', '-5'",
		"'0,100;200,3', '200'",
		"'3.14,2.71;0.99,4.01', '4.01'"
	})
	void testMax(String matrixStr, String expectedMax) {
		assertEquals(new BigNumber(expectedMax, locale), matrix(matrixStr).max());
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
		boolean result;
		try {
			result = m1.equalsMatrix(m2);
		} catch (IllegalStateException e) {
			result = false;
		}
		assertEquals(expected, result);
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

}

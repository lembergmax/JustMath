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

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberMatrix;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigNumberMatrixTest {

	private static final Locale locale = Locale.US;

	private static Stream<org.junit.jupiter.params.provider.Arguments> matrixAdditionProvider() {
		return Stream.of(
			org.junit.jupiter.params.provider.Arguments.of(
				List.of(List.of("1", "2"), List.of("3", "4")),
				List.of(List.of("5", "6"), List.of("7", "8")),
				List.of(List.of("6", "8"), List.of("10", "12"))
			),
			org.junit.jupiter.params.provider.Arguments.of(
				List.of(List.of("0", "0"), List.of("0", "0")),
				List.of(List.of("1", "1"), List.of("1", "1")),
				List.of(List.of("1", "1"), List.of("1", "1"))
			)
		);
	}

	@ParameterizedTest
	@MethodSource("matrixAdditionProvider")
	void testAdd(List<List<String>> a, List<List<String>> b, List<List<String>> expected) {
		BigNumberMatrix m1 = createMatrix(a);
		BigNumberMatrix m2 = createMatrix(b);
		BigNumberMatrix result = m1.add(m2);
		assertMatrixEquals(expected, result);
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> matrixSubtractionProvider() {
		return Stream.of(
			org.junit.jupiter.params.provider.Arguments.of(
				List.of(List.of("5", "6"), List.of("7", "8")),
				List.of(List.of("1", "2"), List.of("3", "4")),
				List.of(List.of("4", "4"), List.of("4", "4"))
			)
		);
	}

	@ParameterizedTest
	@MethodSource("matrixSubtractionProvider")
	void testSubtract(List<List<String>> a, List<List<String>> b, List<List<String>> expected) {
		BigNumberMatrix m1 = createMatrix(a);
		BigNumberMatrix m2 = createMatrix(b);
		BigNumberMatrix result = m1.subtract(m2);
		assertMatrixEquals(expected, result);
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> matrixMultiplicationProvider() {
		return Stream.of(
			org.junit.jupiter.params.provider.Arguments.of(
				List.of(List.of("1", "2"), List.of("3", "4")), // 2x2
				List.of(List.of("2", "0"), List.of("1", "2")), // 2x2
				List.of(List.of("4", "4"), List.of("10", "8")) // result
			)
		);
	}

	@ParameterizedTest
	@MethodSource("matrixMultiplicationProvider")
	void testMultiply(List<List<String>> a, List<List<String>> b, List<List<String>> expected) {
		BigNumberMatrix m1 = createMatrix(a);
		BigNumberMatrix m2 = createMatrix(b);
		BigNumberMatrix result = m1.multiply(m2);
		assertMatrixEquals(expected, result);
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> matrixDivisionProvider() {
		return Stream.of(
			org.junit.jupiter.params.provider.Arguments.of(
				List.of(List.of("6", "4"), List.of("2", "8")),
				List.of(List.of("2", "2"), List.of("2", "4")),
				List.of(List.of("3", "2"), List.of("1", "2"))
			)
		);
	}

	@ParameterizedTest
	@MethodSource("matrixDivisionProvider")
	void testDivide(List<List<String>> a, List<List<String>> b, List<List<String>> expected) {
		BigNumberMatrix m1 = createMatrix(a);
		BigNumberMatrix m2 = createMatrix(b);
		BigNumberMatrix result = m1.divide(m2);
		assertMatrixEquals(expected, result);
	}

	private static BigNumberMatrix createMatrix(List<List<String>> rawData) {
		int rows = rawData.size();
		int cols = rawData.getFirst().size();
		BigNumberMatrix matrix = new BigNumberMatrix(
			new BigNumber(String.valueOf(rows), locale),
			new BigNumber(String.valueOf(cols), locale),
			locale
		);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix.set(
					new BigNumber(String.valueOf(i), locale),
					new BigNumber(String.valueOf(j), locale),
					new BigNumber(rawData.get(i).get(j), locale)
				);
			}
		}
		return matrix;
	}

	private static void assertMatrixEquals(List<List<String>> expected, BigNumberMatrix actual) {
		int rows = expected.size();
		int cols = expected.getFirst().size();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				String expectedValue = expected.get(i).get(j);
				BigNumber actualValue = actual.get(
					new BigNumber(String.valueOf(i), locale),
					new BigNumber(String.valueOf(j), locale)
				);
				assertEquals(expectedValue, actualValue.toString(), "Mismatch at (" + i + "," + j + ")");
			}
		}
	}
}

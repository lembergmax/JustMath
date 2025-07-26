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

package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberMatrix;
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NonNull;

import java.util.Locale;

public class MatrixMath {

	/**
	 * Computes the element-wise addition of two matrices.
	 * <p>
	 * Given two matrices <em>A</em> and <em>B</em> of the same dimensions (m × n),
	 * this method returns their sum matrix <em>C</em> where each element
	 * <em>C<sub>ij</sub> = A<sub>ij</sub> + B<sub>ij</sub></em>.
	 * Matrix addition is defined only when both matrices have the same number of rows and columns.
	 * </p>
	 *
	 * @param augend
	 * 	the first matrix (<em>A</em>), must have the same dimensions as {@code addend}
	 * @param addend
	 * 	the second matrix (<em>B</em>), must have the same dimensions as {@code augend}
	 * @param locale
	 * 	the locale to be used for internal number formatting and operations
	 *
	 * @return a new matrix <em>C</em> representing the sum of <em>A</em> and <em>B</em>
	 *
	 * @throws IllegalArgumentException
	 * 	if the dimensions of the two matrices differ
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 */
	public static BigNumberMatrix add(@NonNull final BigNumberMatrix augend, @NonNull final BigNumberMatrix addend, @NonNull final Locale locale) {
		checkParamsForSameMatrixSize(augend, addend);

		BigNumberMatrix result = new BigNumberMatrix(augend.getRows(), augend.getColumns(), locale);

		augend.forEachElement((row, col, valueA) -> {
			BigNumber valueB = addend.get(row, col);
			result.set(row, col, valueA.add(valueB));
		});

		return result;
	}

	/**
	 * Computes the element-wise subtraction of one matrix from another.
	 * <p>
	 * Given two matrices <em>A</em> (minuend) and <em>B</em> (subtrahend) of the same dimensions (m × n),
	 * this method returns their difference matrix <em>C</em> where each element
	 * <em>C<sub>ij</sub> = A<sub>ij</sub> - B<sub>ij</sub></em>.
	 * Subtraction is defined only when both matrices have the same number of rows and columns.
	 * </p>
	 *
	 * @param minuend
	 * 	the matrix <em>A</em> from which to subtract
	 * @param subtrahend
	 * 	the matrix <em>B</em> to subtract from {@code minuend}
	 *
	 * @return a new matrix <em>C</em> representing the difference <em>A - B</em>
	 *
	 * @throws IllegalArgumentException
	 * 	if the dimensions of the two matrices differ
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 */
	public static BigNumberMatrix subtract(@NonNull final BigNumberMatrix minuend, @NonNull final BigNumberMatrix subtrahend) {
		checkParamsForSameMatrixSize(minuend, subtrahend);

		BigNumberMatrix result = new BigNumberMatrix(minuend.getRows(), minuend.getColumns(), minuend.getLocale());

		minuend.forEachElement((row, col, a) -> {
			BigNumber b = subtrahend.get(row, col);
			result.set(row, col, a.subtract(b));
		});

		return result;
	}

	/**
	 * Computes the matrix product of two matrices.
	 * <p>
	 * Given a matrix <em>A</em> of dimensions (m × p) and a matrix <em>B</em> of dimensions (p × n),
	 * this method returns the product matrix <em>C</em> of dimensions (m × n), where each element
	 * is calculated as the dot product of the <em>i</em>-th row of <em>A</em> and the <em>j</em>-th column of
	 * <em>B</em>:
	 * </p>
	 * <pre>
	 *     C<sub>ij</sub> = Σ<sub>k=1 to p</sub> A<sub>ik</sub> × B<sub>kj</sub>
	 * </pre>
	 * <p>
	 * Matrix multiplication is only defined if the number of columns in the first matrix equals
	 * the number of rows in the second matrix.
	 * </p>
	 *
	 * @param multiplier
	 * 	the left matrix <em>A</em> with dimensions (m × p)
	 * @param multiplicand
	 * 	the right matrix <em>B</em> with dimensions (p × n)
	 *
	 * @return the resulting matrix <em>C</em> of dimensions (m × n)
	 *
	 * @throws IllegalArgumentException
	 * 	if the number of columns in {@code multiplier} is not equal to the number of rows in {@code multiplicand}
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 */
	public static BigNumberMatrix multiply(@NonNull final BigNumberMatrix multiplier, @NonNull final BigNumberMatrix multiplicand) {
		if (!multiplier.getColumns().isEqualTo(multiplicand.getRows())) {
			throw new IllegalArgumentException("Number of columns of multiplier must equal number of rows of multiplicand.");
		}

		BigNumberMatrix result = new BigNumberMatrix(multiplier.getRows(), multiplicand.getColumns(), multiplier.getLocale());

		result.forEachElement((i, j, value) -> {
			BigNumber sum = BigNumbers.ZERO;
			for (BigNumber k = BigNumbers.ZERO; k.isLessThan(multiplier.getColumns()); k = k.add(BigNumbers.ONE)) {
				BigNumber a = multiplier.get(i, k);
				BigNumber b = multiplicand.get(k, j);
				sum = sum.add(a.multiply(b));
			}
			result.set(i, j, sum);
		});

		return result;
	}

	/**
	 * Computes the element-wise division of one matrix by another.
	 * <p>
	 * Given two matrices <em>A</em> (dividend) and <em>B</em> (divisor) of the same dimensions (m × n),
	 * this method returns the element-wise quotient matrix <em>C</em> where each element
	 * <em>C<sub>ij</sub> = A<sub>ij</sub> / B<sub>ij</sub></em>.
	 * This operation is not standard matrix division but rather an element-wise division.
	 * Use with caution as division by zero elements in {@code divisor} will cause an error.
	 * </p>
	 *
	 * @param dividend
	 * 	the numerator matrix <em>A</em>
	 * @param divisor
	 * 	the denominator matrix <em>B</em>, must be of the same dimensions as {@code dividend}
	 *
	 * @return a new matrix <em>C</em> representing the element-wise division <em>A / B</em>
	 *
	 * @throws IllegalArgumentException
	 * 	if the dimensions of the two matrices differ
	 * @throws ArithmeticException
	 * 	if division by zero occurs in any element of {@code divisor}
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 */
	public static BigNumberMatrix divide(@NonNull final BigNumberMatrix dividend, @NonNull final BigNumberMatrix divisor) {
		checkParamsForSameMatrixSize(dividend, divisor);

		BigNumberMatrix result = new BigNumberMatrix(dividend.getRows(), dividend.getColumns(), dividend.getLocale());

		dividend.forEachElement((row, col, a) -> {
			BigNumber b = divisor.get(row, col);
			result.set(row, col, a.divide(b));
		});

		return result;
	}

	/**
	 * Multiplies each element of the matrix by a scalar value.
	 *
	 * @param matrix
	 * 	the matrix to be scaled
	 * @param scalar
	 * 	the scalar value
	 *
	 * @return a new matrix with each element multiplied by the scalar
	 */
	public static BigNumberMatrix scalarMultiply(@NonNull final BigNumberMatrix matrix, @NonNull final BigNumber scalar) {
		BigNumberMatrix result = new BigNumberMatrix(matrix.getRows(), matrix.getColumns(), matrix.getLocale());

		matrix.forEachElement((row, col, value) -> result.set(row, col, value.multiply(scalar)));

		return result;
	}


	/**
	 * Computes the transpose of a matrix.
	 * <p>
	 * The transpose of an m × n matrix <em>A</em> is the n × m matrix <em>A<sup>T</sup></em> formed by
	 * swapping the rows and columns of <em>A</em>. Formally, each element of <em>A<sup>T</sup></em>
	 * is given by:
	 * </p>
	 * <pre>
	 *     A<sup>T</sup><sub>ij</sub> = A<sub>ji</sub>
	 * </pre>
	 * <p>
	 * Transposition reflects the matrix across its main diagonal.
	 * </p>
	 *
	 * @param matrix
	 * 	the matrix to transpose
	 *
	 * @return the transposed matrix
	 *
	 * @throws NullPointerException
	 * 	if {@code matrix} is {@code null}
	 */
	public static BigNumberMatrix transpose(@NonNull final BigNumberMatrix matrix) {
		BigNumberMatrix result = new BigNumberMatrix(matrix.getColumns(), matrix.getRows(), matrix.getLocale());

		matrix.forEachElement((row, col, value) -> result.set(col, row, value));

		return result;
	}

	/**
	 * Checks if two matrices are valid for addition or subtraction.
	 * Ensures both matrices have the same dimensions and that their dimensions are greater than zero.
	 *
	 * @param augend
	 * 	the first matrix
	 * @param addend
	 * 	the second matrix
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrices have different dimensions or non-positive size
	 */
	private static void checkParamsForSameMatrixSize(BigNumberMatrix augend, BigNumberMatrix addend) {
		if (!augend.getRows().isEqualTo(addend.getRows()) || !augend.getColumns().isEqualTo(addend.getColumns())) {
			throw new IllegalArgumentException("The rows and columns of both matrices must be equal.");
		}

		if (!augend.getRows().isGreaterThan(BigNumbers.ZERO) || !augend.getColumns().isGreaterThan(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("The rows and columns of both matrices must be greater than zero");
		}
	}

}

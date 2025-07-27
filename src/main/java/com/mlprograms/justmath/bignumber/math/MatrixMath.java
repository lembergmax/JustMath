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
	 * Computes the determinant of a square matrix.
	 * <p>
	 * For 1×1 and 2×2 matrices, this method uses a direct formula.
	 * For larger matrices, it recursively computes the determinant via Laplace expansion
	 * along the first row.
	 * </p>
	 *
	 * @param matrix
	 * 	the square matrix whose determinant is to be computed
	 *
	 * @return the determinant as a {@link BigNumber}
	 *
	 * @throws NullPointerException
	 * 	if {@code matrix} is {@code null}
	 */
	public static BigNumber determinant(@NonNull final BigNumberMatrix matrix) {
		BigNumber n = matrix.getRows();

		if (n.isEqualTo(BigNumbers.ONE)) {
			return matrix.get(BigNumbers.ZERO, BigNumbers.ZERO);
		}

		if (n.isEqualTo(BigNumbers.TWO)) {
			BigNumber a = matrix.get(new BigNumber("0"), new BigNumber("0"));
			BigNumber b = matrix.get(new BigNumber("0"), new BigNumber("1"));
			BigNumber c = matrix.get(new BigNumber("1"), new BigNumber("0"));
			BigNumber d = matrix.get(new BigNumber("1"), new BigNumber("1"));

			return a.multiply(d).subtract(b.multiply(c));
		}

		BigNumber det = BigNumbers.ZERO;

		for (BigNumber col = BigNumbers.ZERO; col.isLessThan(n); col = col.add(BigNumbers.ONE)) {
			BigNumber sign = (col.modulo(BigNumbers.TWO).isEqualTo(BigNumbers.ZERO)) ? BigNumbers.ONE : BigNumbers.NEGATIVE_ONE;
			BigNumber element = matrix.get(new BigNumber("0"), new BigNumber(String.valueOf(col)));
			BigNumberMatrix minor = minor(matrix, BigNumbers.ZERO, col);

			det = det.add(sign.multiply(element).multiply(determinant(minor)));
		}

		return det;
	}

	/**
	 * Computes the inverse of a square matrix.
	 * <p>
	 * The inverse of a matrix <em>A</em> is the matrix <em>A<sup>-1</sup></em> such that
	 * <em>A × A<sup>-1</sup> = I</em>, where <em>I</em> is the identity matrix.
	 * This method calculates the inverse using the adjugate and determinant:
	 * <em>A<sup>-1</sup> = adj(A) / det(A)</em>.
	 * </p>
	 *
	 * @param matrix
	 * 	the square matrix to invert
	 *
	 * @return the inverse of the matrix
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrix is not invertible (determinant is zero)
	 * @throws NullPointerException
	 * 	if {@code matrix} is {@code null}
	 */
	public static BigNumberMatrix inverse(@NonNull final BigNumberMatrix matrix) {
		BigNumber determinant = determinant(matrix);

		if (determinant.isEqualTo(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("Matrix is not invertible (determinant is zero).");
		}

		return scalarMultiply(adjugate(matrix), BigNumbers.ONE.divide(determinant));
	}

	/**
	 * Raises a square matrix to a non-negative integer power.
	 * <p>
	 * This method computes <em>base<sup>exponent</sup></em> using exponentiation by squaring,
	 * which is efficient for large exponents. The exponent must be a non-negative integer.
	 * </p>
	 *
	 * @param base
	 * 	the square matrix to be exponentiated
	 * @param exponent
	 * 	the non-negative integer exponent
	 *
	 * @return the matrix raised to the given power
	 *
	 * @throws IllegalArgumentException
	 * 	if the exponent is negative or not an integer
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 */
	public static BigNumberMatrix power(@NonNull final BigNumberMatrix base, @NonNull final BigNumber exponent) {
		if (!exponent.isInteger() || exponent.isNegative()) {
			throw new IllegalArgumentException("Matrix exponent must be a non-negative integer.");
		}

		BigNumberMatrix result = identity(base.getRows(), base.getLocale());
		BigNumberMatrix temp = base.clone();
		BigNumber exp = exponent;

		while (exp.isGreaterThan(BigNumbers.ZERO)) {
			if (exp.modulo(BigNumbers.TWO).isEqualTo(BigNumbers.ONE)) {
				result = multiply(result, temp);
			}

			temp = multiply(temp, temp);
			exp = exp.divide(BigNumbers.TWO).floor();
		}

		return result;
	}

	/**
	 * Returns the minor of the matrix by removing the specified row and column.
	 *
	 * @param matrix
	 * 	the original matrix
	 * @param rowToRemove
	 * 	the row index to remove
	 * @param colToRemove
	 * 	the column index to remove
	 *
	 * @return the resulting minor matrix
	 */
	public static BigNumberMatrix minor(@NonNull final BigNumberMatrix matrix, @NonNull final BigNumber rowToRemove, @NonNull final BigNumber colToRemove) {
		BigNumber size = matrix.getRows();

		BigNumberMatrix result = new BigNumberMatrix(size.subtract(BigNumbers.ONE), size.subtract(BigNumbers.ONE), matrix.getLocale());

		BigNumber newRow = BigNumbers.ZERO;
		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(size); row = row.add(BigNumbers.ONE)) {
			if (row.isEqualTo(rowToRemove)) {
				continue;
			}

			BigNumber newCol = BigNumbers.ZERO;
			for (BigNumber col = BigNumbers.ZERO; col.isLessThan(size); col = col.add(BigNumbers.ONE)) {
				if (col.isEqualTo(colToRemove)) {
					continue;
				}

				BigNumber value = matrix.get(new BigNumber(row), new BigNumber(col));
				result.set(new BigNumber(newRow), new BigNumber(newCol), value);
				newCol = newCol.add(BigNumbers.ONE);
			}
			newRow = newRow.add(BigNumbers.ONE);
		}

		return result;
	}

	/**
	 * Creates an identity matrix of the given size and locale.
	 *
	 * @param size
	 * 	the size (number of rows and columns) of the identity matrix
	 * @param locale
	 * 	the locale for number formatting
	 *
	 * @return an identity matrix of dimension size × size
	 */
	public static BigNumberMatrix identity(@NonNull BigNumber size, @NonNull Locale locale) {
		int n = size.intValue();
		BigNumberMatrix result = new BigNumberMatrix(size, size, locale);

		for (int i = 0; i < n; i++) {
			result.set(new BigNumber(String.valueOf(i)), new BigNumber(String.valueOf(i)), BigNumbers.ONE);
		}

		return result;
	}

	/**
	 * Computes the adjugate (adjoint) of a square matrix.
	 * The adjugate is the transpose of the cofactor matrix.
	 *
	 * @param matrix
	 * 	the input square matrix
	 *
	 * @return the adjugate matrix
	 */
	public static BigNumberMatrix adjugate(@NonNull BigNumberMatrix matrix) {
		BigNumber n = matrix.getRows();
		BigNumberMatrix cofactorMatrix = new BigNumberMatrix(n, n, matrix.getLocale());

		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(n); row = row.add(BigNumbers.ONE)) {
			for (BigNumber col = BigNumbers.ZERO; col.isLessThan(n); col = col.add(BigNumbers.ONE)) {
				BigNumber sign = (row.add(col).modulo(BigNumbers.TWO).isEqualTo(BigNumbers.ZERO)) ? BigNumbers.ONE : BigNumbers.NEGATIVE_ONE;
				BigNumber minorDet = MatrixMath.determinant(minor(matrix, row, col));

				cofactorMatrix.set(new BigNumber(String.valueOf(row)), new BigNumber(String.valueOf(col)), sign.multiply(minorDet));
			}
		}

		return MatrixMath.transpose(cofactorMatrix);
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

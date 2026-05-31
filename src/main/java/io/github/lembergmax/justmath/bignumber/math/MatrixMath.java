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

package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumberMatrix;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.bignumber.matrix.MatrixMessages;

import java.math.MathContext;
import java.util.Locale;
import java.util.Map;

import lombok.NonNull;

public final class MatrixMath {

	private MatrixMath() {
		// Utility class: never instantiated.
	}


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

		final BigNumberMatrix result = new BigNumberMatrix(augend.getRows(), augend.getColumns(), locale);
		augend.forEachElement((row, col, valueA) -> result.set(row, col, valueA.add(addend.get(row, col))));
		// The previous code wrapped {@code result} in a defensive copy via the {@link BigNumberMatrix}
		// copy constructor — that copy is now redundant because {@code result} is a freshly allocated
		// matrix owned exclusively by this method, and the copy constructor itself already performs
		// a deep copy, so the wrap doubled up the work.
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

		final BigNumberMatrix result = new BigNumberMatrix(minuend.getRows(), minuend.getColumns(), minuend.getLocale());
		minuend.forEachElement((row, col, minuendValue) -> result.set(row, col, minuendValue.subtract(subtrahend.get(row, col))));
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
			throw new IllegalArgumentException(MatrixMessages.get(multiplier.getLocale(),
					"matrix.error.multiplyDimMismatch",
					Map.of(
							"leftRows", multiplier.getRows().toString(),
							"leftCols", multiplier.getColumns().toString(),
							"rightRows", multiplicand.getRows().toString(),
							"rightCols", multiplicand.getColumns().toString())));
		}

		// Dimensions are bounded by {@code Integer.MAX_VALUE} (see {@code BigNumberMatrix#validateDimensions}),
		// so the inner dot-product loop can run with primitive {@code int} counters instead of
		// allocating a fresh {@link BigNumber} per increment via {@code k.add(BigNumbers.ONE)}.
		// For an n×n×n multiplication this saves roughly n^3 throw-away BigNumber allocations.
		final int innerCount = multiplier.getColumns().intValue();
		final BigNumberMatrix result = new BigNumberMatrix(multiplier.getRows(), multiplicand.getColumns(), multiplier.getLocale());
		result.forEachElement((rowIndex, columnIndex, ignoredZero) -> {
			BigNumber dotProduct = BigNumbers.ZERO;
			for (int innerIndex = 0; innerIndex < innerCount; innerIndex++) {
				final BigNumber innerIndexAsBigNumber = BigNumber.valueOf(innerIndex);
				final BigNumber leftValue = multiplier.get(rowIndex, innerIndexAsBigNumber);
				final BigNumber rightValue = multiplicand.get(innerIndexAsBigNumber, columnIndex);
				dotProduct = dotProduct.add(leftValue.multiply(rightValue));
			}
			result.set(rowIndex, columnIndex, dotProduct);
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
		return divide(dividend, divisor, BigNumbers.DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Element-wise division of one matrix by another using the supplied {@link MathContext} for the
	 * per-element quotients, so callers can control precision and rounding instead of relying on the
	 * dividend's instance default.
	 *
	 * @param dividend    the numerator matrix
	 * @param divisor     the denominator matrix, of the same dimensions as {@code dividend}
	 * @param mathContext the precision and rounding for each element-wise quotient
	 * @return a new matrix holding the element-wise quotients
	 * @throws IllegalArgumentException if the dimensions differ
	 * @throws ArithmeticException      if any divisor element is zero
	 * @throws NullPointerException     if any argument is {@code null}
	 */
	public static BigNumberMatrix divide(@NonNull final BigNumberMatrix dividend, @NonNull final BigNumberMatrix divisor, @NonNull final MathContext mathContext) {
		checkParamsForSameMatrixSize(dividend, divisor);

		final BigNumberMatrix result = new BigNumberMatrix(dividend.getRows(), dividend.getColumns(), dividend.getLocale());
		dividend.forEachElement((row, col, dividendValue) -> result.set(row, col, dividendValue.divide(divisor.get(row, col), mathContext)));
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
		final BigNumberMatrix result = new BigNumberMatrix(matrix.getRows(), matrix.getColumns(), matrix.getLocale());
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
		final BigNumberMatrix result = new BigNumberMatrix(matrix.getColumns(), matrix.getRows(), matrix.getLocale());
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
		return determinant(matrix, BigNumbers.DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Computes the determinant of a square matrix using the supplied {@link MathContext} for the
	 * fraction-free elimination divisions.
	 * <p>
	 * The 1×1, 2×2 and 3×3 cases use exact closed forms and ignore {@code mathContext}. For
	 * {@code n ≥ 4} the determinant is computed via {@link #determinantViaBareiss}, whose divisions
	 * are exact for integer matrices, so an integer matrix yields its exact integer determinant.
	 *
	 * @param matrix      the square matrix whose determinant is to be computed
	 * @param mathContext the precision/rounding for the elimination divisions (n ≥ 4)
	 * @return the determinant as a {@link BigNumber}
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static BigNumber determinant(@NonNull final BigNumberMatrix matrix, @NonNull final MathContext mathContext) {
		final BigNumber sizeAsBigNumber = matrix.getRows();
		if (sizeAsBigNumber.isEqualTo(BigNumbers.ZERO)) {
			// Convention: the determinant of the empty 0×0 matrix is the multiplicative identity 1.
			// This makes the recursive cofactor expansion for 1×1 inverses produce the correct result.
			// Fresh instance, never the shared constant: the caller may mutate the result.
			return new BigNumber("1", matrix.getLocale());
		}

		final int size = sizeAsBigNumber.intValue();
		if (size == 1) {
			return matrix.get(BigNumbers.ZERO, BigNumbers.ZERO);
		}
		if (size == 2) {
			return determinantTwoByTwo(matrix);
		}
		if (size == 3) {
			return determinantThreeByThree(matrix);
		}
		return determinantViaBareiss(matrix, size, mathContext);
	}

	/**
	 * Direct closed-form determinant for a 2×2 matrix: {@code a*d - b*c}.
	 */
	private static BigNumber determinantTwoByTwo(final BigNumberMatrix matrix) {
		final BigNumber a = matrix.get(BigNumbers.ZERO, BigNumbers.ZERO);
		final BigNumber b = matrix.get(BigNumbers.ZERO, BigNumbers.ONE);
		final BigNumber c = matrix.get(BigNumbers.ONE, BigNumbers.ZERO);
		final BigNumber d = matrix.get(BigNumbers.ONE, BigNumbers.ONE);
		return a.multiply(d).subtract(b.multiply(c));
	}

	/**
	 * Direct rule-of-Sarrus determinant for a 3×3 matrix. Kept as a fast path because LU
	 * decomposition adds noticeable overhead for matrices this small.
	 */
	private static BigNumber determinantThreeByThree(final BigNumberMatrix matrix) {
		final BigNumber two = BigNumbers.TWO;
		final BigNumber a = matrix.get(BigNumbers.ZERO, BigNumbers.ZERO);
		final BigNumber b = matrix.get(BigNumbers.ZERO, BigNumbers.ONE);
		final BigNumber c = matrix.get(BigNumbers.ZERO, two);
		final BigNumber d = matrix.get(BigNumbers.ONE, BigNumbers.ZERO);
		final BigNumber e = matrix.get(BigNumbers.ONE, BigNumbers.ONE);
		final BigNumber f = matrix.get(BigNumbers.ONE, two);
		final BigNumber g = matrix.get(two, BigNumbers.ZERO);
		final BigNumber h = matrix.get(two, BigNumbers.ONE);
		final BigNumber i = matrix.get(two, two);
		return a.multiply(e.multiply(i).subtract(f.multiply(h)))
				.subtract(b.multiply(d.multiply(i).subtract(f.multiply(g))))
				.add(c.multiply(d.multiply(h).subtract(e.multiply(g))));
	}

	/**
	 * Computes the determinant via the Bareiss fraction-free elimination algorithm in
	 * {@code O(n^3)} arithmetic operations.
	 *
	 * <p>Bareiss is used instead of plain LU decomposition because every division it performs is
	 * <em>exact</em> for an integer matrix (the Bareiss identity guarantees the running quotient
	 * divides without remainder). An integer matrix therefore yields its <em>exact</em> integer
	 * determinant — e.g. an integer 4×4 matrix whose determinant is 3 returns exactly {@code 3},
	 * not {@code 2.999…} as the rounding LU division could. For non-integer matrices the divisions
	 * fall back to {@code mathContext} precision.</p>
	 *
	 * <p>The matrix is copied into a primitive 2D {@link BigNumber} array so the inner loops use
	 * direct array access. Because the arithmetic is exact, no magnitude-based partial pivoting is
	 * needed; a row is only swapped to step over a zero pivot, and each swap flips the sign. A
	 * column with no non-zero entry at or below the diagonal means the matrix is singular and the
	 * determinant is exactly zero.</p>
	 *
	 * @param matrix      the square source matrix; must not be {@code null}
	 * @param size        the matrix dimension, guaranteed to fit in {@code int}
	 * @param mathContext precision/rounding for the (exact-for-integers) elimination divisions
	 * @return the determinant of {@code matrix}
	 */
	private static BigNumber determinantViaBareiss(final BigNumberMatrix matrix, final int size, final MathContext mathContext) {
		final BigNumber[][] workingCopy = toPrimitiveArray(matrix, size);
		boolean rowSwapNegatesSign = false;
		BigNumber previousPivot = BigNumbers.ONE;

		for (int pivotColumn = 0; pivotColumn < size - 1; pivotColumn++) {
			if (workingCopy[pivotColumn][pivotColumn].isEqualTo(BigNumbers.ZERO)) {
				final int swapRow = findNonZeroRowInColumn(workingCopy, pivotColumn, size);
				if (swapRow == -1) {
					// Fresh instance, never the shared constant: the caller may mutate the determinant.
					return new BigNumber("0", matrix.getLocale());
				}
				swapRows(workingCopy, pivotColumn, swapRow);
				rowSwapNegatesSign = !rowSwapNegatesSign;
			}

			final BigNumber pivotValue = workingCopy[pivotColumn][pivotColumn];
			for (int eliminationRow = pivotColumn + 1; eliminationRow < size; eliminationRow++) {
				for (int columnIndex = pivotColumn + 1; columnIndex < size; columnIndex++) {
					final BigNumber numerator = workingCopy[eliminationRow][columnIndex].multiply(pivotValue)
							.subtract(workingCopy[eliminationRow][pivotColumn].multiply(workingCopy[pivotColumn][columnIndex]));
					// Exact for integer matrices (Bareiss identity); rounds to mathContext otherwise.
					workingCopy[eliminationRow][columnIndex] = numerator.divide(previousPivot, mathContext);
				}
				workingCopy[eliminationRow][pivotColumn] = BigNumbers.ZERO;
			}
			previousPivot = pivotValue;
		}

		final BigNumber determinant = workingCopy[size - 1][size - 1];
		return rowSwapNegatesSign ? determinant.negate() : determinant;
	}

	/**
	 * Copies the matrix into a primitive 2D {@link BigNumber} array for the LU loops to mutate.
	 */
	private static BigNumber[][] toPrimitiveArray(final BigNumberMatrix matrix, final int size) {
		final BigNumber[][] result = new BigNumber[size][size];
		for (int rowIndex = 0; rowIndex < size; rowIndex++) {
			final BigNumber rowAsBigNumber = BigNumber.valueOf(rowIndex);
			for (int columnIndex = 0; columnIndex < size; columnIndex++) {
				result[rowIndex][columnIndex] = matrix.get(rowAsBigNumber, BigNumber.valueOf(columnIndex));
			}
		}
		return result;
	}

	/**
	 * Returns the index of the first row strictly below the diagonal whose entry in
	 * {@code pivotColumn} is non-zero, or {@code -1} if none exists (the column is all zeros at and
	 * below the diagonal, so the matrix is singular). Exact arithmetic needs no magnitude-based
	 * partial pivoting; a row swap is only required to step over a zero pivot.
	 */
	private static int findNonZeroRowInColumn(final BigNumber[][] matrix, final int pivotColumn, final int size) {
		for (int candidateRow = pivotColumn + 1; candidateRow < size; candidateRow++) {
			if (!matrix[candidateRow][pivotColumn].isEqualTo(BigNumbers.ZERO)) {
				return candidateRow;
			}
		}
		return -1;
	}

	private static void swapRows(final BigNumber[][] matrix, final int firstRow, final int secondRow) {
		final BigNumber[] temporaryReference = matrix[firstRow];
		matrix[firstRow] = matrix[secondRow];
		matrix[secondRow] = temporaryReference;
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
		return inverse(matrix, BigNumbers.DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Computes the inverse using the supplied {@link MathContext} for the determinant elimination and
	 * the {@code 1/det} scaling, so callers can control precision instead of inheriting a constant's
	 * default context.
	 *
	 * @param matrix      the square matrix to invert
	 * @param mathContext the precision/rounding for the determinant and the reciprocal scale factor
	 * @return the inverse of the matrix
	 * @throws IllegalArgumentException if the matrix is not invertible (determinant is zero)
	 * @throws NullPointerException     if any argument is {@code null}
	 */
	public static BigNumberMatrix inverse(@NonNull final BigNumberMatrix matrix, @NonNull final MathContext mathContext) {
		final BigNumber determinant = determinant(matrix, mathContext);

		if (determinant.isEqualTo(BigNumbers.ZERO)) {
			throw new IllegalArgumentException(
					MatrixMessages.get(matrix.getLocale(), "matrix.error.singular"));
		}

		return scalarMultiply(adjugate(matrix, mathContext), BigNumbers.ONE.divide(determinant, mathContext));
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
			throw new IllegalArgumentException(
					MatrixMessages.get(base.getLocale(), "matrix.error.invalidExponent"));
		}

		BigNumberMatrix accumulator = identity(base.getRows(), base.getLocale());
		BigNumberMatrix squaringBase = base.clone();
		BigNumber remainingExponent = exponent;

		// Exponentiation by squaring on an arbitrary-precision exponent: while the exponent is
		// non-zero, fold the current squared base into the accumulator on odd bits, then square
		// the base and shift the exponent right by one (modelled as integer division by two).
		while (remainingExponent.isGreaterThan(BigNumbers.ZERO)) {
			if (remainingExponent.modulo(BigNumbers.TWO).isEqualTo(BigNumbers.ONE)) {
				accumulator = multiply(accumulator, squaringBase);
			}
			squaringBase = multiply(squaringBase, squaringBase);
			remainingExponent = remainingExponent.divide(BigNumbers.TWO).floor();
		}
		return accumulator;
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
		final int size = matrix.getRows().intValue();
		final int removedRow = rowToRemove.intValue();
		final int removedColumn = colToRemove.intValue();

		final BigNumber minorSizeAsBigNumber = BigNumber.valueOf(size - 1);
		final BigNumberMatrix result = new BigNumberMatrix(minorSizeAsBigNumber, minorSizeAsBigNumber, matrix.getLocale());

		int targetRow = 0;
		for (int sourceRow = 0; sourceRow < size; sourceRow++) {
			if (sourceRow == removedRow) {
				continue;
			}
			final BigNumber sourceRowAsBigNumber = BigNumber.valueOf(sourceRow);
			final BigNumber targetRowAsBigNumber = BigNumber.valueOf(targetRow);

			int targetColumn = 0;
			for (int sourceColumn = 0; sourceColumn < size; sourceColumn++) {
				if (sourceColumn == removedColumn) {
					continue;
				}
				result.set(targetRowAsBigNumber, BigNumber.valueOf(targetColumn),
						matrix.get(sourceRowAsBigNumber, BigNumber.valueOf(sourceColumn)));
				targetColumn++;
			}
			targetRow++;
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
	public static BigNumberMatrix identity(@NonNull final BigNumber size, @NonNull final Locale locale) {
		final int sizeAsInt = size.intValue();
		final BigNumberMatrix result = new BigNumberMatrix(size, size, locale);
		for (int diagonalIndex = 0; diagonalIndex < sizeAsInt; diagonalIndex++) {
			final BigNumber diagonalIndexAsBigNumber = BigNumber.valueOf(diagonalIndex);
			result.set(diagonalIndexAsBigNumber, diagonalIndexAsBigNumber, BigNumbers.ONE);
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
	public static BigNumberMatrix adjugate(@NonNull final BigNumberMatrix matrix) {
		return adjugate(matrix, BigNumbers.DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Computes the adjugate using the supplied {@link MathContext} for the cofactor minor
	 * determinants (relevant only when the minors are 4×4 or larger).
	 *
	 * @param matrix      the input square matrix
	 * @param mathContext the precision/rounding forwarded to each minor determinant
	 * @return the adjugate matrix
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static BigNumberMatrix adjugate(@NonNull final BigNumberMatrix matrix, @NonNull final MathContext mathContext) {
		final BigNumber sizeAsBigNumber = matrix.getRows();
		final int size = sizeAsBigNumber.intValue();
		final BigNumberMatrix cofactorMatrix = new BigNumberMatrix(sizeAsBigNumber, sizeAsBigNumber, matrix.getLocale());

		for (int rowIndex = 0; rowIndex < size; rowIndex++) {
			final BigNumber rowIndexAsBigNumber = BigNumber.valueOf(rowIndex);
			for (int columnIndex = 0; columnIndex < size; columnIndex++) {
				final BigNumber columnIndexAsBigNumber = BigNumber.valueOf(columnIndex);
				final BigNumber sign = ((rowIndex + columnIndex) & 1) == 0 ? BigNumbers.ONE : BigNumbers.NEGATIVE_ONE;
				final BigNumber minorDeterminant = determinant(minor(matrix, rowIndexAsBigNumber, columnIndexAsBigNumber), mathContext);
				cofactorMatrix.set(rowIndexAsBigNumber, columnIndexAsBigNumber, sign.multiply(minorDeterminant));
			}
		}
		return transpose(cofactorMatrix);
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
		Locale locale = augend.getLocale();

		if (!augend.getRows().isEqualTo(addend.getRows()) || !augend.getColumns().isEqualTo(addend.getColumns())) {
			throw new IllegalArgumentException(MatrixMessages.get(locale,
					"matrix.error.elementWiseDimMismatch",
					Map.of(
							"leftRows", augend.getRows().toString(),
							"leftCols", augend.getColumns().toString(),
							"rightRows", addend.getRows().toString(),
							"rightCols", addend.getColumns().toString())));
		}

		if (!augend.getRows().isGreaterThan(BigNumbers.ZERO) || !augend.getColumns().isGreaterThan(BigNumbers.ZERO)) {
			throw new IllegalArgumentException(MatrixMessages.get(locale, "matrix.error.zeroDim"));
		}
	}

}

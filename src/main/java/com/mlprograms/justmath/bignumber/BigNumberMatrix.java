package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.math.MatrixMath;
import com.mlprograms.justmath.bignumber.matrix.MatrixElementConsumer;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * Represents a matrix whose elements are arbitrary-precision decimal numbers ({@link BigNumber}),
 * offering high-precision mathematical operations and clean object-oriented access.
 * <p>
 * This class supports creation from dimensions, string representations, or nested lists of strings,
 * and provides standard matrix operations such as addition, subtraction, multiplication,
 * transposition, and scalar operations.
 * </p>
 * <p>
 * All arithmetic operations preserve locale-specific formatting and parsing through {@link BigNumber}.
 * The matrix is internally stored as a 2D {@code List<List<BigNumber>>}.
 * </p>
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>Immutable dimensions</li>
 *   <li>Strict input validation (non-negative integers for size)</li>
 *   <li>Locale-aware BigNumber construction</li>
 *   <li>Matrix values are not nullable</li>
 * </ul>
 */
@Getter
public class BigNumberMatrix implements Cloneable {

	/**
	 * The number of rows in this matrix.
	 * Guaranteed to be a non-negative integer {@link BigNumber}.
	 */
	private final BigNumber rows;

	/**
	 * The number of columns in this matrix.
	 * Guaranteed to be a non-negative integer {@link BigNumber}.
	 */
	private final BigNumber columns;

	/**
	 * The matrix data stored as a two-dimensional list of {@link BigNumber} values.
	 * Each sublist represents a row, and each element in the sublist represents a column entry.
	 * The matrix is always rectangular: each row has exactly {@code columns} elements.
	 */
	private final List<List<BigNumber>> data;

	/**
	 * The {@link Locale} used for parsing and formatting {@link BigNumber} values in this matrix.
	 * Ensures consistency in string-based operations, parsing, and output.
	 */
	private final Locale locale;

	/**
	 * Constructs a matrix with the given number of rows and columns, initializing all values to zero.
	 *
	 * @param rows
	 * 	the number of rows (must be a non-negative integer)
	 * @param columns
	 * 	the number of columns (must be a non-negative integer)
	 * @param locale
	 * 	the locale used to format and parse BigNumber entries
	 *
	 * @throws IllegalArgumentException
	 * 	if dimensions are negative, non-integer, or too large
	 */
	public BigNumberMatrix(@NonNull final BigNumber rows, @NonNull final BigNumber columns, @NonNull final Locale locale) {
		this.rows = rows;
		this.columns = columns;
		this.locale = locale;
		this.data = new ArrayList<>();

		validateDimensions();
		fillWithZeroes();
	}

	/**
	 * Constructs a matrix from a nested list of strings, where each sublist represents a row.
	 * All data are parsed into {@link BigNumber} using the provided locale.
	 *
	 * @param data
	 * 	2D list of {@link BigNumber} data; all rows must have equal length
	 * @param locale
	 * 	the locale used to parse the strings into {@link BigNumber} data
	 *
	 * @throws IllegalArgumentException
	 * 	if any value is invalid or dimensions are inconsistent
	 */
	public BigNumberMatrix(@NonNull List<List<BigNumber>> data, @NonNull Locale locale) {
		this.locale = locale;
		this.data = data;
		this.rows = new BigNumber(String.valueOf(data.size()), locale);
		this.columns = new BigNumber(String.valueOf(data.getFirst().size()), locale);
	}

	/**
	 * Constructs a matrix from a semicolon-separated string representation.
	 * Each row is separated by a semicolon (';'), and columns are separated by commas (',').
	 * <p>
	 * Example: {@code "1, 2; 3, 4"} becomes a 2×2 matrix.
	 *
	 * @param matrixString
	 * 	the string representation of the matrix
	 * @param locale
	 * 	the locale used to parse the entries into {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the input format is invalid or inconsistent
	 */
	public BigNumberMatrix(@NonNull String matrixString, @NonNull Locale locale) {
		this(parseMatrixString(matrixString), locale);
	}

	/**
	 * Copy constructor.
	 * Creates a new BigNumberMatrix by copying the locale, data, rows, and columns from the given matrix.
	 * Note: The data list is shallow-copied; the inner lists and BigNumber elements are not deeply cloned.
	 *
	 * @param bigNumberMatrix
	 * 	the matrix to copy
	 */
	public BigNumberMatrix(@NonNull final BigNumberMatrix bigNumberMatrix) {
		this.locale = bigNumberMatrix.getLocale();
		this.rows = bigNumberMatrix.getRows();
		this.columns = bigNumberMatrix.getColumns();
		this.data = new ArrayList<>();

		deepCopyData(bigNumberMatrix);
	}

	/**
	 * Parses a string-based matrix representation into a nested list of strings.
	 * Rows are separated by semicolons (;) and columns by commas (,).
	 *
	 * @param input
	 * 	the string representation of the matrix (e.g. "1, 2; 3, 4")
	 *
	 * @return a 2D list of strings representing the parsed matrix entries
	 *
	 * @throws IllegalArgumentException
	 * 	if the input is empty, rows have inconsistent column counts,
	 * 	or any matrix entry is empty
	 */
	private static List<List<BigNumber>> parseMatrixString(@NonNull final String input) {
		List<List<BigNumber>> result = new ArrayList<>();

		String[] rows = input.split(";");
		int expectedCols = -1;

		for (String row : rows) {
			String[] cols = row.trim().split(",");

			if (expectedCols == -1) {
				expectedCols = cols.length;
			} else if (cols.length != expectedCols) {
				throw new IllegalArgumentException("All rows must have same column count.");
			}

			List<BigNumber> parsedRow = new ArrayList<>();

			for (String col : cols) {
				String trimmed = col.trim();

				if (trimmed.isEmpty()) {
					throw new IllegalArgumentException("Matrix entry must not be empty.");
				}

				parsedRow.add(new BigNumber(trimmed));
			}

			result.add(parsedRow);
		}

		return result;
	}

	/**
	 * Performs a deep copy of the matrix data from the given {@code bigNumberMatrix}.
	 * Each {@link BigNumber} element is individually copied to ensure immutability.
	 * The resulting {@code data} list contains new row lists and new {@code BigNumber} instances.
	 *
	 * @param bigNumberMatrix
	 * 	the matrix from which to copy the data
	 */
	private void deepCopyData(@NonNull final BigNumberMatrix bigNumberMatrix) {
		for (List<BigNumber> row : bigNumberMatrix.getData()) {
			List<BigNumber> newRow = new ArrayList<>(row.size());

			for (BigNumber value : row) {
				newRow.add(new BigNumber(value));
			}

			data.add(newRow);
		}
	}

	/**
	 * Validates the {@code rows} and {@code columns} fields to ensure they represent valid dimensions.
	 * Both must be non-negative integers, and neither may exceed {@link Integer#MAX_VALUE}.
	 *
	 * @throws IllegalArgumentException
	 * 	if the dimension values are invalid or too large
	 */
	private void validateDimensions() {
		if (!rows.isInteger() || !columns.isInteger() || rows.isNegative() || columns.isNegative()) {
			throw new IllegalArgumentException("Matrix dimensions must be non-negative integers.");
		}

		BigNumber max = new BigNumber(String.valueOf(Integer.MAX_VALUE));

		if (rows.isGreaterThan(max) || columns.isGreaterThan(max)) {
			throw new IllegalArgumentException("Matrix size must be smaller than Integer.MAX_VALUE.");
		}
	}

	/**
	 * Initializes the internal matrix structure with all entries set to {@code BigNumber.ZERO}.
	 * The resulting matrix will have exactly {@code rows × columns} zero entries.
	 */
	private void fillWithZeroes() {
		for (int i = 0; i < rows.intValue(); i++) {
			List<BigNumber> row = new ArrayList<>();

			for (int j = 0; j < columns.intValue(); j++) {
				row.add(new BigNumber("0", locale));
			}

			data.add(row);
		}
	}

	/**
	 * Parses the nested string list into {@link BigNumber} values using the configured locale.
	 * All rows must have the same number of columns as defined in {@code this.columns}.
	 *
	 * @param values
	 * 	the 2D list of string entries to parse and fill into the matrix
	 *
	 * @throws IllegalArgumentException
	 * 	if any row has a different number of columns
	 */
	private void fillFromStringList(@NonNull final List<List<String>> values) {
		for (List<String> row : values) {
			if (row.size() != columns.intValue()) {
				throw new IllegalArgumentException("Inconsistent column count in row.");
			}

			List<BigNumber> parsedRow = new ArrayList<>();

			for (String val : row) {
				parsedRow.add(new BigNumber(val, locale));
			}

			data.add(parsedRow);
		}
	}

	/**
	 * Sets the matrix entry at the given (row, column) index.
	 *
	 * @param row
	 * 	the row index (0-based, must be within bounds)
	 * @param col
	 * 	the column index (0-based, must be within bounds)
	 * @param value
	 * 	the value to set
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if either index is out of range
	 */
	public void set(@NonNull final BigNumber row, @NonNull final BigNumber col, @NonNull final BigNumber value) {
		validateIndex(row, rows, "row");
		validateIndex(col, columns, "column");

		data.get(row.intValue()).set(col.intValue(), value);
	}

	/**
	 * Retrieves the matrix entry at the given (row, column) index.
	 *
	 * @param row
	 * 	the row index (0-based, must be within bounds)
	 * @param col
	 * 	the column index (0-based, must be within bounds)
	 *
	 * @return the {@link BigNumber} value at the specified position
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if either index is out of range
	 */
	public BigNumber get(@NonNull final BigNumber row, @NonNull final BigNumber col) {
		validateIndex(row, rows, "row");
		validateIndex(col, columns, "column");

		return data.get(row.intValue()).get(col.intValue());
	}

	/**
	 * Validates whether the given index lies within the valid bounds {@code [0, max)}.
	 * Also ensures the index is a non-negative integer.
	 *
	 * @param index
	 * 	the row or column index to check
	 * @param max
	 * 	the exclusive upper bound (typically {@code rows} or {@code columns})
	 * @param type
	 * 	a string indicating the type ("row" or "column") for error messaging
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if the index is out of bounds or invalid
	 */
	private void validateIndex(@NonNull final BigNumber index, @NonNull final BigNumber max, @NonNull final String type) {
		if (!index.isInteger() || index.isNegative() || index.isGreaterThanOrEqualTo(max)) {
			throw new IndexOutOfBoundsException(type + " index out of bounds: " + index);
		}
	}

	/**
	 * Returns a new matrix that is the result of element-wise addition with another matrix.
	 *
	 * @param other
	 * 	the matrix to add (must be the same size)
	 *
	 * @return a new {@link BigNumberMatrix} representing the sum
	 *
	 * @throws IllegalArgumentException
	 * 	if dimensions do not match
	 */
	public BigNumberMatrix add(@NonNull final BigNumberMatrix other) {
		return MatrixMath.add(this, other, locale);
	}

	/**
	 * Returns a new matrix that is the result of element-wise subtraction from this matrix.
	 *
	 * @param other
	 * 	the matrix to subtract (must be the same size)
	 *
	 * @return a new {@link BigNumberMatrix} representing the difference
	 *
	 * @throws IllegalArgumentException
	 * 	if dimensions do not match
	 */
	public BigNumberMatrix subtract(@NonNull final BigNumberMatrix other) {
		return MatrixMath.subtract(this, other);
	}

	/**
	 * Returns the matrix product of this matrix and another.
	 *
	 * @param other
	 * 	the right-hand matrix of the multiplication (must have compatible dimensions)
	 *
	 * @return the result of matrix multiplication
	 *
	 * @throws IllegalArgumentException
	 * 	if dimensions are incompatible
	 */
	public BigNumberMatrix multiply(@NonNull final BigNumberMatrix other) {
		return MatrixMath.multiply(this, other);
	}

	/**
	 * Performs element-wise division of this matrix by another matrix.
	 *
	 * @param other
	 * 	the divisor matrix (must be the same size)
	 *
	 * @return the result of element-wise division
	 *
	 * @throws IllegalArgumentException
	 * 	if dimensions do not match or division by zero occurs
	 */
	public BigNumberMatrix divide(@NonNull final BigNumberMatrix other) {
		return MatrixMath.divide(this, other);
	}

	/**
	 * Returns the transpose of this matrix (rows become columns and vice versa).
	 *
	 * @return the transposed matrix
	 */
	public BigNumberMatrix transpose() {
		return MatrixMath.transpose(this);
	}

	/**
	 * Returns the result of multiplying every matrix entry by a scalar value.
	 *
	 * @param scalar
	 * 	the scalar {@link BigNumber} to multiply with
	 *
	 * @return the scaled matrix
	 */
	public BigNumberMatrix scalarMultiply(@NonNull final BigNumber scalar) {
		return MatrixMath.scalarMultiply(this, scalar);
	}

	/**
	 * Returns a matrix where each element is the negation of the original element.
	 *
	 * @return the negated matrix
	 */
	public BigNumberMatrix negate() {
		return MatrixMath.scalarMultiply(this, new BigNumber("-1", locale));
	}

	/**
	 * Computes the determinant of this matrix using LU decomposition or a recursive strategy,
	 * depending on the implementation of {@link MatrixMath}.
	 * <p>
	 * The determinant is a scalar value that describes properties such as:
	 * <ul>
	 *   <li>Whether the matrix is invertible (non-zero determinant)</li>
	 *   <li>Volume scaling factor in linear transformations</li>
	 *   <li>The sign and orientation of basis vectors</li>
	 * </ul>
	 *
	 * @return the determinant as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrix is not square
	 */
	public BigNumber determinant() {
		if (!isSquare()) {
			throw new IllegalArgumentException("Determinant is only defined for square matrices.");
		}

		return MatrixMath.determinant(this);
	}

	/**
	 * Computes the inverse of this matrix.
	 * <p>
	 * The inverse matrix {@code A⁻¹} satisfies the condition: {@code A × A⁻¹ = I}, where {@code I}
	 * is the identity matrix. This operation is only defined for square and invertible matrices.
	 *
	 * @return the inverse of this matrix
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrix is not square or not invertible
	 */
	public BigNumberMatrix inverse() {
		if (!isSquare()) {
			throw new IllegalArgumentException("Only square matrices can be inverted.");
		}

		return MatrixMath.inverse(this);
	}

	/**
	 * Computes the matrix raised to the power of a given exponent.
	 * <p>
	 * This operation performs repeated matrix multiplication:
	 * <pre>
	 * A^0 = I (identity matrix)
	 * A^1 = A
	 * A^2 = A × A
	 * A^n = A × A × ... × A (n times)
	 * </pre>
	 * <p>
	 * Only square matrices can be exponentiated. If the exponent is zero,
	 * the result is the identity matrix of the same dimension.
	 *
	 * @param exponent
	 * 	the {@link BigNumber} exponent
	 *
	 * @return a new matrix representing this matrix raised to the given power
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrix is not square or the exponent is negative
	 */
	public BigNumberMatrix power(@NonNull final BigNumber exponent) {
		if (!isSquare()) {
			throw new IllegalArgumentException("Matrix power only defined for square matrices.");
		}

		return MatrixMath.power(this, exponent);
	}

	/**
	 * Computes the trace of the matrix, defined as the sum of the diagonal elements.
	 * <p>
	 * The trace has several mathematical applications:
	 * <ul>
	 *   <li>Used in matrix invariants</li>
	 *   <li>Important in linear algebra and quantum mechanics</li>
	 *   <li>Equal to the sum of eigenvalues of a matrix</li>
	 * </ul>
	 *
	 * @return the trace as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the matrix is not square
	 */
	public BigNumber trace() {
		if (!isSquare()) {
			throw new IllegalArgumentException("Trace only defined for square matrices.");
		}

		BigNumber sum = BigNumbers.ZERO;

		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(rows); i = i.add(BigNumbers.ONE)) {
			sum = sum.add(get(i, i));
		}

		return sum;
	}

	/**
	 * Checks whether the matrix is symmetric.
	 * <p>
	 * A matrix is symmetric if it is equal to its transpose, i.e., {@code A[i][j] == A[j][i]}
	 * for all valid indices {@code i, j}.
	 *
	 * @return {@code true} if the matrix is symmetric; {@code false} otherwise
	 */
	public boolean isSymmetric() {
		if (!isSquare()) {
			return false;
		}

		AtomicBoolean symmetric = new AtomicBoolean(true);
		forEachIndex((i, j) -> {
			if (!get(i, j).isEqualTo(get(j, i))) {
				symmetric.set(false);
			}
		});

		return symmetric.get();
	}

	/**
	 * Computes the sum of all elements in the matrix.
	 * <p>
	 * This is useful for statistical calculations, checksum operations,
	 * or simple aggregate evaluations of matrix content.
	 *
	 * @return the total sum of all matrix entries
	 */
	public BigNumber sumElements() {
		BigNumber[] sum = new BigNumber[] { BigNumbers.ZERO };

		forEachElement((i, j, value) -> sum[ 0 ] = sum[ 0 ].add(value));

		return sum[ 0 ];
	}

	/**
	 * Returns the maximum value among all elements in the matrix.
	 * <p>
	 * This method iterates over all matrix entries and compares their values
	 * using {@link BigNumber#isGreaterThan(BigNumber)} to determine the maximum.
	 *
	 * @return the largest {@link BigNumber} present in the matrix
	 */
	public BigNumber max() {
		BigNumber[] currentMax = new BigNumber[] { get(BigNumbers.ZERO, BigNumbers.ZERO) };

		forEachElement((i, j, value) -> {
			if (value.isGreaterThan(currentMax[ 0 ])) {
				currentMax[ 0 ] = value;
			}
		});

		return currentMax[ 0 ];
	}

	/**
	 * Checks whether the matrix is square (i.e., number of rows equals number of columns).
	 *
	 * @return true if the matrix is square, false otherwise
	 */
	public boolean isSquare() {
		return rows.isEqualTo(columns);
	}

	/**
	 * Checks whether all elements of the matrix are exactly zero.
	 *
	 * @return true if this is a zero matrix, false otherwise
	 */
	public boolean isZeroMatrix() {
		for (List<BigNumber> row : getData()) {
			for (BigNumber element : row) {
				if (!element.isEqualTo(BigNumbers.ZERO)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Checks whether this matrix is an <strong>identity matrix</strong>.
	 * <p>
	 * An <em>identity matrix</em> is a special kind of square matrix in which all the elements
	 * on the <strong>main diagonal</strong> (i.e., positions where the row index equals the column index)
	 * are equal to one (1), and all other elements are equal to zero (0).
	 * It is denoted by the symbol {@code I<sub>n</sub>} for an {@code n × n} matrix.
	 * </p>
	 *
	 * <p>Mathematically, an identity matrix satisfies the condition:</p>
	 * <pre>
	 * I * A = A * I = A
	 * </pre>
	 * for any matrix {@code A} of compatible dimensions, where {@code *} denotes matrix multiplication.
	 *
	 * <p>
	 * This method first verifies that the matrix is square, since identity matrices must have the same
	 * number of rows and columns. It then iterates over all elements and checks:
	 * </p>
	 * <ul>
	 *   <li>All diagonal elements {@code A[i][i]} are equal to one (1)</li>
	 *   <li>All off-diagonal elements {@code A[i][j]} for {@code i ≠ j} are equal to zero (0)</li>
	 * </ul>
	 *
	 * <p>
	 * This method is locale-aware: the values 0 and 1 are parsed using the matrix's configured {@link Locale}.
	 * </p>
	 *
	 * @return {@code true} if the matrix is an identity matrix; {@code false} otherwise
	 *
	 * @see #isSquare()
	 * @see #isZeroMatrix()
	 */
	public boolean isIdentityMatrix() {
		if (!isSquare()) {
			return false;
		}

		BigNumber one = new BigNumber("1", locale);
		BigNumber zero = new BigNumber("0", locale);

		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(rows); i = i.add(BigNumbers.ONE)) {
			for (BigNumber j = BigNumbers.ZERO; j.isLessThan(columns); j = j.add(BigNumbers.ONE)) {
				BigNumber value = get(i, j);
				if (i.isEqualTo(j)) {
					if (!value.isEqualTo(one)) {
						return false;
					}
				} else {
					if (!value.isEqualTo(zero)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Returns a flattened list of all elements in the matrix in row-major order.
	 * <p>
	 * This is useful for serialization, vectorization, or converting a 2D structure
	 * into a linear representation.
	 *
	 * @return a list of all matrix elements, row by row
	 */
	public List<BigNumber> flatten() {
		List<BigNumber> list = new ArrayList<>();
		forEachElement((i, j, value) -> list.add(value));

		return list;
	}

	/**
	 * Compares this matrix with another matrix for structural and numerical equality.
	 * <p>
	 * Two matrices are considered equal if they have the same dimensions and
	 * corresponding elements are numerically equal via {@link BigNumber#isEqualTo(BigNumber)}.
	 *
	 * @param other
	 * 	the matrix to compare against
	 *
	 * @return {@code true} if both matrices are equal; {@code false} otherwise
	 */

	public boolean equalsMatrix(@NonNull BigNumberMatrix other) {
		if (!rows.isEqualTo(other.getRows()) || !columns.isEqualTo(other.getColumns())) {
			return false;
		}

		AtomicBoolean equal = new AtomicBoolean(true);
		forEachIndex((i, j) -> {
			if (!get(i, j).isEqualTo(other.get(i, j))) {
				equal.set(false);
			}
		});

		return equal.get();
	}

	/**
	 * Iterates over every element of the matrix and applies the specified action.
	 * <p>
	 * For each element {@code A[i][j]} in the matrix, this method provides the
	 * corresponding row index {@code i}, column index {@code j}, and the current
	 * value {@code A[i][j]} to the given {@link MatrixElementConsumer}.
	 * This is useful for operations that require access to both the matrix indices
	 * and the element value, such as printing, transformation, conditional modification,
	 * or accumulation.
	 * <p>
	 * Example use cases:
	 * <ul>
	 *   <li>Summing all diagonal elements</li>
	 *   <li>Applying a function to all elements (e.g., squaring or negating)</li>
	 *   <li>Debugging by printing values along with their positions</li>
	 * </ul>
	 *
	 * @param action
	 * 	the operation to perform on each matrix element;
	 * 	receives the row index, column index, and the value at that position
	 *
	 * @throws NullPointerException
	 * 	if {@code action} is {@code null}
	 * @see MatrixElementConsumer
	 */
	public void forEachElement(@NonNull MatrixElementConsumer action) {
		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(rows); row = row.add(BigNumbers.ONE)) {
			for (BigNumber col = BigNumbers.ZERO; col.isLessThan(columns); col = col.add(BigNumbers.ONE)) {
				action.accept(row, col, get(row, col));
			}
		}
	}


	/**
	 * Iterates over every index pair {@code (i, j)} in the matrix,
	 * invoking the specified action for each pair.
	 * <p>
	 * Unlike {@link #forEachElement(MatrixElementConsumer)}, this method does not access or pass
	 * the matrix values themselves. Instead, it focuses purely on iterating through the index space.
	 * This is particularly useful when the value access is not needed or should be performed manually.
	 * <p>
	 * Example use cases:
	 * <ul>
	 *   <li>Generating index-based patterns or masks</li>
	 *   <li>Allocating or initializing external structures of the same shape</li>
	 *   <li>Counting specific index properties (e.g., checking if (i + j) is even)</li>
	 * </ul>
	 *
	 * @param action
	 * 	a {@link BiConsumer} that receives the row and column index of each matrix position
	 *
	 * @throws NullPointerException
	 * 	if {@code action} is {@code null}
	 */
	public void forEachIndex(@NonNull BiConsumer<BigNumber, BigNumber> action) {
		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(rows); row = row.add(BigNumbers.ONE)) {
			for (BigNumber col = BigNumbers.ZERO; col.isLessThan(columns); col = col.add(BigNumbers.ONE)) {
				action.accept(row, col);
			}
		}
	}

	/**
	 * Returns a human-readable string describing the matrix, including dimensions and values.
	 *
	 * @return a string representation of this matrix
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BigNumberMatrix[").append(rows).append("x").append(columns).append("]\n");

		for (List<BigNumber> row : data) {
			sb.append("  ").append(row).append("\n");
		}

		return sb.toString();
	}

	/**
	 * Returns a plain string representation of the matrix data only,
	 * excluding class or dimension information.
	 *
	 * @return a plain-text representation of the matrix values
	 */
	public String toPlainDataString() {
		StringBuilder sb = new StringBuilder("[");

		for (List<BigNumber> row : data) {
			sb.append("\n  ").append(row);
		}

		return sb.append("\n]").toString();
	}

	/**
	 * Creates a deep copy of this matrix.
	 * <p>
	 * Equivalent to the copy constructor {@link #BigNumberMatrix(BigNumberMatrix)}.
	 * All internal {@link BigNumber} instances are also cloned.
	 *
	 * @return a new matrix that is a deep copy of this matrix
	 */
	@Override
	public BigNumberMatrix clone() {
		return new BigNumberMatrix(this);
	}

}

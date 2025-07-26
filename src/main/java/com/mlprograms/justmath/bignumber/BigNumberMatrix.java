package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.math.MatrixMath;
import com.mlprograms.justmath.bignumber.matrix.MatrixElementConsumer;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class BigNumberMatrix {

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
	 * All values are parsed into {@link BigNumber} using the provided locale.
	 *
	 * @param values
	 * 	2D list of string values; all rows must have equal length
	 * @param locale
	 * 	the locale used to parse the strings into {@link BigNumber} values
	 *
	 * @throws IllegalArgumentException
	 * 	if any value is invalid or dimensions are inconsistent
	 */
	public BigNumberMatrix(@NonNull List<List<String>> values, @NonNull Locale locale) {
		this.locale = locale;
		this.data = new ArrayList<>();
		this.rows = new BigNumber(String.valueOf(values.size()), locale);
		this.columns = new BigNumber(String.valueOf(values.getFirst().size()), locale);

		fillFromStringList(values);
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
	private static List<List<String>> parseMatrixString(String input) {
		List<List<String>> result = new ArrayList<>();
		if (input.trim().isEmpty()) {
			throw new IllegalArgumentException("Matrix string must not be empty.");
		}

		String[] rows = input.split(";");
		int expectedCols = -1;

		for (String row : rows) {
			String[] cols = row.trim().split(",");
			if (expectedCols == -1) {
				expectedCols = cols.length;
			} else if (cols.length != expectedCols) {
				throw new IllegalArgumentException("All rows must have same column count.");
			}

			List<String> parsedRow = new ArrayList<>();

			for (String col : cols) {
				String trimmed = col.trim();
				if (trimmed.isEmpty()) throw new IllegalArgumentException("Matrix entry must not be empty.");
				parsedRow.add(trimmed);
			}

			result.add(parsedRow);
		}

		return result;
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
	private void fillFromStringList(List<List<String>> values) {
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
	public void set(BigNumber row, BigNumber col, BigNumber value) {
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
	public BigNumber get(BigNumber row, BigNumber col) {
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
	private void validateIndex(BigNumber index, BigNumber max, String type) {
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
	public BigNumberMatrix add(@NonNull BigNumberMatrix other) {
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
	public BigNumberMatrix subtract(@NonNull BigNumberMatrix other) {
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
	public BigNumberMatrix multiply(@NonNull BigNumberMatrix other) {
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
	public BigNumberMatrix divide(@NonNull BigNumberMatrix other) {
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
	public BigNumberMatrix scalarMultiply(BigNumber scalar) {
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
	 * Checks whether the matrix is square (i.e., number of rows equals number of columns).
	 *
	 * @return true if the matrix is square, false otherwise
	 */
	public boolean isSquare() {
		return rows.equals(columns);
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
	 * Checks whether this matrix is an identity matrix:
	 * all diagonal elements are 1 and all off-diagonal elements are 0.
	 *
	 * @return true if the matrix is an identity matrix, false otherwise
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
				if (i.equals(j)) {
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

}

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

package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.math.MatrixMath;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a matrix with arbitrary-precision BigNumber values for both elements and dimensions.
 * Internally uses List<List<BigNumber>> to store the data.
 */
@Getter
public class BigNumberMatrix {

	private final BigNumber rows;
	private final BigNumber columns;
	private final List<List<BigNumber>> data;
	private final Locale locale;

	/**
	 * Constructs a BigNumberMatrix with the given number of rows and columns.
	 *
	 * @param rows
	 * 	Number of rows (must be a non-negative whole number)
	 * @param columns
	 * 	Number of columns (must be a non-negative whole number)
	 */
	public BigNumberMatrix(@NonNull final BigNumber rows, @NonNull final BigNumber columns, @NonNull final Locale locale) {
		if (!rows.isInteger() || !columns.isInteger() || rows.isNegative() || columns.isNegative()) {
			throw new IllegalArgumentException("Matrix dimensions must be non-negative integer numbers.");
		}

		BigNumber integerLimit = new BigNumber(String.valueOf(Integer.MAX_VALUE));
		if (rows.isGreaterThan(integerLimit) || columns.isGreaterThan(integerLimit)) {
			throw new IllegalArgumentException("Matrix dimensions must be less than " + integerLimit + ".");
		}

		this.rows = rows;
		this.columns = columns;
		this.locale = locale;
		this.data = new ArrayList<>();

		initializeData(data, rows, columns, locale);
	}

	// TODO
	public BigNumberMatrix add(@NonNull final BigNumberMatrix bigNumberMatrix) {
		return MatrixMath.add(this, bigNumberMatrix, this.getLocale());
	}

	// TODO
	public BigNumberMatrix subtract(@NonNull final BigNumberMatrix bigNumberMatrix) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// TODO
	public BigNumberMatrix multiply(@NonNull final BigNumberMatrix bigNumberMatrix) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// TODO
	public BigNumberMatrix divide(@NonNull final BigNumberMatrix bigNumberMatrix) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Initializes the matrix data with the specified number of rows and columns.
	 * Each element is initialized to BigNumbers.ZERO.
	 *
	 * @param data
	 * 	The list to populate with matrix rows.
	 * @param rows
	 * 	The number of rows to create.
	 * @param columns
	 * 	The number of columns to create in each row.
	 */
	private void initializeData(List<List<BigNumber>> data, BigNumber rows, BigNumber columns, Locale locale) {
		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(rows); i = i.add(BigNumbers.ONE)) {
			List<BigNumber> row = new ArrayList<>();
			for (BigNumber j = BigNumbers.ZERO; j.isLessThan(columns); j = j.add(BigNumbers.ONE)) {
				row.add(new BigNumber("0", locale));
			}
			data.add(row);
		}
	}

	/**
	 * Sets the value at the specified position in the matrix.
	 *
	 * @param row
	 * 	The row index (zero-based, as BigNumber)
	 * @param col
	 * 	The column index (zero-based, as BigNumber)
	 * @param value
	 * 	The BigNumber value to set
	 */
	public void set(BigNumber row, BigNumber col, BigNumber value) {
		checkRowIndex(row);
		checkColIndex(col);

		data.get(row.intValue()).set(col.intValue(), value);
	}

	/**
	 * Gets the value at the specified position in the matrix.
	 *
	 * @param row
	 * 	The row index (zero-based, as BigNumber)
	 * @param col
	 * 	The column index (zero-based, as BigNumber)
	 *
	 * @return The BigNumber value at that position
	 */
	public BigNumber get(BigNumber row, BigNumber col) {
		checkRowIndex(row);
		checkColIndex(col);

		return data.get(row.intValue()).get(col.intValue());
	}

	/**
	 * Checks if the given row index is valid for this matrix.
	 *
	 * @param row
	 * 	The row index to check (as BigNumber).
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if the index is negative, not an integer, or out of bounds.
	 */
	private void checkRowIndex(BigNumber row) {
		if (row.isNegative() || row.isGreaterThanOrEqualTo(rows) || !row.isInteger()) {
			throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
		}
	}

	/**
	 * Checks if the given column index is valid for this matrix.
	 *
	 * @param col
	 * 	The column index to check (as BigNumber).
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if the index is negative, not an integer, or out of bounds.
	 */
	private void checkColIndex(BigNumber col) {
		if (col.isNegative() || col.isGreaterThanOrEqualTo(columns) || !col.isInteger()) {
			throw new IndexOutOfBoundsException("Column index out of bounds: " + col);
		}
	}

	/**
	 * Returns a string representation of the BigNumberMatrix, including its dimensions and data.
	 *
	 * @return A formatted string describing the matrix.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BigNumberMatrix[\n");
		sb.append("  rows=").append(rows).append(",\n");
		sb.append("  cols=").append(columns).append(",\n");
		sb.append("  data=[\n");
		for (List<BigNumber> row : data) {
			sb.append("  ").append(row).append(",\n");
		}
		sb.append("  ]\n");
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns a plain string representation of the matrix data only.
	 *
	 * @return A string showing the matrix rows and their contents.
	 */
	public String toPlainDataString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for (List<BigNumber> row : data) {
			sb.append("  ").append(row).append(",\n");
		}
		sb.append("]");
		return sb.toString();
	}

}

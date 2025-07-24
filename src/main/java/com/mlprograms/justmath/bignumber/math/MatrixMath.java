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

	// TODO: javadoc
	public static BigNumberMatrix add(@NonNull final BigNumberMatrix augend, @NonNull final BigNumberMatrix addend, @NonNull final Locale locale) {
		checkParamsForAdditionAndSubtraction(augend, addend);

		BigNumberMatrix result = new BigNumberMatrix(augend.getRows(), augend.getColumns(), augend.getLocale());

		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(augend.getRows()); row = row.add(BigNumbers.ONE)) {
			for (BigNumber column = BigNumbers.ZERO; column.isLessThan(augend.getColumns()); column = column.add(BigNumbers.ONE)) {
				BigNumber augendMatrixFieldValue = augend.get(row, column);
				BigNumber addendMatrixFieldValue = addend.get(row, column);
				result.set(row, column, augendMatrixFieldValue.add(addendMatrixFieldValue));
			}
		}

		return result;
	}

	// TODO: javadoc
	public static BigNumberMatrix subtract(@NonNull final BigNumberMatrix minuend, @NonNull final BigNumberMatrix subtrahend) {
		checkParamsForAdditionAndSubtraction(minuend, subtrahend);

		BigNumberMatrix result = new BigNumberMatrix(minuend.getRows(), minuend.getColumns(), minuend.getLocale());

		for (BigNumber row = BigNumbers.ZERO; row.isLessThan(minuend.getRows()); row = row.add(BigNumbers.ONE)) {
			for (BigNumber column = BigNumbers.ZERO; column.isLessThan(minuend.getColumns()); column = column.add(BigNumbers.ONE)) {
				BigNumber minuendMatrixFieldValue = minuend.get(row, column);
				BigNumber subtrahendMatrixFieldValue = subtrahend.get(row, column);
				result.set(row, column, minuendMatrixFieldValue.subtract(subtrahendMatrixFieldValue));
			}
		}

		return result;
	}

	// TODO
	public static BigNumberMatrix multiply(@NonNull final BigNumberMatrix multiplier, @NonNull final BigNumberMatrix multiplicand) {
		if(!multiplier.getColumns().isEqualTo(multiplicand.getRows())) {
			throw new IllegalArgumentException("The number of columns of the multiplier must be equal to the number of rows of the multiplicand");
		}

		throw new UnsupportedOperationException("Not yet implemented");
	}

	// TODO
	public static BigNumberMatrix divide(@NonNull final BigNumberMatrix dividend, @NonNull final BigNumberMatrix divisor) {
		throw new UnsupportedOperationException("Not yet implemented");
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
	private static void checkParamsForAdditionAndSubtraction(BigNumberMatrix augend, BigNumberMatrix addend) {
		if (!augend.getRows().isEqualTo(addend.getRows()) || !augend.getColumns().isEqualTo(addend.getColumns())) {
			throw new IllegalArgumentException("The rows and columns of augend and addend must be equal");
		}

		if (!augend.getRows().isGreaterThan(BigNumbers.ZERO) || !augend.getColumns().isGreaterThan(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("The rows and columns of augend and addend must be greater than zero");
		}
	}

}

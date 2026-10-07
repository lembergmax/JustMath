/*
 * Copyright (c) 2026 Max Lemberg
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
package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * The dimensions of a {@link BigNumberMatrix} are {@link BigNumber} values, and {@code BigNumber} has in-place
 * mutators ({@code negateThis()}, the setters). The matrix must therefore neither keep the instances the caller
 * passed in nor hand out its own: otherwise a later mutation on the caller's side would change the matrix.
 */
class BigNumberMatrixDimensionIsolationTest {

    @Test
    @DisplayName("mutating the BigNumbers passed to the dimension constructor does not change the matrix")
    void constructorArgumentsAreCopied() {
        final BigNumber rows = new BigNumber("2");
        final BigNumber columns = new BigNumber("3");
        final BigNumberMatrix matrix = new BigNumberMatrix(rows, columns, Locale.US);

        rows.negateThis();
        columns.negateThis();

        assertEquals("2", matrix.getRows().toString());
        assertEquals("3", matrix.getColumns().toString());
    }

    @Test
    @DisplayName("mutating the BigNumbers returned by getRows() and getColumns() does not change the matrix")
    void gettersReturnCopies() {
        final BigNumberMatrix matrix = new BigNumberMatrix("1,2,3;4,5,6", Locale.US);

        matrix.getRows().negateThis();
        matrix.getColumns().negateThis();

        assertEquals("2", matrix.getRows().toString());
        assertEquals("3", matrix.getColumns().toString());
    }
}

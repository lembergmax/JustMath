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

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Compares {@link BigNumberMatrix} with integer matrix arithmetic on {@link BigInteger}: the determinant, the
 * product, the transpose, the trace and the inverse. The entries are small integers, so every result except the
 * inverse is exact.
 */
class BigNumberMatrixPropertyTest {

    private static final int TRIES = 200;

    private static final int MAX_DIMENSION = 5;

    private static final int MAX_ENTRY = 9;

    private static final BigDecimal INVERSE_TOLERANCE = new BigDecimal("1E-80");

    private record IntegerMatrix(BigInteger[][] entries) {

        int rows() {
            return entries.length;
        }

        int columns() {
            return entries[0].length;
        }

        IntegerMatrix multiply(final IntegerMatrix other) {
            final BigInteger[][] product = new BigInteger[rows()][other.columns()];
            for (int row = 0; row < rows(); row++) {
                for (int column = 0; column < other.columns(); column++) {
                    BigInteger sum = BigInteger.ZERO;
                    for (int inner = 0; inner < columns(); inner++) {
                        sum = sum.add(entries[row][inner].multiply(other.entries[inner][column]));
                    }
                    product[row][column] = sum;
                }
            }
            return new IntegerMatrix(product);
        }

        IntegerMatrix transpose() {
            final BigInteger[][] transposed = new BigInteger[columns()][rows()];
            for (int row = 0; row < rows(); row++) {
                for (int column = 0; column < columns(); column++) {
                    transposed[column][row] = entries[row][column];
                }
            }
            return new IntegerMatrix(transposed);
        }

        BigInteger determinant() {
            return determinantOf(entries);
        }

        BigInteger trace() {
            BigInteger sum = BigInteger.ZERO;
            for (int index = 0; index < rows(); index++) {
                sum = sum.add(entries[index][index]);
            }
            return sum;
        }

        BigNumberMatrix toMatrix() {
            final List<List<BigNumber>> data = new ArrayList<>();
            for (final BigInteger[] row : entries) {
                final List<BigNumber> converted = new ArrayList<>();
                for (final BigInteger entry : row) {
                    converted.add(new BigNumber(entry.toString()));
                }
                data.add(converted);
            }
            return new BigNumberMatrix(data, Locale.US);
        }

        @Override
        public String toString() {
            return java.util.Arrays.deepToString(entries);
        }
    }

    private static BigInteger determinantOf(final BigInteger[][] matrix) {
        final int size = matrix.length;
        if (size == 1) {
            return matrix[0][0];
        }
        BigInteger determinant = BigInteger.ZERO;
        for (int column = 0; column < size; column++) {
            final BigInteger[][] minor = new BigInteger[size - 1][size - 1];
            for (int row = 1; row < size; row++) {
                int target = 0;
                for (int other = 0; other < size; other++) {
                    if (other != column) {
                        minor[row - 1][target++] = matrix[row][other];
                    }
                }
            }
            final BigInteger term = matrix[0][column].multiply(determinantOf(minor));
            determinant = column % 2 == 0 ? determinant.add(term) : determinant.subtract(term);
        }
        return determinant;
    }

    private static Arbitrary<IntegerMatrix> matrices(final int rows, final int columns) {
        return Arbitraries.integers().between(-MAX_ENTRY, MAX_ENTRY).map(BigInteger::valueOf)
                .array(BigInteger[].class).ofSize(columns)
                .array(BigInteger[][].class).ofSize(rows)
                .map(IntegerMatrix::new);
    }

    @Provide
    Arbitrary<IntegerMatrix> squareMatrices() {
        return Arbitraries.integers().between(1, MAX_DIMENSION).flatMap(size -> matrices(size, size));
    }

    @Provide
    Arbitrary<List<IntegerMatrix>> squarePairs() {
        return Arbitraries.integers().between(1, MAX_DIMENSION)
                .flatMap(size -> Combinators.combine(matrices(size, size), matrices(size, size)).as(List::of));
    }

    @Provide
    Arbitrary<List<IntegerMatrix>> compatibleFactors() {
        return Combinators.combine(
                        Arbitraries.integers().between(1, MAX_DIMENSION),
                        Arbitraries.integers().between(1, MAX_DIMENSION),
                        Arbitraries.integers().between(1, MAX_DIMENSION))
                .flatAs((rows, inner, columns) -> Combinators.combine(matrices(rows, inner), matrices(inner, columns)).as(List::of));
    }

    @Provide
    Arbitrary<IntegerMatrix> invertibleMatrices() {
        return squareMatrices().filter(matrix -> matrix.determinant().signum() != 0);
    }

    private static void assertSameEntries(final IntegerMatrix expected, final BigNumberMatrix actual, final String description) {
        assertEquals(expected.rows(), actual.getRows().intValue(), description + ": rows");
        assertEquals(expected.columns(), actual.getColumns().intValue(), description + ": columns");
        for (int row = 0; row < expected.rows(); row++) {
            for (int column = 0; column < expected.columns(); column++) {
                final BigDecimal entry = actual.get(BigNumber.valueOf(row), BigNumber.valueOf(column)).toBigDecimal();
                assertEquals(0, new BigDecimal(expected.entries()[row][column]).compareTo(entry),
                        description + ": entry (" + row + ", " + column + ") expected " + expected.entries()[row][column] + " but was " + entry);
            }
        }
    }

    @Property(tries = TRIES, seed = SEED)
    void determinantMatchesTheCofactorExpansion(@ForAll("squareMatrices") final IntegerMatrix matrix) {
        assertEquals(0, new BigDecimal(matrix.determinant()).compareTo(matrix.toMatrix().determinant().toBigDecimal()), "determinant of " + matrix);
    }

    @Property(tries = TRIES, seed = SEED)
    void theDeterminantOfAProductIsTheProductOfTheDeterminants(@ForAll("squarePairs") final List<IntegerMatrix> pair) {
        final BigNumber product = pair.get(0).toMatrix().multiply(pair.get(1).toMatrix()).determinant();

        assertEquals(0, new BigDecimal(pair.get(0).determinant().multiply(pair.get(1).determinant())).compareTo(product.toBigDecimal()), "det(A*B) of " + pair);
    }

    @Property(tries = TRIES, seed = SEED)
    void productMatchesTheIntegerProduct(@ForAll("compatibleFactors") final List<IntegerMatrix> factors) {
        assertSameEntries(factors.get(0).multiply(factors.get(1)), factors.get(0).toMatrix().multiply(factors.get(1).toMatrix()), "product of " + factors);
    }

    @Property(tries = TRIES, seed = SEED)
    void theTransposeOfAProductIsTheProductOfTheTransposesInReverse(@ForAll("compatibleFactors") final List<IntegerMatrix> factors) {
        final BigNumberMatrix left = factors.get(0).toMatrix();
        final BigNumberMatrix right = factors.get(1).toMatrix();

        assertSameEntries(factors.get(1).transpose().multiply(factors.get(0).transpose()), left.multiply(right).transpose(), "transpose of " + factors);
        assertTrue(left.transpose().transpose().equalsMatrix(left), "transposing twice returns the matrix");
    }

    @Property(tries = TRIES, seed = SEED)
    void traceOfASumIsTheSumOfTheTraces(@ForAll("squarePairs") final List<IntegerMatrix> pair) {
        final BigNumber trace = pair.get(0).toMatrix().add(pair.get(1).toMatrix()).trace();

        assertEquals(0, new BigDecimal(pair.get(0).trace().add(pair.get(1).trace())).compareTo(trace.toBigDecimal()), "trace of " + pair);
    }

    @Property(tries = TRIES, seed = SEED)
    void addingAndSubtractingTheSameMatrixReturnsTheOriginal(@ForAll("squarePairs") final List<IntegerMatrix> pair) {
        final BigNumberMatrix original = pair.get(0).toMatrix();

        assertTrue(original.add(pair.get(1).toMatrix()).subtract(pair.get(1).toMatrix()).equalsMatrix(original), "A + B - B for " + pair);
    }

    @Property(tries = TRIES, seed = SEED)
    void aMatrixTimesItsInverseIsTheIdentity(@ForAll("invertibleMatrices") final IntegerMatrix matrix) {
        final BigNumberMatrix original = matrix.toMatrix();

        final BigNumberMatrix product = original.multiply(original.inverse());

        for (int row = 0; row < matrix.rows(); row++) {
            for (int column = 0; column < matrix.columns(); column++) {
                final BigDecimal expected = row == column ? BigDecimal.ONE : BigDecimal.ZERO;
                final BigDecimal entry = product.get(BigNumber.valueOf(row), BigNumber.valueOf(column)).toBigDecimal();
                assertTrue(expected.subtract(entry).abs().compareTo(INVERSE_TOLERANCE) <= 0,
                        "entry (" + row + ", " + column + ") of A * A^-1 was " + entry + " for " + matrix);
            }
        }
    }

}

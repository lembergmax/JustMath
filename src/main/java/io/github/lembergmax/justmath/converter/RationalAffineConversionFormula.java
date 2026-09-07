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

package io.github.lembergmax.justmath.converter;

import java.math.MathContext;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import lombok.NonNull;

/**
 * Immutable affine conversion formula whose scale <em>and</em> offset are expressed as exact rationals:
 *
 * <pre>
 * base  = value * (scaleNum / scaleDen) + (offsetNum / offsetDen)
 * value = (base - offsetNum / offsetDen) * (scaleDen / scaleNum)
 * </pre>
 *
 * <p>This exists for affine units whose scale and/or offset have no finite decimal representation — most
 * notably temperature ({@code °F}: scale {@code 5/9}, offset {@code -160/9}). Storing those as pre-rounded
 * decimals (e.g. {@code 0.555…556} / {@code -17.777…778}) bakes a rounding error into every conversion, so
 * that even canonical points such as {@code 32 °F → 0 °C} and {@code 100 °C → 212 °F} come out inexact.</p>
 *
 * <p>Both directions are evaluated as a single exact-integer-style numerator over a single denominator,
 * deferring exactly one division to conversion time using the caller-supplied {@link MathContext}. With a
 * sufficient precision the canonical points are exact.</p>
 *
 * <p>The type is package-private to keep the public API minimal; instances are obtained via
 * {@link ConversionFormulas#affine(BigNumber, BigNumber, BigNumber, BigNumber)}. Immutable and thread-safe.</p>
 */
final class RationalAffineConversionFormula implements ConversionFormula {

    private final BigNumber scaleNumerator;
    private final BigNumber scaleDenominator;
    private final BigNumber offsetNumerator;
    private final BigNumber offsetDenominator;

    /** Precomputed {@code scaleDenominator * offsetDenominator}, the common denominator of {@link #toBase}. */
    private final BigNumber toBaseDenominator;

    /** Precomputed {@code offsetDenominator * scaleNumerator}, the common denominator of {@link #fromBase}. */
    private final BigNumber fromBaseDenominator;

    /**
     * Creates a new exact rational affine conversion formula.
     *
     * @param scaleNumerator    numerator of the scale into base units; must not be {@code null} and must not be zero
     * @param scaleDenominator  denominator of the scale into base units; must not be {@code null} and must not be zero
     * @param offsetNumerator   numerator of the offset into base units; must not be {@code null} (may be zero)
     * @param offsetDenominator denominator of the offset into base units; must not be {@code null} and must not be zero
     * @throws IllegalArgumentException if any required value is zero
     */
    RationalAffineConversionFormula(@NonNull final BigNumber scaleNumerator,
                                    @NonNull final BigNumber scaleDenominator,
                                    @NonNull final BigNumber offsetNumerator,
                                    @NonNull final BigNumber offsetDenominator) {
        if (scaleNumerator.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale numerator must not be zero");
        }
        if (scaleDenominator.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale denominator must not be zero");
        }
        if (offsetDenominator.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("offset denominator must not be zero");
        }
        this.scaleNumerator = scaleNumerator;
        this.scaleDenominator = scaleDenominator;
        this.offsetNumerator = offsetNumerator;
        this.offsetDenominator = offsetDenominator;
        this.toBaseDenominator = scaleDenominator.multiply(offsetDenominator);
        this.fromBaseDenominator = offsetDenominator.multiply(scaleNumerator);
    }

    /**
     * Converts a value in the concrete unit to the base unit:
     * <pre>
     * base = (value * scaleNum * offsetDen + offsetNum * scaleDen) / (scaleDen * offsetDen)
     * </pre>
     * The single division is the only rounding step.
     *
     * @param value       input value in the concrete unit; must not be {@code null}
     * @param mathContext precision/rounding for the final division; must not be {@code null}
     * @return value in the base unit; never {@code null}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        final BigNumber numerator = value.multiply(scaleNumerator).multiply(offsetDenominator)
                .add(offsetNumerator.multiply(scaleDenominator));
        return numerator.divide(toBaseDenominator, mathContext);
    }

    /**
     * Converts a value in the base unit back to the concrete unit:
     * <pre>
     * value = (base * offsetDen - offsetNum) * scaleDen / (offsetDen * scaleNum)
     * </pre>
     * The single division is the only rounding step.
     *
     * @param baseValue   input value in the base unit; must not be {@code null}
     * @param mathContext precision/rounding for the final division; must not be {@code null}
     * @return value in the concrete unit; never {@code null}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        final BigNumber numerator = baseValue.multiply(offsetDenominator).subtract(offsetNumerator)
                .multiply(scaleDenominator);
        return numerator.divide(fromBaseDenominator, mathContext);
    }

}

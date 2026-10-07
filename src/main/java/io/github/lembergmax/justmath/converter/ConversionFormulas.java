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
import lombok.experimental.UtilityClass;

/**
 * Factory methods for creating common {@link ConversionFormula} implementations.
 *
 * <p>
 * This class is intentionally small and stable: it provides reusable conversion strategies
 * that can be referenced by the internal unit catalog as well as by library consumers who
 * want to define their own unit registries or custom units outside the built-in catalog.
 * </p>
 *
 * <p>
 * The returned {@link ConversionFormula} implementations are immutable and thread-safe.
 * </p>
 */
@UtilityClass
public class ConversionFormulas {

    /**
     * Creates a purely linear conversion formula of the form:
     *
     * <pre>
     * base = value * scale
     * </pre>
     *
     * <p>
     * This is equivalent to an affine conversion with an offset of {@code 0}.
     * </p>
     *
     * @param scale multiplicative factor into the base unit; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the linear mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    public static ConversionFormula linear(@NonNull final BigNumber scale) {
        return affine(scale, BigNumbers.ZERO);
    }

    /**
     * Creates an exact rational linear conversion formula of the form:
     *
     * <pre>
     * base = value * numerator / denominator
     * </pre>
     *
     * <p>
     * Use this factory when the conversion factor has no finite decimal representation
     * (e.g. {@code 1/3}, {@code 1000/3600} for {@code km/h -> m/s}, or {@code 1/60} for
     * {@code min -> s} derived units). Storing the factor as an exact ratio avoids
     * accumulated rounding errors that arise from pre-rounded decimal scale factors:
     * the division is deferred and performed at conversion time using the caller-supplied
     * {@link MathContext}.
     * </p>
     *
     * @param numerator   numerator of the scale factor into the base unit; must not be {@code null} and must not be zero
     * @param denominator denominator of the scale factor into the base unit; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the exact rational linear mapping; never {@code null}
     * @throws IllegalArgumentException if {@code numerator} or {@code denominator} is zero
     */
    public static ConversionFormula linear(@NonNull final BigNumber numerator, @NonNull final BigNumber denominator) {
        return new RationalConversionFormula(numerator, denominator);
    }

    /**
     * Creates an affine conversion formula of the form:
     *
     * <pre>
     * base = value * scale + offset
     * </pre>
     *
     * <p>
     * The inverse conversion is:
     * </p>
     *
     * <pre>
     * value = (base - offset) / scale
     * </pre>
     *
     * @param scale  multiplicative factor into the base unit; must not be {@code null} and must not be zero
     * @param offset additive offset into the base unit; must not be {@code null}
     * @return a conversion formula implementing the affine mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    public static ConversionFormula affine(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        return new AffineConversionFormula(scale, offset);
    }

    /**
     * Creates an exact rational <em>affine</em> conversion formula of the form:
     *
     * <pre>
     * base = value * (scaleNumerator / scaleDenominator) + (offsetNumerator / offsetDenominator)
     * </pre>
     *
     * <p>
     * Use this factory for affine units whose scale and/or offset have no finite decimal representation —
     * most notably temperature (e.g. {@code °F}: scale {@code 5/9}, offset {@code -160/9}). Unlike
     * {@link #affine(BigNumber, BigNumber)} with pre-rounded decimal constants, both directions are
     * evaluated as a single numerator over a single denominator with exactly one deferred division at the
     * caller-supplied {@link MathContext}, so canonical points (such as {@code 32 °F → 0 °C}) come out exact.
     * </p>
     *
     * @param scaleNumerator    numerator of the scale into the base unit; must not be {@code null} and must not be zero
     * @param scaleDenominator  denominator of the scale into the base unit; must not be {@code null} and must not be zero
     * @param offsetNumerator   numerator of the offset into the base unit; must not be {@code null} (may be zero)
     * @param offsetDenominator denominator of the offset into the base unit; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the exact rational affine mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scaleNumerator}, {@code scaleDenominator}, or {@code offsetDenominator} is zero
     */
    public static ConversionFormula affine(@NonNull final BigNumber scaleNumerator,
                                           @NonNull final BigNumber scaleDenominator,
                                           @NonNull final BigNumber offsetNumerator,
                                           @NonNull final BigNumber offsetDenominator) {
        return new RationalAffineConversionFormula(scaleNumerator, scaleDenominator, offsetNumerator, offsetDenominator);
    }

    /**
     * Creates a reciprocal conversion formula of the form:
     *
     * <pre>
     * base = scale / value
     * </pre>
     *
     * <p>
     * The inverse conversion is symmetric: {@code value = scale / base}.
     * </p>
     *
     * <p>
     * This formula is required for units that represent the reciprocal of the category
     * base unit — for example {@code L/100 km} relative to the base {@code m/L} for
     * fuel consumption.
     * </p>
     *
     * @param scale multiplicative factor used in the reciprocal mapping; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the reciprocal mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    public static ConversionFormula reciprocal(@NonNull final BigNumber scale) {
        return new ReciprocalConversionFormula(scale);
    }

    /**
     * Creates an exact rational reciprocal conversion formula of the form:
     *
     * <pre>
     * base = numerator / (value * denominator)
     * </pre>
     *
     * <p>
     * Use this factory for reciprocal units (e.g. {@code gallon/mile}, {@code L/100 km}) whose
     * scale is itself a non-terminating ratio (e.g. {@code 1609.344 / 3.785411784}). Storing
     * the scale as an exact rational pair avoids the rounding artifacts that arise from a
     * pre-rounded decimal scale: the division is deferred and executed at conversion time
     * with the caller-supplied {@link MathContext}.
     * </p>
     *
     * @param numerator   numerator of the reciprocal scale; must not be {@code null} and must not be zero
     * @param denominator denominator of the reciprocal scale; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the exact rational reciprocal mapping; never {@code null}
     * @throws IllegalArgumentException if {@code numerator} or {@code denominator} is zero
     */
    public static ConversionFormula reciprocal(@NonNull final BigNumber numerator, @NonNull final BigNumber denominator) {
        return new RationalReciprocalConversionFormula(numerator, denominator);
    }

}

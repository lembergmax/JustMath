package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NonNull;

import java.math.MathContext;

/**
 * Internal conversion formula that models unit conversions using a scale and an offset.
 *
 * <p>
 * This formula is intentionally not exposed as a separate "linear" or "affine" public concept.
 * Instead, every built-in unit is described uniformly using these parameters:
 * </p>
 *
 * <pre>
 * base  = value * scale + offset
 * value = (base - offset) / scale
 * </pre>
 *
 * <p>
 * A purely linear conversion is simply a special case where {@code offset = 0}.
 * </p>
 *
 * <p>
 * This class is package-private to keep the public API small and stable.
 * Library users interact with conversions through {@link UnitConverter}, {@link UnitElements}
 * and {@link UnitDefinition}, not through concrete formula classes.
 * </p>
 */
final class ScaleOffsetConversionFormula implements ConversionFormula {

    /**
     * Multiplicative factor used to scale values into the category base unit.
     * <p>
     * Must never be zero because {@link #fromBase(BigNumber, MathContext)} divides by this value.
     * </p>
     */
    private final BigNumber scale;

    /**
     * Additive offset applied after scaling.
     * <p>
     * For most units (e.g., length, mass) this is {@code 0}. For offset-based conversions
     * such as temperature, this value is typically non-zero.
     * </p>
     */
    private final BigNumber offset;

    /**
     * Creates a new formula instance.
     *
     * <p>
     * Use {@link #of(BigNumber, BigNumber)} to create instances with validation.
     * </p>
     *
     * @param scale multiplicative factor; must not be {@code null}
     * @param offset additive offset; must not be {@code null}
     */
    private ScaleOffsetConversionFormula(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        this.scale = scale;
        this.offset = offset;
    }

    /**
     * Creates a validated {@link ConversionFormula} instance based on {@code scale} and {@code offset}.
     *
     * @param scale multiplicative factor; must not be {@code null} and must not be zero
     * @param offset additive offset; must not be {@code null}
     * @return an immutable, thread-safe formula instance; never {@code null}
     * @throws IllegalArgumentException if {@code scale} equals zero
     */
    static ConversionFormula of(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        if (scale.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale must not be zero");
        }
        return new ScaleOffsetConversionFormula(scale, offset);
    }

    /**
     * Converts a concrete unit value to the category base unit value.
     *
     * <p>
     * The {@link MathContext} parameter is accepted to comply with the interface contract.
     * The operation typically does not require rounding because it is composed of
     * multiplication and addition, but implementations must still accept it for consistency.
     * </p>
     *
     * @param value the input value expressed in the concrete unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the converted value expressed in the base unit; never {@code null}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return value.multiply(scale).add(offset);
    }

    /**
     * Converts a category base unit value into the concrete unit value.
     *
     * <p>
     * This operation performs a division by {@link #scale} and therefore uses
     * {@link MathContext} to control rounding and precision.
     * </p>
     *
     * @param baseValue the input value expressed in the base unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the converted value expressed in the concrete unit; never {@code null}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return baseValue.subtract(offset).divide(scale, mathContext);
    }

}
package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;

/**
 * Immutable unit definition that contains:
 * <ul>
 *   <li>human-readable metadata (display name, symbol)</li>
 *   <li>the unit category</li>
 *   <li>conversion behavior to and from the category base unit</li>
 * </ul>
 *
 * <p>
 * This type is intentionally separate from {@link Unit}. The {@link Unit} enum remains a pure
 * identifier list (no metadata, no conversion values).
 * </p>
 *
 * <p>
 * The converter module treats this record as internal "unit configuration".
 * Public access is provided via {@link UnitElements}.
 * </p>
 *
 * @param category the domain category of the unit; must not be {@code null}
 * @param displayName human-readable name intended for UI display; must not be {@code null}
 * @param symbol short unit symbol used for parsing/formatting; must not be {@code null}
 * @param formula conversion strategy used for converting values to/from base; must not be {@code null}
 */
record UnitDefinition(
        @NonNull UnitCategory category,
        @NonNull String displayName,
        @NonNull String symbol,
        @NonNull ConversionFormula formula
) {

    /**
     * Converts a value expressed in this unit into the base unit of the unit's category.
     *
     * @param value the input value expressed in this concrete unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the converted value expressed in the category base unit; never {@code null}
     */
    BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return formula.toBase(value, mathContext);
    }

    /**
     * Converts a value expressed in the base unit of the category into this unit.
     *
     * @param baseValue the input value expressed in the category base unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the converted value expressed in this concrete unit; never {@code null}
     */
    BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return formula.fromBase(baseValue, mathContext);
    }

}
package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;

/**
 * Strategy interface that defines how a concrete unit value is converted
 * to the base unit of a category and back.
 * <p>
 * A {@link ConversionFormula} is the conversion "engine" for a unit definition.
 * It must be:
 * </p>
 * <ul>
 *   <li><strong>Immutable</strong> (no mutable state)</li>
 *   <li><strong>Thread-safe</strong> (safe to reuse across threads)</li>
 *   <li><strong>Deterministic</strong> (same input produces same output)</li>
 * </ul>
 *
 * <p>
 * The converter module uses {@link MathContext} to control precision and rounding.
 * Even if an implementation does not need the {@link MathContext} for some operations
 * (e.g., multiplication), it must accept it to keep a stable contract and to allow
 * consistent use across different formulas.
 * </p>
 */
public interface ConversionFormula {

    /**
     * Converts a value expressed in a concrete unit into the base unit of the unit's category.
     *
     * <p>
     * Example (length category, base = meter):
     * converting {@code 1 km} to base might produce {@code 1000 m}.
     * </p>
     *
     * @param value       the value expressed in the concrete unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the corresponding value expressed in the category base unit; never {@code null}
     */
    BigNumber toBase(@NonNull BigNumber value, @NonNull MathContext mathContext);

    /**
     * Converts a value expressed in the base unit of the unit's category back into the concrete unit.
     *
     * <p>
     * Example (length category, base = meter):
     * converting {@code 1000 m} from base might produce {@code 1 km}.
     * </p>
     *
     * @param baseValue   the value expressed in the category base unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the corresponding value expressed in the concrete unit; never {@code null}
     */
    BigNumber fromBase(@NonNull BigNumber baseValue, @NonNull MathContext mathContext);

}
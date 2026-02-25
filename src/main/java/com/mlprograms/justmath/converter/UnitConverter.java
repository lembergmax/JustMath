package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * Converts numeric values between units of the same category.
 * <p>
 * Instances are immutable and thread-safe.
 * </p>
 */
public final class UnitConverter {

    /**
     * Math context used to control precision and rounding.
     */
    @Getter
    private final MathContext mathContext;

    /**
     * Creates a converter using the library's default division precision.
     */
    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    /**
     * Creates a converter using a math context derived from the given precision.
     *
     * @param divisionPrecision precision used to build the internal {@link MathContext}
     */
    public UnitConverter(final int divisionPrecision) {
        this(CalculatorEngineUtils.getDefaultMathContext(divisionPrecision));
    }

    /**
     * Creates a converter with an explicit math context.
     *
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     */
    public UnitConverter(@NonNull final MathContext mathContext) {
        this.mathContext = mathContext;
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit}.
     * <p>
     * Conversions are only valid within the same unit category. If the units belong to different
     * categories, a {@link UnitConversionException} is thrown.
     * </p>
     *
     * @param value    input value; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit   target unit; must not be {@code null}
     * @return converted value; never {@code null}
     * @throws UnitConversionException if the units are from different categories
     */
    public BigNumber convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        final var fromCategory = UnitElements.getCategory(fromUnit);
        final var toCategory = UnitElements.getCategory(toUnit);

        if (fromCategory != toCategory) {
            throw new UnitConversionException(
                    "Incompatible unit categories: cannot convert from " + fromCategory + " (" + fromUnit + ") to "
                            + toCategory + " (" + toUnit + ")."
            );
        }

        final BigNumber valueInBase = UnitElements.toBase(fromUnit, value);
        return UnitElements.fromBase(toUnit, valueInBase, mathContext);
    }

}
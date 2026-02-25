package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.NonNull;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * Converts numeric values between units of the same {@link UnitCategory}.
 *
 * <p>
 * Instances of this class are immutable and thread-safe. You can keep one instance as a singleton
 * per desired precision and reuse it across the application.
 * </p>
 *
 * <p>
 * The converter enforces a strict rule:
 * </p>
 * <ul>
 *   <li>Conversions are only allowed within the same category (e.g., LENGTH -> LENGTH).</li>
 *   <li>Cross-category conversions are rejected with {@link UnitConversionException}.</li>
 * </ul>
 */
public final class UnitConverter {

    /**
     * Math context used for conversion operations that require rounding/precision.
     *
     * <p>
     * In this module, rounding is typically relevant for division operations when converting
     * from base units back into a concrete unit.
     * </p>
     */
    private final MathContext mathContext;

    /**
     * Creates a converter using the library default division precision.
     *
     * <p>
     * The precision value originates from {@link DEFAULT_DIVISION_PRECISION} and is used to create
     * the internal {@link MathContext} via {@link CalculatorEngineUtils#getDefaultMathContext(int)}.
     * </p>
     */
    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    /**
     * Creates a converter using a {@link MathContext} derived from the given division precision.
     *
     * <p>
     * This constructor is a convenience for library users who want to control precision with a simple integer.
     * Internally it uses {@link CalculatorEngineUtils#getDefaultMathContext(int)}.
     * </p>
     *
     * @param divisionPrecision precision used to build the internal math context
     */
    public UnitConverter(final int divisionPrecision) {
        this(CalculatorEngineUtils.getDefaultMathContext(divisionPrecision));
    }

    /**
     * Creates a converter with an explicit {@link MathContext}.
     *
     * <p>
     * This is the most flexible constructor and should be preferred when the caller
     * already maintains an application-wide math context.
     * </p>
     *
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     */
    public UnitConverter(@NonNull final MathContext mathContext) {
        this.mathContext = mathContext;
    }

    /**
     * Returns the {@link MathContext} used by this converter instance.
     *
     * @return math context; never {@code null}
     */
    public MathContext getMathContext() {
        return mathContext;
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit}.
     *
     * <p>
     * The conversion always follows the same conceptual path:
     * </p>
     * <ol>
     *   <li>Convert {@code value} to the category base unit (e.g., meters for LENGTH).</li>
     *   <li>Convert the base-unit value to {@code toUnit}.</li>
     * </ol>
     *
     * <p>
     * If the units belong to different categories, the conversion is rejected.
     * </p>
     *
     * @param value input value; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit target unit; must not be {@code null}
     * @return converted value expressed in {@code toUnit}; never {@code null}
     * @throws UnitConversionException if {@code fromUnit} and {@code toUnit} are in different categories
     */
    public BigNumber convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        final UnitCategory fromCategory = UnitElements.getCategory(fromUnit);
        final UnitCategory toCategory = UnitElements.getCategory(toUnit);

        if (fromCategory != toCategory) {
            throw new UnitConversionException(
                    "Incompatible unit categories: cannot convert from " + fromCategory + " (" + fromUnit + ") to "
                            + toCategory + " (" + toUnit + ")."
            );
        }

        final BigNumber valueInBase = UnitElements.toBase(fromUnit, value, mathContext);
        return UnitElements.fromBase(toUnit, valueInBase, mathContext);
    }

}
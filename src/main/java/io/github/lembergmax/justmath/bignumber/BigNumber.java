package io.github.lembergmax.justmath.bignumber;

import io.github.lembergmax.justmath.bignumber.math.BasicMath;
import io.github.lembergmax.justmath.bignumber.math.MathTools;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.ParseResult;
import lombok.Getter;

import java.math.MathContext;
import java.util.Locale;
import java.util.Objects;

public class BigNumber {

    /**
     * The locale all calculations are done in. It defines "." as the decimal separator.
     */
    public static final Locale INTERNAL_LOCALE = Locale.US;
    /**
     * Parses the strings the public constructor receives.
     */
    private static final BigNumberParser bigNumberParser = new BigNumberParser();

    /**
     * The digits before the decimal separator, without sign and without grouping separators.
     * Never empty, a number without integer digits has "0".
     */
    @Getter
    private String beforeDecimal;
    /**
     * The digits after the decimal separator. A number without a fractional part has "0".
     */
    @Getter
    private String afterDecimal;
    /**
     * Whether the number has a minus sign. The parser never marks a parsed zero as negative,
     * but {@link #negate()} does, so a zero can carry a minus.
     */
    @Getter
    private boolean isNegative;

    /**
     * Creates a number from a string.
     * A sign is only allowed as the very first character. Grouping separators and leading and
     * trailing zeros are removed, and an empty string is 0.
     *
     * @param number the number as text, for example "-1.234,5" for {@link Locale#GERMAN}
     * @param locale the locale the number is written in, it defines the decimal and grouping separator
     * @throws io.github.lembergmax.justmath.exception.NotAValidCharacterException        if the number contains a
     *                                                                                    character that is neither
     *                                                                                    a digit nor a separator
     *                                                                                    nor a leading sign
     * @throws io.github.lembergmax.justmath.exception.DuplicateDecimalSeparatorException if the decimal separator
     *                                                                                    occurs more than once
     */
    public BigNumber(
            final String number,
            final Locale locale
    ) {
        this(bigNumberParser.parse(number, locale));
    }

    /**
     * Creates a copy of a number.
     *
     * @param bigNumber the number to copy
     */
    public BigNumber(
            final BigNumber bigNumber
    ) {
        this.beforeDecimal = bigNumber.beforeDecimal;
        this.afterDecimal = bigNumber.afterDecimal;
        this.isNegative = bigNumber.isNegative;
    }

    /**
     * Creates a number from the result of the parser.
     *
     * @param parseResult the parsed parts of the number
     */
    private BigNumber(
            final ParseResult parseResult
    ) {
        this(
                parseResult.beforeDecimals(),
                parseResult.afterDecimals(),
                parseResult.isNegative()
        );
    }

    /**
     * Creates a number from its parts without any check.
     *
     * @param beforeDecimal the digits before the decimal separator
     * @param afterDecimal  the digits after the decimal separator
     * @param isNegative    whether the number has a minus sign
     */
    private BigNumber(
            final String beforeDecimal,
            final String afterDecimal,
            final boolean isNegative
    ) {
        this.beforeDecimal = beforeDecimal;
        this.afterDecimal = afterDecimal;
        this.isNegative = isNegative;
    }

    /**
     * Adds a number to this number.
     *
     * @param otherAddend the number to add
     * @return the sum, this number is not changed
     * @see BasicMath#add(BigNumber, BigNumber)
     */
    public BigNumber add(
            final BigNumber otherAddend
    ) {
        return BasicMath.add(this, otherAddend);
    }

    /**
     * Subtracts a number from this number.
     *
     * @param subtrahend the number to subtract
     * @return the difference, this number is not changed
     * @see BasicMath#subtract(BigNumber, BigNumber)
     */
    public BigNumber subtract(
            final BigNumber subtrahend
    ) {
        return BasicMath.subtract(this, subtrahend);
    }

    /**
     * Multiplies this number by another number.
     *
     * @param otherFactor the number to multiply by
     * @return the product, this number is not changed
     * @see BasicMath#multiply(BigNumber, BigNumber)
     */
    public BigNumber multiply(
            final BigNumber otherFactor
    ) {
        return BasicMath.multiply(this, otherFactor);
    }

    /**
     * Divides this number by another number. If the result never ends (for example 1 / 3), it is rounded
     * with {@link BigNumbers#DEFAULT_MATH_CONTEXT}.
     *
     * @param divisor the number to divide by
     * @return the quotient, this number is not changed
     * @throws io.github.lembergmax.justmath.exception.DivisionByZeroException if the divisor is zero
     * @see BasicMath#divide(BigNumber, BigNumber)
     */
    public BigNumber divide(
            final BigNumber divisor
    ) {
        return BasicMath.divide(this, divisor);
    }

    /**
     * Divides this number by another number and rounds the result with the given math context.
     *
     * @param divisor     the number to divide by
     * @param mathContext the precision (number of significant digits) and the rounding mode of the result,
     *                    a precision of 0 means no rounding
     * @return the quotient, this number is not changed
     * @throws io.github.lembergmax.justmath.exception.DivisionByZeroException if the divisor is zero
     * @see BasicMath#divide(BigNumber, BigNumber, MathContext)
     */
    public BigNumber divide(
            final BigNumber divisor,
            final MathContext mathContext
    ) {
        return BasicMath.divide(this, divisor, mathContext);
    }

    /**
     * Checks whether this number is greater than another number. Equal numbers are not greater.
     * Numbers written in different locales are compared by their value.
     *
     * @param other the number to compare with
     * @return true if this number is greater than the other number
     */
    public boolean isGreaterThan(
            final BigNumber other
    ) {
        return BasicMath.isGreaterThan(this, other);
    }

    /**
     * Checks whether this number has the same value as another number. Trailing and leading zeros
     * and the locale a number was written in do not matter.
     *
     * @param other the number to compare with
     * @return true if this number is neither greater nor smaller than the other number
     */
    public boolean isEqualTo(
            final BigNumber other
    ) {
        return BasicMath.isEqualTo(this, other);
    }

    /**
     * Checks whether this number is smaller than another number. Equal numbers are not smaller.
     *
     * @param other the number to compare with
     * @return true if this number is smaller than the other number
     */
    public boolean isSmallerThan(
            final BigNumber other
    ) {
        return BasicMath.isSmallerThan(this, other);
    }

    /**
     * Returns the absolute value of this number.
     *
     * @return a copy of this number without a minus sign, this number is not changed
     */
    public BigNumber abs() {
        BigNumber bigNumber = new BigNumber(this);
        bigNumber.isNegative = false;
        return bigNumber;
    }

    /**
     * Swaps the sign of this number. This is also done for 0, so the result is a 0 with a minus
     * that prints as "0" but is smaller than a 0 without a minus.
     *
     * @return a copy of this number with the opposite sign, this number is not changed
     */
    public BigNumber negate() {
        BigNumber bigNumber = new BigNumber(this);
        bigNumber.isNegative = !this.isNegative;
        return bigNumber;
    }

    /**
     * Returns the number in the {@link #INTERNAL_LOCALE}, so with "." as the decimal separator.
     * A number without a fractional part has no decimal separator, and any zero is "0".
     *
     * @return the number as text, for example "-1234.5"
     */
    @Override
    public String toString() {
        return toString(INTERNAL_LOCALE);
    }

    /**
     * Returns the number written in the given locale. A number without a fractional part has no decimal
     * separator, and any zero is "0" without a sign. There are no grouping separators.
     *
     * @param locale the locale that defines the decimal separator
     * @return the number as text, for example "-1234,5" for {@link Locale#GERMAN}
     */
    public String toString(
            final Locale locale
    ) {
        if (beforeDecimal == null || beforeDecimal.isBlank()) {
            beforeDecimal = BigNumberParser.ZERO_AS_STRING;
        }
        if (afterDecimal == null || afterDecimal.isBlank()) {
            afterDecimal = BigNumberParser.ZERO_AS_STRING;
        }

        if (beforeDecimal.matches(BigNumberParser.ONLY_CONTAINS_ZEROS_REGEX) &&
                afterDecimal.matches(BigNumberParser.ONLY_CONTAINS_ZEROS_REGEX)
        ) {
            return BigNumberParser.ZERO_AS_STRING;
        }

        final StringBuilder result = new StringBuilder(
                (this.isNegative ? BigNumberParser.NEGATIVE_SIGN : "") + beforeDecimal);
        if (!BigNumberParser.ZERO_AS_STRING.equals(afterDecimal)) {
            result.append(NumberFormatLookup.getDecimalSeparatorForLocale(locale));
            result.append(afterDecimal);
        }

        return result.toString();
    }

    /**
     * Checks whether another object is a number with the same text in the {@link #INTERNAL_LOCALE}.
     * The locale a number was written in does not matter, and a 0 with a minus equals a 0 without.
     *
     * @param o the object to compare with
     * @return true if the object is a {@link BigNumber} that prints the same text
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof BigNumber && this.toString().equals(o.toString());
    }

    /**
     * Returns a hash code that fits {@link #equals(Object)}: numbers with the same text get the same hash code.
     *
     * @return the hash code of the text of this number
     */
    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

}

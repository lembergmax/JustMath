package io.github.lembergmax.justmath.bignumber;

import io.github.lembergmax.justmath.bignumber.math.MathTools;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.ParseResult;
import lombok.Getter;

import java.util.Locale;

public class BigNumber {

    private static final BigNumberParser bigNumberParser = new BigNumberParser();

    @Getter
    private String beforeDecimal;
    @Getter
    private String afterDecimal;
    @Getter
    private boolean isNegative;
    @Getter
    private Locale locale;

    public BigNumber(
            final String number,
            final Locale locale
    ) {
        this(bigNumberParser.parse(number, locale));
    }

    public BigNumber(
            final BigNumber bigNumber
    ) {
        this.beforeDecimal = bigNumber.beforeDecimal;
        this.afterDecimal = bigNumber.afterDecimal;
        this.isNegative = bigNumber.isNegative;
        this.locale = bigNumber.locale;
    }

    private BigNumber(
            final ParseResult parseResult
    ) {
        this(
                parseResult.beforeDecimals(),
                parseResult.afterDecimals(),
                parseResult.isNegative(),
                parseResult.locale()
        );
    }

    public BigNumber(
            final String beforeDecimal,
            final String afterDecimal,
            final boolean isNegative,
            final Locale locale
    ) {
        this.beforeDecimal = beforeDecimal;
        this.afterDecimal = afterDecimal;
        this.isNegative = isNegative;
        this.locale = locale;
    }

    public boolean isGreaterThan(
            final BigNumber other
    ) {
        final BigNumber thisNumber = new BigNumber(this);
        final BigNumber otherNumber = new BigNumber(other);

        final String decimalSeparatorOfThisNumber = NumberFormatLookup.getDecimalSeparatorForLocale(thisNumber.getLocale());
        final String cleanedThisNumber = MathTools.makeNumbersSameLength(thisNumber, otherNumber);
        final String cleanedOtherNumber = MathTools.makeNumbersSameLength(otherNumber, thisNumber);

        // wenn verschiedene vorzeichen
        if (this.isNegative() != other.isNegative()) {
            // wenn die aktuelle positiv ist, dann ist diese größer
            return !this.isNegative();
        }

        // gleiche Zahlen
        if (cleanedThisNumber.equals(cleanedOtherNumber)) {
            return false;
        }

        for (int i = 0; i <= cleanedThisNumber.length() - 1; i++) {
            final String currentCharacterOfThisNumber = String.valueOf(cleanedThisNumber.charAt(i));
            if (decimalSeparatorOfThisNumber.equals(currentCharacterOfThisNumber)) {
                continue;
            }

            final int currentDigitOfThisNumber = MathTools.charAsInt(cleanedThisNumber.charAt(i));
            final int currentDigitOfOtherNumber = MathTools.charAsInt(cleanedOtherNumber.charAt(i));

            // wenn die zahlen unterschiedlich sind
            if (currentDigitOfThisNumber != currentDigitOfOtherNumber) {
                // bei negativen Zahlen ist der Betrag umgekehrt: die kleinere Ziffer bedeutet die größere Zahl
                return this.isNegative() == (currentDigitOfThisNumber < currentDigitOfOtherNumber);
            }
        }

        return false;
    }

    public boolean isSmallerThan(
            final BigNumber other
    ) {
        return other.isGreaterThan(this);
    }

    public String toString() {
        if (beforeDecimal == null || beforeDecimal.isBlank()) {
            beforeDecimal = BigNumberParser.ZERO_AS_STRING;
        }
        if (afterDecimal == null || afterDecimal.isBlank()) {
            afterDecimal = BigNumberParser.ZERO_AS_STRING;
        }

        if (beforeDecimal.matches(BigNumberParser.ONLY_CONTAINS_ZEROS_REGEX) && afterDecimal.matches(BigNumberParser.ONLY_CONTAINS_ZEROS_REGEX)
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

    public BigNumber abs() {
        BigNumber bigNumber = new BigNumber(this);
        bigNumber.isNegative = false;
        return bigNumber;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BigNumber && this.toString().equals(o.toString());
    }

}

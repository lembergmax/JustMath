package io.github.lembergmax.justmath.bignumber;

import io.github.lembergmax.justmath.bignumber.math.MathTools;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.ParseResult;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

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

    public BigNumber(BigNumber bigNumber) {
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

    private BigNumber(
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

    public boolean greaterThan(
            final BigNumber other
    ) {
        BigNumber thisNumber = new BigNumber(this);
        BigNumber otherNumber = new BigNumber(other);

        String number1 = MathTools.makeNumbersSameLength(thisNumber, otherNumber);
        String number2 = MathTools.makeNumbersSameLength(otherNumber, thisNumber);

        for (int i = 0; i <= number1.length() - 1; i++) {
            if (thisNumber.isNegative && otherNumber.isNegative && !thisNumber.equals(otherNumber)) {
                if ("0".equals(thisNumber.toString())) {
                    return new BigNumber(thisNumber.toString(), thisNumber.locale).smallerThan(otherNumber);
                }
                return !new BigNumber(thisNumber.toString(), thisNumber.locale).smallerThan(otherNumber);
            }

            if (number1.charAt(i) != number2.charAt(i)) {
                return Integer.parseInt(String.valueOf(number1.charAt(i))) > Integer.parseInt(String.valueOf(number2.charAt(i)));
            }
        }
        return false;
    }

    public boolean smallerThan(
            final BigNumber other
    ) {
        BigNumber thisNumber = new BigNumber(this);
        BigNumber otherNumber = new BigNumber(other);

        String number1 = MathTools.makeNumbersSameLength(thisNumber, otherNumber);
        String number2 = MathTools.makeNumbersSameLength(otherNumber, thisNumber);

        for (int i = 0; i <= number1.length() - 1; i++) {
            if (thisNumber.isNegative && otherNumber.isNegative) {
                return new BigNumber(number1, thisNumber.locale).greaterThan(new BigNumber(number2, thisNumber.locale));
            }

            if (number1.charAt(i) != number2.charAt(i)) {
                return Integer.parseInt(String.valueOf(number1.charAt(i))) < Integer.parseInt(String.valueOf(number2.charAt(i)));
            }
        }
        return false;
    }

    public String toString() {
        if (beforeDecimal == null || beforeDecimal.isBlank()) {
            beforeDecimal = BigNumberParser.ZERO_AS_STRING;
        }
        if (afterDecimal == null || afterDecimal.isBlank()) {
            afterDecimal = BigNumberParser.ZERO_AS_STRING;
        }

        final String onlyContainsZerosRegex = "^[" + BigNumberParser.ZERO_AS_STRING + "]+$";
        if (beforeDecimal.matches(onlyContainsZerosRegex) && afterDecimal.matches(onlyContainsZerosRegex)
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

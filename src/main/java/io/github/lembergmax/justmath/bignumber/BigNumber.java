package io.github.lembergmax.justmath.bignumber;

import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.ParseResult;
import lombok.Getter;

import java.math.BigInteger;
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

    public String toString() {
        if (beforeDecimal == null || beforeDecimal.isBlank()) {
            beforeDecimal = "0";
        }
        if (afterDecimal == null || afterDecimal.isBlank()) {
            afterDecimal = "0";
        }

        final String onlyContainsZerosRegex = "^[0]+$";
        if (("0".equals(beforeDecimal) && "0".equals(afterDecimal)) ||
                (beforeDecimal.matches(onlyContainsZerosRegex) && afterDecimal.matches(onlyContainsZerosRegex))
        ) {
            this.isNegative = false;
            return "0";
        }

        final StringBuilder result = new StringBuilder(
                (this.isNegative ? BigNumberParser.NEGATIVE_SIGN : "") + beforeDecimal);
        if (!"0".equals(afterDecimal)) {
            result.append(NumberFormatLookup.getDecimalSeparatorForLocale(locale));
            result.append(afterDecimal);
        }

        return result.toString();
    }

}

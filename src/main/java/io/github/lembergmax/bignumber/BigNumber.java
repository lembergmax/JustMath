package io.github.lembergmax.bignumber;

import io.github.lembergmax.core.BigNumberParser;
import io.github.lembergmax.core.NumberFormatLookup;
import io.github.lembergmax.core.ParseResult;
import io.github.lembergmax.exception.DuplicateDecimalSeparatorException;

import java.util.Locale;

public class BigNumber {

    private static final BigNumberParser bigNumberParser = new BigNumberParser();

    private String beforeDecimal;
    private String afterDecimal;
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
        this(parseResult.beforeDecimals(), parseResult.afterDecimals(), parseResult.locale());
    }

    private BigNumber(
            final String beforeDecimal,
            final String afterDecimal,
            final Locale locale
    ) {
        this.beforeDecimal = beforeDecimal;
        this.afterDecimal = afterDecimal;
        this.locale = locale;
    }

    public String toString() {
        if(beforeDecimal == null || beforeDecimal.isBlank()) {
            beforeDecimal = "0";
        }
        if(afterDecimal == null || afterDecimal.isBlank()) {
            afterDecimal = "0";
        }

        final StringBuilder result = new StringBuilder(beforeDecimal);
        if(!"0".equals(afterDecimal)) {
            result.append(NumberFormatLookup.getDecimalSeparatorForLocale(locale));
            result.append(afterDecimal);
        }

        return result.toString();
    }

}

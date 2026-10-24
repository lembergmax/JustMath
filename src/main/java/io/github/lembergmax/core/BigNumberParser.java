package io.github.lembergmax.core;

import java.util.Locale;

public class BigNumberParser {

    public ParseResult parse(
            final String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        final String groupingSeparator = NumberFormatLookup.getGroupingSeparatorForLocale(locale);

        final String[] splitNumber = number.split(decimalSeparator);

        final String beforeDecimal = splitNumber[0].replace(groupingSeparator, "");
        final String afterDecimal;
        if (splitNumber.length == 2) {
            afterDecimal = splitNumber[1];
        } else {
            afterDecimal = "0";
        }

        return new ParseResult(beforeDecimal, afterDecimal, locale);
    }

}

package io.github.lembergmax.core;

import io.github.lembergmax.exception.NotAValidCharacterException;

import java.util.Locale;

public class BigNumberParser {

    public ParseResult parse(
            String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        final String groupingSeparator = NumberFormatLookup.getGroupingSeparatorForLocale(locale);

        number = cleanupInputString(number, groupingSeparator, decimalSeparator);

        final String[] splitNumber = number.split(decimalSeparator);

        final String beforeDecimal = splitNumber[0];
        final String afterDecimal;
        if (splitNumber.length == 2) {
            afterDecimal = splitNumber[1];
        } else {
            afterDecimal = "0";
        }

        return new ParseResult(beforeDecimal, afterDecimal, locale);
    }

    public String cleanupInputString(
            final String number,
            final String groupingSeparator,
            final String decimalSeparator
    ) {
        for(int i = 0; i < number.length(); i++) {
            final String charAsString = String.valueOf(number.charAt(i));
            if(
                    !Character.isDigit(number.charAt(i)) &&
                    !groupingSeparator.equalsIgnoreCase(charAsString) &&
                    !decimalSeparator.equalsIgnoreCase(charAsString)
            ) {
                throw new NotAValidCharacterException("'" + charAsString + "'" + "is not a valid character.");
            }
        }

        String cleanedNumber;
        cleanedNumber = number.replace(groupingSeparator, "");

        return cleanedNumber;
    }

}

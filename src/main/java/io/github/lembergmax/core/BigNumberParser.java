package io.github.lembergmax.core;

import io.github.lembergmax.exception.NotAValidCharacterException;

import java.util.Locale;

public class BigNumberParser {

    public ParseResult parse(
            final String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        final String groupingSeparator = NumberFormatLookup.getGroupingSeparatorForLocale(locale);

        String cleanedNumber = removeGroupingSeparators(number, groupingSeparator, decimalSeparator);

        return cleanedNumber.contains(decimalSeparator) ?
                parseWithDecimalSeparator(cleanedNumber, decimalSeparator, locale) :
                parseWithoutDecimalSeparator(cleanedNumber, locale);
    }

    private ParseResult parseWithoutDecimalSeparator(
            final String number,
            final Locale locale
    ) {
        return new ParseResult(number, "0", locale);
    }

    private ParseResult parseWithDecimalSeparator(
            final String number,
            final String decimalSeparator,
            final Locale locale
    ) {
        final int indexOfDecimalSeparator = decimalSeparator.indexOf(decimalSeparator);

        final String beforeDecimal = number.substring(0, indexOfDecimalSeparator);
        final String afterDecimal = number.substring(indexOfDecimalSeparator + 1);

        return new ParseResult(beforeDecimal, afterDecimal, locale);
    }

    private String removeGroupingSeparators(
            final String number,
            final String groupingSeparator,
            final String decimalSeparator
    ) {
        checkForNotValidCharacters(number, groupingSeparator, decimalSeparator);

        String cleanedNumber;
        cleanedNumber = number.replace(groupingSeparator, "");

        return cleanedNumber;
    }

    private void checkForNotValidCharacters(String number, String groupingSeparator, String decimalSeparator) {
        for (int i = 0; i < number.length(); i++) {
            final String charAsString = String.valueOf(number.charAt(i));
            if (
                    !Character.isDigit(number.charAt(i)) &&
                            !groupingSeparator.equalsIgnoreCase(charAsString) &&
                            !decimalSeparator.equalsIgnoreCase(charAsString)
            ) {
                throw new NotAValidCharacterException("'" + charAsString + "'" + "is not a valid character.");
            }
        }
    }

}

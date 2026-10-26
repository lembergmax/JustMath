package io.github.lembergmax.core;

import io.github.lembergmax.exception.DuplicateDecimalSeparatorException;
import io.github.lembergmax.exception.NotAValidCharacterException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class BigNumberParser {

    public ParseResult parse(
            final String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        final String groupingSeparator = NumberFormatLookup.getGroupingSeparatorForLocale(locale);

        String cleanedNumber = cleanupNumberInputString(number, groupingSeparator, decimalSeparator);

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
        final int indexOfDecimalSeparator = number.indexOf(decimalSeparator);

        final String beforeDecimal = number.substring(0, indexOfDecimalSeparator);
        final String afterDecimal = number.substring(indexOfDecimalSeparator + 1);

        return new ParseResult(beforeDecimal, afterDecimal, locale);
    }

    private String cleanupNumberInputString(
            final String number,
            final String groupingSeparator,
            final String decimalSeparator
    ) {
        checkForNotValidCharacters(number, groupingSeparator, decimalSeparator);

        String cleanedNumber;
        cleanedNumber = number.replace(groupingSeparator, "");
        cleanedNumber = trimLeadingAndTrailingZeros(cleanedNumber);

        return cleanedNumber;
    }

    private String trimLeadingAndTrailingZeros(
            final String number
    ) {
        return number.replaceAll("^0+|0+$", "");
    }

    private void checkForNotValidCharacters(
            final String number,
            final String groupingSeparator,
            final String decimalSeparator
    ) {
        // Alle Zeichen welche nicht eine Zahl und der Dezimaltrenner und Gruppentrenner sind
        for (int i = 0; i < number.length(); i++) {
            final String charAsString = String.valueOf(number.charAt(i));
            if (
                    !Character.isDigit(number.charAt(i)) &&
                    !groupingSeparator.equalsIgnoreCase(charAsString) &&
                    !decimalSeparator.equalsIgnoreCase(charAsString)
            ) {
                throw new NotAValidCharacterException(
                        "'" + charAsString + "'" + "is not a valid character."
                );
            }
        }

        // Wenn der Dezimaltrenner mehr als einmal vorkommt
        if(StringUtils.countMatches(number, decimalSeparator) > 1) {
            throw new DuplicateDecimalSeparatorException(
                    "The decimal separator '" + decimalSeparator + "' occurrence more than once."
            );
        }
    }

}

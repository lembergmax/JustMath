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
        final String cleanedNumber;

        final ParseResult parseResultToCleanNumber;
        if (number.contains(decimalSeparator)) {
            parseResultToCleanNumber = parseWithDecimalSeparator(number, decimalSeparator, locale);
        } else {
            parseResultToCleanNumber = parseWithoutDecimalSeparator(number, locale);
        }
        cleanedNumber = cleanupNumberInputString(
                parseResultToCleanNumber.beforeDecimals(),
                parseResultToCleanNumber.afterDecimals(),
                locale
        );

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
            final String beforeDecimals,
            final String afterDecimals,
            final Locale locale
    ) {
        final String groupingSeparator = NumberFormatLookup.getGroupingSeparatorForLocale(locale);
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);

        checkForNotValidCharacters(
                beforeDecimals + decimalSeparator + afterDecimals,
                groupingSeparator, decimalSeparator);

        String cleanedBeforeDecimals = removeAllSpaces(trimLeadingZeros(beforeDecimals));
        String cleanedAfterDecimals = removeAllSpaces(trimTrailingZeros(afterDecimals));

        if (cleanedBeforeDecimals.isBlank()) {
            cleanedBeforeDecimals = "0";
        }
        if (cleanedAfterDecimals.isBlank()) {
            cleanedAfterDecimals = "0";
        }

        return (cleanedBeforeDecimals + decimalSeparator + cleanedAfterDecimals).replace(groupingSeparator, "");
    }

    private String trimLeadingZeros(
            final String number
    ) {
        return number.replaceAll("^0+", "");
    }

    private String trimTrailingZeros(
            final String number
    ) {
        return number.replaceAll("0+$", "");
    }

    private String removeAllSpaces(
            final String string
    ) {
        return string.replace(" ", "");
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
                    !decimalSeparator.equalsIgnoreCase(charAsString) &&
                    !"-".equals(charAsString)
            ) {
                throw new NotAValidCharacterException(
                        "'" + charAsString + "'" + " is not a valid character."
                );
            }
        }

        // Wenn der Dezimaltrenner mehr als einmal vorkommt
        if (StringUtils.countMatches(number, decimalSeparator) > 1) {
            throw new DuplicateDecimalSeparatorException(
                    "The decimal separator '" + decimalSeparator + "' occurrence more than once."
            );
        }
    }

}

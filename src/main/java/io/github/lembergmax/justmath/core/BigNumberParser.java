package io.github.lembergmax.justmath.core;

import io.github.lembergmax.justmath.exception.DuplicateDecimalSeparatorException;
import io.github.lembergmax.justmath.exception.NotAValidCharacterException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class BigNumberParser {

    public static final String NEGATIVE_SIGN = "-";
    public static final String POSITIVE_SIGN = "+";
    private final String ZERO_AS_STRING = "0";

    public ParseResult parse(
            final String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        final String cleanedNumber;

        final boolean isNegative = number.startsWith(NEGATIVE_SIGN) && !number.startsWith(POSITIVE_SIGN);

        final ParseResult parseResultToCleanNumber;
        if (number.contains(decimalSeparator)) {
            parseResultToCleanNumber = parseWithDecimalSeparator(number, isNegative, decimalSeparator, locale);
        } else {
            parseResultToCleanNumber = parseWithoutDecimalSeparator(number, isNegative, locale);
        }
        cleanedNumber = cleanupNumberInputString(
                parseResultToCleanNumber.beforeDecimals(),
                parseResultToCleanNumber.afterDecimals(),
                locale
        );

        return cleanedNumber.contains(decimalSeparator) ?
                parseWithDecimalSeparator(cleanedNumber, isNegative, decimalSeparator, locale) :
                parseWithoutDecimalSeparator(cleanedNumber, isNegative, locale);
    }

    private ParseResult parseWithoutDecimalSeparator(
            final String number,
            final boolean isNegative,
            final Locale locale
    ) {
        return new ParseResult(number, ZERO_AS_STRING, isNegative, locale);
    }

    private ParseResult parseWithDecimalSeparator(
            final String number,
            final boolean isNegative,
            final String decimalSeparator,
            final Locale locale
    ) {
        final int indexOfDecimalSeparator = number.indexOf(decimalSeparator);

        final String beforeDecimal = number.substring(0, indexOfDecimalSeparator);
        final String afterDecimal = number.substring(indexOfDecimalSeparator + 1);

        return new ParseResult(beforeDecimal, afterDecimal, isNegative, locale);
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

        String cleanedBeforeDecimals = removePositiveOrNegativeSign(removeAllSpaces(trimLeadingZeros(beforeDecimals)));
        String cleanedAfterDecimals = removeAllSpaces(trimTrailingZeros(afterDecimals));

        if (cleanedBeforeDecimals.isBlank()) {
            cleanedBeforeDecimals = ZERO_AS_STRING;
        }
        if (cleanedAfterDecimals.isBlank()) {
            cleanedAfterDecimals = ZERO_AS_STRING;
        }

        return (cleanedBeforeDecimals + decimalSeparator + cleanedAfterDecimals).replace(groupingSeparator, "");
    }

    private String removePositiveOrNegativeSign(
            final String number
    ) {
        return number.replace(POSITIVE_SIGN, "").replace(NEGATIVE_SIGN, "");
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
                            !NEGATIVE_SIGN.equals(charAsString) &&
                            !POSITIVE_SIGN.equals(charAsString)
            ) {
                throw new NotAValidCharacterException(charAsString);
            }
        }

        // Wenn der Dezimaltrenner mehr als einmal vorkommt
        if (StringUtils.countMatches(number, decimalSeparator) > 1) {
            throw new DuplicateDecimalSeparatorException(decimalSeparator);
        }
    }

}

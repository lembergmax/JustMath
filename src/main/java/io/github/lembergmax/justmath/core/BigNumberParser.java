package io.github.lembergmax.justmath.core;

import io.github.lembergmax.justmath.exception.DuplicateDecimalSeparatorException;
import io.github.lembergmax.justmath.exception.NotAValidCharacterException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class BigNumberParser {

    public static final String NEGATIVE_SIGN = "-";
    public static final String POSITIVE_SIGN = "+";
    public static final String ZERO_AS_STRING = "0";
    public static final String ONLY_CONTAINS_ZEROS_REGEX = "^[" + ZERO_AS_STRING + "]+$";

    /**
     * Parses a number from text. A sign is only allowed as the very first character. Grouping separators
     * and leading and trailing zeros are removed, and an empty text is 0. A zero never gets a minus.
     *
     * @param number the number as text
     * @param locale the locale the number is written in, it defines the decimal and grouping separator
     * @return the sign, the digits before and the digits after the decimal separator
     * @throws NotAValidCharacterException        if the text contains a character that is neither a digit,
     *                                            a separator nor a leading sign
     * @throws DuplicateDecimalSeparatorException if the decimal separator occurs more than once
     */
    public ParseResult parse(
            final String number,
            final Locale locale
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(locale);
        checkForMisplacedSigns(number);
        final String cleanedNumber;

        final boolean isNegative = number.startsWith(NEGATIVE_SIGN) && !number.startsWith(POSITIVE_SIGN);

        final ParseResult parseResultToCleanNumber;
        if (number.contains(decimalSeparator)) {
            parseResultToCleanNumber = parseWithDecimalSeparator(number, isNegative, decimalSeparator);
        } else {
            parseResultToCleanNumber = parseWithoutDecimalSeparator(number, isNegative);
        }
        cleanedNumber = cleanupNumberInputString(
                parseResultToCleanNumber.beforeDecimals(),
                parseResultToCleanNumber.afterDecimals(),
                locale
        );

        return cleanedNumber.contains(decimalSeparator) ?
                parseWithDecimalSeparator(cleanedNumber, isNegative, decimalSeparator) :
                parseWithoutDecimalSeparator(cleanedNumber, isNegative);
    }

    private ParseResult parseWithoutDecimalSeparator(
            final String number,
            final boolean isNegative
    ) {
        return new ParseResult(number, ZERO_AS_STRING, isNegative);
    }

    private ParseResult parseWithDecimalSeparator(
            final String number,
            boolean isNegative,
            final String decimalSeparator
    ) {
        final int indexOfDecimalSeparator = number.indexOf(decimalSeparator);

        final String beforeDecimal = number.substring(0, indexOfDecimalSeparator);
        final String afterDecimal = number.substring(indexOfDecimalSeparator + 1);

        if (isOnlyZeros(beforeDecimal) && isOnlyZeros(afterDecimal)) {
            isNegative = false;
        }

        return new ParseResult(beforeDecimal, afterDecimal, isNegative);
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

        String cleanedBeforeDecimals = trimLeadingZeros(removePositiveOrNegativeSign(removeAllSpaces(trimLeadingZeros(beforeDecimals))));
        String cleanedAfterDecimals = trimTrailingZeros(removeAllSpaces(trimTrailingZeros(afterDecimals)));

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

    private void checkForMisplacedSigns(
            final String number
    ) {
        for (int i = 1; i < number.length(); i++) {
            final String charAsString = String.valueOf(number.charAt(i));
            if (NEGATIVE_SIGN.equals(charAsString) || POSITIVE_SIGN.equals(charAsString)) {
                throw new NotAValidCharacterException(charAsString);
            }
        }
    }

    private void checkForNotValidCharacters(
            final String number,
            final String groupingSeparator,
            final String decimalSeparator
    ) {
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

    private boolean isOnlyZeros(
            final String number
    ) {
        return number.matches(ONLY_CONTAINS_ZEROS_REGEX);
    }

}

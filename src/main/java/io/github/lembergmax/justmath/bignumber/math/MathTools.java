package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import org.apache.commons.lang3.StringUtils;

public class MathTools {

    /**
     * Aligns the digit length of a number with another number by padding it with zeros
     * on the left of the integer part and on the right of the fractional part as needed.
     *
     * @param numberToChangeLength the BigNumber whose length should be adjusted to match the reference value
     * @param numberToCompareTo    the BigNumber defining the target digit lengths for comparison
     * @return a string representation of {@code numberToChangeLength} padded so its integer and
     * fractional parts match the length of {@code numberToCompareTo}
     */
    public static String makeNumbersSameLength(
            final BigNumber numberToChangeLength,
            final BigNumber numberToCompareTo
    ) {
        final int zerosToAddToAfterDecimals =
                numberToCompareTo.getAfterDecimal().length() -
                        numberToChangeLength.getAfterDecimal().length();

        final int zerosToAddToBeforeDecimals =
                numberToCompareTo.getBeforeDecimal().length() -
                        numberToChangeLength.getBeforeDecimal().length();

        return getZeroNTimes(zerosToAddToBeforeDecimals) + numberToChangeLength.getBeforeDecimal() +
                NumberFormatLookup.getDecimalSeparatorForLocale(numberToChangeLength.getLocale()) +
                numberToChangeLength.getAfterDecimal() + getZeroNTimes(zerosToAddToAfterDecimals);
    }

    /**
     * Returns a "0" n times in a string.
     *
     * @param nTimes the number of times a "0" should be returned.
     * @return the string with the zeros.
     */
    private static String getZeroNTimes(
            final int nTimes
    ) {
        return StringUtils.repeat("0", nTimes);
    }

    /**
     * Calculates the sign of the result for an addition.
     *
     * @param isFirstAddendNegative whether the first addend is positive, given that the addends are sorted by size (ignoring their sign)
     * @return the sign "+" or "-"
     */
    public static String signForAdditionResult(
            final boolean isFirstAddendNegative
    ) {
        return isFirstAddendNegative ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN;
    }

    /**
     * Puts the larger number at the beginning of the returned array.
     * The absolute value of both numbers is compared here.
     *
     * @param addend1 a BigNumber
     * @param addend2 another BigNumber
     * @return an array with the larger BigNumber at the beginning.
     */
    public static BigNumber[] sortBySizeIgnoreSigns(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        return addend1.abs().isGreaterThan(addend2.abs()) ?
                new BigNumber[]{new BigNumber(addend1), new BigNumber(addend2)} :
                new BigNumber[]{new BigNumber(addend2), new BigNumber(addend1)};
    }

    /**
     * Converts a char into an int.
     *
     * @param character the char to be converted
     * @return the converted char as an int
     */
    public static int charAsInt(
            final char character
    ) {
        return Integer.parseInt(String.valueOf(character));
    }

    /**
     * Returns the sign of a number as a string representation.
     *
     * @param minuend the BigNumber to inspect
     * @return "-" if the value is negative, otherwise "+"
     */
    public static String getSignAsString(final BigNumber minuend) {
        return minuend.isNegative() ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN;
    }

}

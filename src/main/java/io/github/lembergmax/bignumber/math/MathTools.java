package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.core.NumberFormatLookup;
import org.apache.commons.lang3.StringUtils;

public class MathTools {

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

    private static String getZeroNTimes(
            final int nTimes
    ) {
        return StringUtils.repeat("0", nTimes);
    }
}

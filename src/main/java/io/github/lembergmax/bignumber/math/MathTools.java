package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.core.NumberFormatLookup;
import org.apache.commons.lang3.StringUtils;

public class MathTools {

    public static BigNumber makeNumbersSameLength(
            final BigNumber numberToChangeLength,
            final BigNumber numberToCompareTo
    ) {
        final int neededAfterDecimalLength = numberToCompareTo.getAfterDecimal().length();
        final int actualAfterDecimalLength = numberToChangeLength.getAfterDecimal().length();
        final int zerosToAddToAfterDecimals = neededAfterDecimalLength - actualAfterDecimalLength;

        final String newAfterDecimal = numberToChangeLength.getAfterDecimal() + StringUtils.repeat("0", zerosToAddToAfterDecimals);

        final int neededBeforeDecimalLength = numberToCompareTo.getBeforeDecimal().length();
        final int actualBeforeDecimalLength = numberToChangeLength.getBeforeDecimal().length();
        final int zerosToAddToBeforeDecimals = neededBeforeDecimalLength - actualBeforeDecimalLength;

        final String newBeforeDecimal = numberToChangeLength.getBeforeDecimal() + StringUtils.repeat("0", zerosToAddToBeforeDecimals);

        return new BigNumber(
                newBeforeDecimal +
                NumberFormatLookup.getDecimalSeparatorForLocale(numberToChangeLength.getLocale()) +
                newAfterDecimal,
                numberToChangeLength.getLocale()
        );
    }

}

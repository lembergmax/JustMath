package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;

import java.util.Locale;

public class BasicMath {

    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        final boolean isAddend1Negative = addend1.isNegative();
        final boolean isAddend2Negative = addend2.isNegative();
        final boolean isSubtraction = isAddend1Negative != isAddend2Negative;

        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        String cleanedAddend1 = MathTools.makeNumbersSameLength(addend1, addend2);
        String cleanedAddend2 = MathTools.makeNumbersSameLength(addend2, addend1);

        boolean isResultNegative = isAddend1Negative;

        if (isSubtraction && cleanedAddend1.compareTo(cleanedAddend2) < 0) {
            final String tempAddend1 = cleanedAddend1;
            cleanedAddend1 = cleanedAddend2;
            cleanedAddend2 = tempAddend1;
            isResultNegative = isAddend2Negative;
        }

        final StringBuilder result = new StringBuilder();

        int carry = isSubtraction ? 1 : 0;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            if (decimalSeparator.equals(String.valueOf(cleanedAddend1.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            final int addend1CharAtIndex = Integer.parseInt(String.valueOf(cleanedAddend1.charAt(i)));
            final int addend2CharAtIndex = Integer.parseInt(String.valueOf(cleanedAddend2.charAt(i)));

            final int resultForCurrentDigit =
                    addend1CharAtIndex + (isSubtraction ? 9 - addend2CharAtIndex : addend2CharAtIndex) + carry;
            carry = resultForCurrentDigit / 10;
            result.append(resultForCurrentDigit % 10);
        }

        if (!isSubtraction && carry > 0) {
            result.append(carry);
        }

        final String resultAsString = result.reverse().toString();
        return new BigNumber(
                (isResultNegative ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN) +
                        resultAsString, addend1.getLocale()
        );
    }

    public static BigNumber subtract(
            final BigNumber minuend,
            final BigNumber subtrahend
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(minuend.getLocale());
        final String minuendSign = getSignAsString(minuend);
        final String subtrahendSign = getSignAsString(subtrahend);
        final String minuendAsString = MathTools.makeNumbersSameLength(minuend, subtrahend);
        final String subtrahendAsString = MathTools.makeNumbersSameLength(subtrahend, minuend);

        final StringBuilder result = new StringBuilder();

        int carry = 0;
        int currentMinuend = 0;
        int currentSubtrahend = 0;
        int resultForCurrentDigit = 0;

        // Loop rückwärts über alle Zeichen und subtrahieren, Rest immer merken und auf nächste Ziffer projezieren
        for (int i = minuendAsString.length() - 1; i >= 0; i--) {
            // Wenn Aktuelles Zeichen von Subtrahend1 ist gleich decimalSeperator, dann füge decimalSeperator an das result an.
            if (decimalSeparator.equals(String.valueOf(minuendAsString.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            currentMinuend = Integer.parseInt(String.valueOf(minuendAsString.charAt(i)));
            currentSubtrahend = Integer.parseInt(String.valueOf(subtrahendAsString.charAt(i)));
            resultForCurrentDigit = currentMinuend - currentSubtrahend - carry;

            if (i == 0 && carry == 0) {
                resultForCurrentDigit = 0;
            }

            if (resultForCurrentDigit < 0) {
                carry = (currentMinuend - currentSubtrahend) * -1;
                resultForCurrentDigit = 10 + resultForCurrentDigit;
            }

            result.append(resultForCurrentDigit % 10);
        }

        if (new BigNumber(minuendAsString, Locale.GERMAN).greaterThan(new BigNumber(subtrahendAsString, Locale.GERMAN))) {
            result.append(BigNumberParser.POSITIVE_SIGN);
        } else {
            if (minuend.isNegative() && subtrahend.isNegative() || !minuend.isNegative() && subtrahend.isNegative()) {
                result.append(BigNumberParser.POSITIVE_SIGN);
            } else {
                result.append(BigNumberParser.NEGATIVE_SIGN);
            }
        }

        return new BigNumber(result.reverse().toString(), minuend.getLocale());
    }

    private static String getSignAsString(BigNumber minuend) {
        return minuend.isNegative() ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN;
    }

}

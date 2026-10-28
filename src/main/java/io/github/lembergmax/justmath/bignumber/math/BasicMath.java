package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.core.NumberFormatLookup;

public class BasicMath {

    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        final String addend1AsString = MathTools.makeNumbersSameLength(addend2, addend1);
        final String addend2AsString = MathTools.makeNumbersSameLength(addend1, addend2);

        final StringBuilder result = new StringBuilder();

        int carry = 0;
        int addend1CharAtI;
        int addend2CharAtI;
        for (int i = addend1AsString.length() - 1; i >= 0; i--) {
            if (decimalSeparator.equals(String.valueOf(addend1AsString.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            addend1CharAtI = Integer.parseInt(String.valueOf(addend1AsString.charAt(i)));
            addend2CharAtI = Integer.parseInt(String.valueOf(addend2AsString.charAt(i)));
            final int resultForCurrentDigit = addend1CharAtI + addend2CharAtI + carry;
            if (resultForCurrentDigit >= 10) {
                carry = (addend1CharAtI + addend2CharAtI) / 10;
            }
            result.append(resultForCurrentDigit % 10);
        }
        final StringBuilder carryToAppend = new StringBuilder(String.valueOf(carry));
        result.append(carryToAppend.reverse());

        return new BigNumber(result.reverse().toString(), addend1.getLocale());
    }

    public static BigNumber subtract(
            final BigNumber minuend,
            final BigNumber subtrahend
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(minuend.getLocale());
        final String minuendSignAsString = getSignAsString(minuend);
        final String subtrahendSignAsString = getSignAsString(subtrahend);
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

            if (!"-".equals(String.valueOf(minuendAsString.charAt(i))) && !"-".equals(String.valueOf(subtrahendAsString.charAt(i)))) {
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
        }
        // TODO remove later if not needed
//        StringBuilder carryToAppend = new StringBuilder(String.valueOf(currentMinuend - currentSubtrahend - carry));
//        result.append(carryToAppend.reverse());

        return new BigNumber(result.reverse().toString(), minuend.getLocale());
    }

    private static String getSignAsString(BigNumber minuend) {
        return minuend.isNegative() ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN;
    }

}

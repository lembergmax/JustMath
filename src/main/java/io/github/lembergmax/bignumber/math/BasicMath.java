package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.core.BigNumberParser;
import io.github.lembergmax.core.NumberFormatLookup;

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
        int addend1CharAtI = 0;
        int addend2CharAtI = 0;
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
            final BigNumber subtrahend1,
            final BigNumber subtrahend2
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(subtrahend1.getLocale());
        final String subtrahend1AsString = MathTools.makeNumbersSameLength(subtrahend2, subtrahend1);
        final String subtrahend2AsString = MathTools.makeNumbersSameLength(subtrahend1, subtrahend2);

        final StringBuilder result = new StringBuilder();

        int carry = 0;
        int currentSubtrahend1 = 0;
        int currentSubtrahend2 = 0;

        // Loop rückwärts über alle Zeichen und subtrahieren, Rest immer merken und auf nächste Ziffer projezieren
        for (int i = subtrahend1AsString.length() - 1; i > 0; i--) {
            // Ist das aktuelle Zeichen von 
            if (decimalSeparator.equals(String.valueOf(subtrahend1AsString.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            currentSubtrahend1 = Integer.parseInt(String.valueOf(subtrahend1AsString.charAt(i)));
            currentSubtrahend2 = Integer.parseInt(String.valueOf(subtrahend2AsString.charAt(i)));
            final int resultForCurrentDigit = currentSubtrahend1 + currentSubtrahend2 + carry;
            if (resultForCurrentDigit >= 10) {
                carry = (currentSubtrahend1 + currentSubtrahend2) / 10;
            }
            result.append(resultForCurrentDigit % 10);
        }
        StringBuilder carryToAppend = new StringBuilder(String.valueOf(currentSubtrahend1 + currentSubtrahend2 + carry));
        result.append(carryToAppend.reverse());

        return new BigNumber(result.reverse().toString(), subtrahend1.getLocale());
    }

}

package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.core.NumberFormatLookup;

public class BasicMath {

    public static BigNumber add(
            // TODO: besseren namen ausdenken (anstatt zahlen)
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
        for (int i = addend1AsString.length() - 1; i > 0; i--) {
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
        StringBuilder carryToAppend = new StringBuilder(String.valueOf(addend1CharAtI + addend2CharAtI + carry));
        result.append(carryToAppend.reverse());

        return new BigNumber(result.reverse().toString(), addend1.getLocale());
    }

}

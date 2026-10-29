package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.NumberFormatLookup;

public class BasicMath {

    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        final String cleanedAddend1 = MathTools.makeNumbersSameLength(addend2, addend1);
        final String cleanedAddend2 = MathTools.makeNumbersSameLength(addend1, addend2);

        final StringBuilder result = new StringBuilder();

        int carry = 0;
        int addend1CharAtI;
        int addend2CharAtI;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            if (decimalSeparator.equals(String.valueOf(cleanedAddend1.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            addend1CharAtI = Integer.parseInt(String.valueOf(cleanedAddend1.charAt(i)));
            addend2CharAtI = Integer.parseInt(String.valueOf(cleanedAddend2.charAt(i)));
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

}

package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.core.NumberFormatLookup;

public class BasicMath {

    public static BigNumber add(
            // TODO: besseren namen ausdenken (anstatt zahlen)
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        /*
        "[1][2][3][4][5][6]"
        "[6][5][4][3][2][1]"
        - Max String length = 2147483647
         */
        final String decimalFormatSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        final String addend1AsString = MathTools.makeNumbersSameLength(addend2, addend1);
        final String addend2AsString = MathTools.makeNumbersSameLength(addend1, addend2);

        final StringBuilder result = new StringBuilder();

        int carry = 0;
        for(int i = addend1AsString.length() - 1; i > 0; i--) {
            if(decimalFormatSeparator.equals(String.valueOf(addend1AsString.charAt(i)))) {
                result.append(decimalFormatSeparator);
                continue;
            }

            final Integer addend1CharAtI = Integer.valueOf(String.valueOf(addend1AsString.charAt(i)));
            final Integer addend2CharAtI = Integer.valueOf(String.valueOf(addend2AsString.charAt(i)));

            // TODO: fix this shi
            if(i == addend1AsString.length() - 1) {
                result.append(addend1CharAtI + addend2CharAtI);
            } else {
                carry = (addend1CharAtI + addend2CharAtI + carry) % 10;
                result.append(addend1CharAtI + addend2CharAtI + carry);
            }
        }

        return new BigNumber(result.toString(), addend1.getLocale());
    }

}

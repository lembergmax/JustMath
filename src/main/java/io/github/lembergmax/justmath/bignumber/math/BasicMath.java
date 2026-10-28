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
        for (int i = addend1AsString.length() - 1; i >= 0; i--) {
            if (decimalSeparator.equals(String.valueOf(addend1AsString.charAt(i)))) {
                result.append(decimalSeparator);
                continue;
            }

            final int addend1CharAtI = Integer.parseInt(String.valueOf(addend1AsString.charAt(i)));
            final int addend2CharAtI = Integer.parseInt(String.valueOf(addend2AsString.charAt(i)));

            final int resultForCurrentDigit = addend1CharAtI + addend2CharAtI + carry;
            carry = resultForCurrentDigit / 10;
            result.append(resultForCurrentDigit % 10);
        }
        final StringBuilder carryToAppend = new StringBuilder(String.valueOf(carry));
        result.append(carryToAppend.reverse());

        // + + = +
        // - - = -
        // + - / - + = subtract der numbers

        final boolean isAddend1Negative = addend1.isNegative();
        final boolean isAddend2Negative = addend2.isNegative();

        final boolean isResultPositive;
        if (!isAddend1Negative && !isAddend2Negative) {
            isResultPositive = true;
        } else if (isAddend1Negative && isAddend2Negative) {
            isResultPositive = false;
        } else if (!isAddend1Negative) {
            return subtract(addend1, addend2.abs());
        } else {
            return subtract(addend2, addend1.abs());
        }

        return new BigNumber(
                (isResultPositive ? BigNumberParser.POSITIVE_SIGN : BigNumberParser.NEGATIVE_SIGN) +
                        result.reverse(), addend1.getLocale()
        );
    }

}

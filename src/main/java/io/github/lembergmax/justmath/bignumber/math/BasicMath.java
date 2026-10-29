package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.NumberFormatLookup;

import java.util.Locale;

public class BasicMath {

    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        final String cleanedAddend1 = MathTools.makeNumbersSameLength(addend2, addend1);
        final String cleanedAddend2 = MathTools.makeNumbersSameLength(addend1, addend2);

        StringBuilder result = new StringBuilder();
        int carry = 0;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            // Dezimaltrenner überspringen
            final String currentCharacter = String.valueOf(cleanedAddend1.charAt(i));
            if (decimalSeparator.equals(currentCharacter)) {
                result.insert(0, decimalSeparator);
                continue;
            }

            final int currentDigitOfAddend1 = charAsInt(cleanedAddend1.charAt(i));
            final int currentDigitOfAddend2 = charAsInt(cleanedAddend2.charAt(i));
            int resultForCurrentDigits = currentDigitOfAddend1 + currentDigitOfAddend2 + carry;
            carry = resultForCurrentDigits < 10 ? 0 : resultForCurrentDigits / 10;
            if (resultForCurrentDigits >= 10) {
                resultForCurrentDigits = resultForCurrentDigits - 10;
            }

            result.insert(0, resultForCurrentDigits);
        }
        result.insert(0, carry);

        return new BigNumber(result.toString(), Locale.GERMAN); // TODO
    }

    private static int charAsInt(
            final char character
    ) {
        return Integer.parseInt(String.valueOf(character));
    }

}

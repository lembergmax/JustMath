package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.NumberFormatLookup;

public class BasicMath {

    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        // Dezimaltrenner für die aktuelle Locale holen
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(addend1.getLocale());
        // Zahlen gleichlang machen um "Schriftlich" zu addieren
        final String cleanedAddend1 = MathTools.makeNumbersSameLength(addend2, addend1);
        final String cleanedAddend2 = MathTools.makeNumbersSameLength(addend1, addend2);

        // TODO: die kleinere zahl vom größeren abziehen

        StringBuilder result = new StringBuilder();
        int carry = 0;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            // Dezimaltrenner überspringen und in das Ergebnis einfügen
            final String currentCharacter = String.valueOf(cleanedAddend1.charAt(i));
            if (decimalSeparator.equals(currentCharacter)) {
                result.insert(0, decimalSeparator);
                continue;
            }

            // aktuelle Zahlen holen
            final int currentDigitOfAddend1 = charAsInt(cleanedAddend1.charAt(i));
            final int currentDigitOfAddend2 = charAsInt(cleanedAddend2.charAt(i));
            // aktuelle Zahlen und carry miteinander verrechnen
            int resultForCurrentDigits = currentDigitOfAddend1 + currentDigitOfAddend2 + carry;
            // neuen carry berechnen
            carry = resultForCurrentDigits < 10 ? 0 : resultForCurrentDigits / 10;
            // ergebnis der aktuellen Zahlen anpassen, um die erste Stelle einer zweistelligen zahl zu entfernen
            if (resultForCurrentDigits >= 10) {
                resultForCurrentDigits = resultForCurrentDigits - 10;
            }

            result.insert(0, resultForCurrentDigits);
        }
        result.insert(0, carry);

        return new BigNumber(result.toString(), addend1.getLocale());
    }

    /**
     * Wandelt einen char in einen int um.
     *
     * @param character der char, welcher umgewandelt werden soll
     * @return der umgewandelte char als int
     */
    private static int charAsInt(
            final char character
    ) {
        return Integer.parseInt(String.valueOf(character));
    }

}

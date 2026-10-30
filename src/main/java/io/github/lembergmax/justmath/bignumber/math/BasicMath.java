package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.BigNumberParser;

import java.util.Locale;

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

    public static BigNumber subtract(
            BigNumber minuend,
            BigNumber subtrahend
    ) {
        if (!(minuend.isNegative() && !subtrahend.isNegative()) && !(!minuend.isNegative() && subtrahend.isNegative())) {
            if (minuend.abs().smallerThan(subtrahend.abs())) {
                final BigNumber tmpMinuend = new BigNumber(minuend);
                minuend = subtrahend;
                subtrahend = tmpMinuend;
            }
        }

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

            currentMinuend = charAsInt(minuendAsString.charAt(i));
            currentSubtrahend = charAsInt(subtrahendAsString.charAt(i));
            if ((minuend.isNegative() && !subtrahend.isNegative()) || (!minuend.isNegative() && subtrahend.isNegative())) {
                resultForCurrentDigit = currentMinuend + currentSubtrahend + carry;

                if (resultForCurrentDigit >= 10) {
                    resultForCurrentDigit -= 10;
                    carry = 1;
                } else {
                    carry = 0;
                }
            } else {
                resultForCurrentDigit = currentMinuend - currentSubtrahend - carry;

                if (i == 0 && carry == 0) {
                    resultForCurrentDigit = 0;
                }

                if (resultForCurrentDigit < 0) {
                    carry = (currentMinuend - currentSubtrahend) * -1;
                    resultForCurrentDigit = 10 + resultForCurrentDigit;
                }
            }

            result.append(resultForCurrentDigit % 10);
        }

        // Vorzeichen anfügen
        if (new BigNumber(minuendSign + minuendAsString, Locale.GERMAN).greaterThan(new BigNumber(subtrahendSign + subtrahendAsString, Locale.GERMAN))) {
            if (minuend.isNegative() && !subtrahend.isNegative()) {
                result.append(BigNumberParser.NEGATIVE_SIGN);
            } else if (!minuend.isNegative() && subtrahend.isNegative()) {
                result.append(BigNumberParser.POSITIVE_SIGN);
            } else {
                result.append(BigNumberParser.POSITIVE_SIGN);
            }
        } else {
            if (minuend.isNegative() && !subtrahend.isNegative()) {
                result.append(BigNumberParser.NEGATIVE_SIGN);
            } else if (!minuend.isNegative() && subtrahend.isNegative()) {
                result.append(BigNumberParser.POSITIVE_SIGN);
            } else {
                result.append(BigNumberParser.NEGATIVE_SIGN);
            }
        }

        return new BigNumber(result.reverse().toString(), minuend.getLocale());
    }

    public static String getSignAsString(final BigNumber minuend) {
        return minuend.isNegative() ? BigNumberParser.NEGATIVE_SIGN : BigNumberParser.POSITIVE_SIGN;
    }
}
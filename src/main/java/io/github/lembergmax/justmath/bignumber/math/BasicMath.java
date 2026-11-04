package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.BigNumberParser;

import java.util.Locale;

public class BasicMath {

    /**
     * Adds two numbers, accounting for their signs and decimal separators.
     *
     * @param addend1      the first number to add
     * @param addend2      the second number to add
     * @param targetLocale the locale used to format the result
     * @return the sum of the two addends in the target locale
     */
    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2,
            final Locale targetLocale
    ) {
        // Dezimaltrenner für die aktuelle Locale holen
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(BigNumber.INTERNAL_LOCALE);
        final String decimalSeparatorOfTargetLocale = NumberFormatLookup.getDecimalSeparatorForLocale(targetLocale);

        // Zahlen ordnen, also addend1 ist die größte Zahl
        final BigNumber[] newAddends = MathTools.sortBySizeIgnoreSigns(addend1, addend2);

        // Zahlen gleichlang machen um "Schriftlich" zu addieren
        final String cleanedAddend1 = MathTools.makeNumbersSameLength(newAddends[0].abs(), newAddends[1].abs(), BigNumber.INTERNAL_LOCALE);
        final String cleanedAddend2 = MathTools.makeNumbersSameLength(newAddends[1].abs(), newAddends[0].abs(), BigNumber.INTERNAL_LOCALE);

        // Wenn + und - oder - und +
        final boolean subtract = addend1.isNegative() != addend2.isNegative();

        StringBuilder result = new StringBuilder();
        int carryOrBorrow = 0;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            // Dezimaltrenner überspringen und in das Ergebnis einfügen (unter beachtung, dass die beiden zahlen vertauscht worden sein könnten)
            final String currentCharacter = String.valueOf(cleanedAddend1.charAt(i));
            if (decimalSeparator.equals(currentCharacter)) {
                result.insert(0, decimalSeparatorOfTargetLocale);
                continue;
            }

            // aktuelle Zahlen holen
            final int currentDigitOfAddend1 = MathTools.charAsInt(cleanedAddend1.charAt(i));
            final int currentDigitOfAddend2 = MathTools.charAsInt(cleanedAddend2.charAt(i));
            int resultForCurrentDigit;

            if (subtract) {
                // kann 0..19 sein
                final int tempResultForCurrentDigit = currentDigitOfAddend1 - currentDigitOfAddend2 - carryOrBorrow;
                resultForCurrentDigit = tempResultForCurrentDigit < 0 ? tempResultForCurrentDigit + 10 : tempResultForCurrentDigit;
                // borrow setzen
                carryOrBorrow = tempResultForCurrentDigit < 0 ? 1 : 0;
            } else {
                // kann -10..9 sein
                final int tempResultForCurrentDigit = currentDigitOfAddend1 + currentDigitOfAddend2 + carryOrBorrow;
                resultForCurrentDigit = tempResultForCurrentDigit % 10;
                // carry setzen
                carryOrBorrow = tempResultForCurrentDigit >= 10 ? 1 : 0;
            }

            result.insert(0, resultForCurrentDigit);
        }
        result.insert(0, carryOrBorrow);

        final String finalResult = result.toString();
        return new BigNumber(
                MathTools.signForAdditionResult(newAddends[0].isNegative()) + finalResult,
                targetLocale
        );
    }

    /**
     * Subtracts two numbers, accounting for their signs and decimal separators.
     *
     * @param minuend      the first number to add
     * @param subtrahend   the second number to add
     * @param targetLocale the locale used to format the result
     * @return the difference of minuend and subtrahend in the target locale
     */
    public static BigNumber subtract(
            final BigNumber minuend,
            final BigNumber subtrahend,
            final Locale targetLocale
    ) {
        return add(minuend, subtrahend.negate(), targetLocale);
    }

    /**
     * Multiplies two numbers and formats the result using the target locale.
     *
     * @param factor1      the first factor
     * @param factor2      the second factor
     * @param targetLocale the locale used to format the result
     * @return the product of the two factors in the target locale
     */
    public static BigNumber multiply(
            final BigNumber factor1,
            final BigNumber factor2,
            final Locale targetLocale
    ) {
        final String decimalSeparatorOfTargetLocale = NumberFormatLookup.getDecimalSeparatorForLocale(targetLocale);

        final int decimalPlacesOfFactor1 = MathTools.countDecimalPlaces(factor1);
        final int decimalPlacesOfFactor2 = MathTools.countDecimalPlaces(factor2);
        final int decimalPlacesOfResult = decimalPlacesOfFactor1 + decimalPlacesOfFactor2;

        // Dezimaltrenner entfernen, um "schriftlich" zu multiplizieren
        String cleanedFactor1 = factor1.getBeforeDecimal();
        if (decimalPlacesOfFactor1 > 0) {
            cleanedFactor1 += factor1.getAfterDecimal();
        }

        String cleanedFactor2 = factor2.getBeforeDecimal();
        if (decimalPlacesOfFactor2 > 0) {
            cleanedFactor2 += factor2.getAfterDecimal();
        }

        BigNumber currentResult = BigNumbers.ZERO;

        // Für jede Ziffer von factor2 eine Teilzeile berechnen und zum Ergebnis addieren
        for (int i = cleanedFactor2.length() - 1; i >= 0; i--) {
            final int currentDigitOfFactor2 = MathTools.charAsInt(cleanedFactor2.charAt(i));
            final int indentation = cleanedFactor2.length() - 1 - i;

            final StringBuilder currentRow = new StringBuilder();
            int carry = 0;

            for (int j = cleanedFactor1.length() - 1; j >= 0; j--) {
                final int currentDigitOfFactor1 = MathTools.charAsInt(cleanedFactor1.charAt(j));

                final int tempResultForCurrentDigit = currentDigitOfFactor1 * currentDigitOfFactor2 + carry;
                currentRow.insert(0, tempResultForCurrentDigit % 10);
                carry = tempResultForCurrentDigit / 10;
            }

            if (carry > 0) {
                currentRow.insert(0, carry);
            }

            currentRow.append(MathTools.getZeroNTimes(indentation));
            currentResult = add(currentResult, new BigNumber(currentRow.toString(), Locale.US), Locale.US);
        }

        // Dezimaltrenner an der richtigen Stelle wieder einfügen
        StringBuilder scaledResult = new StringBuilder(currentResult.toString());
        while (scaledResult.length() <= decimalPlacesOfResult) {
            scaledResult.insert(0, BigNumberParser.ZERO_AS_STRING);
        }
        final int decimalSeparatorIndex = scaledResult.length() - decimalPlacesOfResult;

        final String finalResult = decimalPlacesOfResult == 0 ?
                scaledResult.toString() :
                scaledResult.substring(0, decimalSeparatorIndex) + decimalSeparatorOfTargetLocale + scaledResult.substring(decimalSeparatorIndex);

        return new BigNumber(
                MathTools.signForMultiplicationResult(factor1.isNegative(), factor2.isNegative()) + finalResult,
                targetLocale
        );
    }

}
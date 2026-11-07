package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.exception.DivisionByZeroException;
import io.github.lembergmax.justmath.exception.RoundingIsNecessaryException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.lembergmax.justmath.bignumber.BigNumber.INTERNAL_LOCALE;

public class BasicMath {

    private static final int UNKNOWN_DECIMAL_PLACES = -1;

    /**
     * Checks whether the first number is greater than the second number. Equal numbers are not greater.
     * The sign counts, so a number without a minus is greater than a number with a minus, even for 0.
     *
     * @param bigNumber1 the number to check
     * @param bigNumber2 the number to compare with
     * @return true if the first number is greater than the second number
     */
    public static boolean isGreaterThan(
            final BigNumber bigNumber1,
            final BigNumber bigNumber2
    ) {
        final String decimalSeparatorOfThisNumber = NumberFormatLookup.getDecimalSeparatorForLocale(INTERNAL_LOCALE);
        final String cleanedThisNumber = MathTools.makeNumbersSameLength(bigNumber1, bigNumber2, INTERNAL_LOCALE);
        final String cleanedOtherNumber = MathTools.makeNumbersSameLength(bigNumber2, bigNumber1, INTERNAL_LOCALE);

        // wenn verschiedene vorzeichen
        if (bigNumber1.isNegative() != bigNumber2.isNegative()) {
            // wenn die aktuelle positiv ist, dann ist diese größer
            return !bigNumber1.isNegative();
        }

        // gleiche Zahlen
        if (cleanedThisNumber.equals(cleanedOtherNumber)) {
            return false;
        }

        for (int i = 0; i <= cleanedThisNumber.length() - 1; i++) {
            final String currentCharacterOfThisNumber = String.valueOf(cleanedThisNumber.charAt(i));
            if (decimalSeparatorOfThisNumber.equals(currentCharacterOfThisNumber)) {
                continue;
            }

            final int currentDigitOfThisNumber = MathTools.charAsInt(cleanedThisNumber.charAt(i));
            final int currentDigitOfOtherNumber = MathTools.charAsInt(cleanedOtherNumber.charAt(i));

            // wenn die zahlen unterschiedlich sind
            if (currentDigitOfThisNumber != currentDigitOfOtherNumber) {
                // bei negativen Zahlen ist der Betrag umgekehrt: die kleinere Ziffer bedeutet die größere Zahl
                return bigNumber1.isNegative() == (currentDigitOfThisNumber < currentDigitOfOtherNumber);
            }
        }

        return false;
    }

    /**
     * Checks whether two numbers have the same value. The locale a number was written in and leading
     * and trailing zeros do not matter, but a 0 with a minus (from {@link BigNumber#negate()}) is not
     * equal to a 0 without a minus.
     *
     * @param bigNumber1 the first number
     * @param bigNumber2 the second number
     * @return true if the first number is neither greater nor smaller than the second number
     */
    public static boolean isEqualTo(
            final BigNumber bigNumber1,
            final BigNumber bigNumber2
    ) {
        return !isGreaterThan(bigNumber1, bigNumber2) && !isSmallerThan(bigNumber1, bigNumber2);
    }

    /**
     * Checks whether the first number is smaller than the second number. Equal numbers are not smaller.
     *
     * @param bigNumber1 the number to check
     * @param bigNumber2 the number to compare with
     * @return true if the first number is smaller than the second number
     */
    public static boolean isSmallerThan(
            final BigNumber bigNumber1,
            final BigNumber bigNumber2
    ) {
        return bigNumber2.isGreaterThan(bigNumber1);
    }

    /**
     * Adds two numbers.
     *
     * @param addend1 the first number to add
     * @param addend2 the second number to add
     * @return the sum of the two addends
     */
    public static BigNumber add(
            final BigNumber addend1,
            final BigNumber addend2
    ) {
        // Dezimaltrenner für die aktuelle Locale holen
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(INTERNAL_LOCALE);

        // Zahlen ordnen, also addend1 ist die größte Zahl
        final BigNumber[] newAddends = MathTools.sortBySizeIgnoreSigns(addend1, addend2);

        // Zahlen gleichlang machen um "Schriftlich" zu addieren
        final String cleanedAddend1 = MathTools.makeNumbersSameLength(newAddends[0].abs(), newAddends[1].abs(), INTERNAL_LOCALE);
        final String cleanedAddend2 = MathTools.makeNumbersSameLength(newAddends[1].abs(), newAddends[0].abs(), INTERNAL_LOCALE);

        // Wenn + und - oder - und +
        final boolean subtract = addend1.isNegative() != addend2.isNegative();

        StringBuilder result = new StringBuilder();
        int carryOrBorrow = 0;
        for (int i = cleanedAddend1.length() - 1; i >= 0; i--) {
            // Dezimaltrenner überspringen und in das Ergebnis einfügen (unter beachtung, dass die beiden zahlen vertauscht worden sein könnten)
            final String currentCharacter = String.valueOf(cleanedAddend1.charAt(i));
            if (decimalSeparator.equals(currentCharacter)) {
                result.insert(0, decimalSeparator);
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
                INTERNAL_LOCALE
        );
    }

    /**
     * Subtracts two numbers.
     *
     * @param minuend    the first number to add
     * @param subtrahend the second number to add
     * @return the difference of minuend and subtrahend
     */
    public static BigNumber subtract(
            final BigNumber minuend,
            final BigNumber subtrahend
    ) {
        return add(minuend, subtrahend.negate());
    }

    /**
     * Multiplies two numbers.
     *
     * @param factor1 the first factor
     * @param factor2 the second factor
     * @return the product of the two factors
     */
    public static BigNumber multiply(
            final BigNumber factor1,
            final BigNumber factor2
    ) {
        final String decimalSeparatorOfTargetLocale = NumberFormatLookup.getDecimalSeparatorForLocale(INTERNAL_LOCALE);

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
            currentResult = add(currentResult, new BigNumber(currentRow.toString(), INTERNAL_LOCALE));
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
                INTERNAL_LOCALE
        );
    }

    /**
     * Divides two numbers. If the result never ends (for example 1 / 3), it is rounded
     * with {@link MathContext#DECIMAL64}.
     *
     * @param dividend the dividend
     * @param divisor  the divisor
     * @return the quotient of dividend and divisor
     * @throws DivisionByZeroException if the divisor is zero
     */
    public static BigNumber divide(
            final BigNumber dividend,
            final BigNumber divisor
    ) {
        return divide(dividend, divisor, BigNumbers.DEFAULT_MATH_CONTEXT);
    }

    /**
     * Divides two numbers. The division stops as soon as the result ends or its digits start to repeat.
     * A result with more decimal places than the precision of the math context is rounded with its rounding mode.
     * The precision only counts decimal places, the digits before the decimal separator are never rounded
     * and do not reduce the precision. Only for a result below 1 the zeros directly after the decimal
     * separator are skipped, so 1 / 300 with a precision of 3 is 0.00333.
     *
     * @param dividend    the dividend
     * @param divisor     the divisor
     * @param mathContext the precision (number of decimal places) and the rounding mode of the result,
     *                    a precision of 0 means no rounding
     * @return the quotient of dividend and divisor
     * @throws DivisionByZeroException if the divisor is zero
     * @throws ArithmeticException         if the precision is 0 and the result never ends
     * @throws RoundingIsNecessaryException  if the result needs rounding but the rounding mode is {@link RoundingMode#UNNECESSARY}
     */
    public static BigNumber divide(
            final BigNumber dividend,
            final BigNumber divisor,
            @NonNull final MathContext mathContext
    ) {
        // Division durch 0 ist nicht möglich, auch nicht durch eine 0 mit Minus
        if (divisor.abs().isEqualTo(BigNumbers.ZERO)) {
            throw new DivisionByZeroException();
        }
        if (dividend.abs().isEqualTo(BigNumbers.ZERO)) {
            return BigNumbers.ZERO;
        }

        // Dezimaltrenner für die aktuelle Locale holen
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(INTERNAL_LOCALE);
        final int precision = mathContext.getPrecision();

        // Zahlen gleichlang machen und den Dezimaltrenner entfernen, der Quotient bleibt dabei gleich (1.5 / 0.25 -> 150 / 25)
        final String cleanedDividend = MathTools.makeNumbersSameLength(dividend.abs(), divisor.abs(), INTERNAL_LOCALE)
                .replace(decimalSeparator, "");
        final BigNumber cleanedDivisor = new BigNumber(
                MathTools.makeNumbersSameLength(divisor.abs(), dividend.abs(), INTERNAL_LOCALE).replace(decimalSeparator, ""),
                INTERNAL_LOCALE
        );

        // Ganzzahliger Teil: "Schriftlich" dividieren, Ziffer für Ziffer von links nach rechts
        StringBuilder integerPart = new StringBuilder();
        BigNumber rest = BigNumbers.ZERO;
        for (int i = 0; i < cleanedDividend.length(); i++) {
            // nächste Ziffer des Dividenden hinten an den Rest anhängen ("herunterholen")
            final int currentDigitOfDividend = MathTools.charAsInt(cleanedDividend.charAt(i));
            rest = new BigNumber(rest.toString() + currentDigitOfDividend, INTERNAL_LOCALE);

            // zählen, wie oft der Divisor in den Rest passt, kann 0..9 sein
            int subtractionCount = 0;
            while (!rest.isSmallerThan(cleanedDivisor)) {
                rest = subtract(rest, cleanedDivisor);
                subtractionCount++;
            }

            integerPart.append(subtractionCount);
        }

        // Ist die Zahl ab 1, zählt die Genauigkeit nur die Nachkommastellen und nicht die Stellen davor
        final boolean hasDigitsBeforeDecimalSeparator = !StringUtils.stripStart(integerPart.toString(), BigNumberParser.ZERO_AS_STRING).isEmpty();
        int decimalPlacesToKeep = precision > 0 && hasDigitsBeforeDecimalSeparator ?
                precision :
                UNKNOWN_DECIMAL_PLACES;

        // Nachkommastellen: statt einer Ziffer des Dividenden kommt eine 0 an den Rest, bis
        // der Rest 0 ist (das Ergebnis endet), die erlaubten Stellen erreicht sind oder sich der Rest wiederholt
        StringBuilder fractionPart = new StringBuilder();
        // Ein Ergebnis, das endet, hat nur Teiler 2 und 5 und damit höchstens log2(divisor) Nachkommastellen, 4 pro Ziffer des Divisors sind mehr als genug
        final int maxDecimalPlacesOfEndingResult = cleanedDivisor.toString().length() * 4;
        final List<BigNumber> restsByPosition = new ArrayList<>();
        final Map<String, Integer> positionByRest = new HashMap<>();
        while (!rest.isEqualTo(BigNumbers.ZERO) && fractionPart.length() != decimalPlacesToKeep) {
            // Gab es diesen Rest schon, wiederholen sich ab dort alle Ziffern (1 / 3: Rest 1 -> Ziffer 3 -> Rest 1 -> ...)
            final Integer firstPositionOfRest = positionByRest.get(rest.toString());
            if (precision == 0 && (firstPositionOfRest != null || fractionPart.length() > maxDecimalPlacesOfEndingResult)) {
                throw new ArithmeticException("The result of " + dividend + " / " + divisor + " never ends and can not be rounded with precision 0.");
            }
            if (firstPositionOfRest != null) {
                // Die restlichen Ziffern und den Rest an der Rundungsstelle aus der Wiederholung übernehmen, statt weiterzurechnen
                final int periodLength = fractionPart.length() - firstPositionOfRest;
                while (fractionPart.length() < decimalPlacesToKeep) {
                    final int positionInPeriod = (fractionPart.length() - firstPositionOfRest) % periodLength;
                    fractionPart.append(fractionPart.charAt(firstPositionOfRest + positionInPeriod));
                }
                rest = restsByPosition.get(firstPositionOfRest + (decimalPlacesToKeep - firstPositionOfRest) % periodLength);
                break;
            }
            positionByRest.put(rest.toString(), fractionPart.length());
            restsByPosition.add(rest);

            rest = new BigNumber(rest + BigNumberParser.ZERO_AS_STRING, INTERNAL_LOCALE);
            int subtractionCount = 0;
            while (!rest.isSmallerThan(cleanedDivisor)) {
                rest = subtract(rest, cleanedDivisor);
                subtractionCount++;
            }
            fractionPart.append(subtractionCount);

            // Bei einer Zahl kleiner als 1 zählen die Nullen nach dem Komma nicht zur Genauigkeit
            if (decimalPlacesToKeep == UNKNOWN_DECIMAL_PLACES && precision > 0 && subtractionCount != 0) {
                decimalPlacesToKeep = fractionPart.length() - 1 + precision;
            }
        }

        final String sign = MathTools.signForDivisionResult(dividend.isNegative(), divisor.isNegative());
        BigNumber quotient = new BigNumber(
                integerPart + (fractionPart.isEmpty() ? "" : decimalSeparator + fractionPart),
                INTERNAL_LOCALE
        );

        // Ist der Rest nicht 0, wurde etwas abgeschnitten, und es muss ggf. gerundet werden
        final String lastDigits = fractionPart.isEmpty() ? integerPart.toString() : fractionPart.toString();
        final int lastDigit = MathTools.charAsInt(lastDigits.charAt(lastDigits.length() - 1));
        if (shouldRoundUp(mathContext.getRoundingMode(), BigNumberParser.NEGATIVE_SIGN.equals(sign), lastDigit, rest, cleanedDivisor)) {
            quotient = add(quotient, getUnitInLastPlace(fractionPart.length(), decimalSeparator));
        }

        return new BigNumber(sign + quotient, INTERNAL_LOCALE);
    }

    /**
     * Decides whether the last kept digit of a quotient has to be rounded up.
     *
     * @param roundingMode     the rounding mode
     * @param isResultNegative whether the quotient is negative
     * @param lastDigit        the last kept digit of the quotient
     * @param rest             the rest of the division after the last kept digit
     * @param divisor          the divisor the rest belongs to
     * @return true if the last kept digit has to be rounded up
     * @throws RoundingIsNecessaryException if the rest is not 0 but the rounding mode is {@link RoundingMode#UNNECESSARY}
     */
    private static boolean shouldRoundUp(
            final RoundingMode roundingMode,
            final boolean isResultNegative,
            final int lastDigit,
            final BigNumber rest,
            final BigNumber divisor
    ) {
        // Ohne Rest wurde nichts abgeschnitten
        if (rest.isEqualTo(BigNumbers.ZERO)) {
            return false;
        }

        // Der abgeschnittene Teil ist rest / divisor einer Stelle, also genau die Hälfte, wenn rest * 2 == divisor
        final BigNumber doubledRest = add(rest, rest);
        final boolean isBelowHalf = doubledRest.isSmallerThan(divisor);
        final boolean isAboveHalf = doubledRest.isGreaterThan(divisor);

        return switch (roundingMode) {
            case UP -> true;
            case DOWN -> false;
            case CEILING -> !isResultNegative;
            case FLOOR -> isResultNegative;
            case HALF_UP -> !isBelowHalf;
            case HALF_DOWN -> isAboveHalf;
            case HALF_EVEN -> isAboveHalf || (!isBelowHalf && lastDigit % 2 != 0);
            case UNNECESSARY -> throw new RoundingIsNecessaryException();
        };
    }

    /**
     * Returns the smallest step of a number with a given count of decimal places, for example 0.001 for 3.
     * Rounding only happens behind the decimal separator, so there is at least one decimal place.
     *
     * @param decimalPlaces    the count of decimal places
     * @param decimalSeparator the decimal separator of {@link BigNumber#INTERNAL_LOCALE}
     * @return the number 1, shifted by the decimal places
     */
    private static BigNumber getUnitInLastPlace(
            final int decimalPlaces,
            final String decimalSeparator
    ) {
        return new BigNumber(
                BigNumberParser.ZERO_AS_STRING + decimalSeparator + MathTools.getZeroNTimes(decimalPlaces - 1) + "1",
                INTERNAL_LOCALE
        );
    }

}

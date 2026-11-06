package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.BigNumberParser;
import io.github.lembergmax.justmath.exception.DivisionByZeroException;

public class BasicMath {

    // Ohne Grenze würde z.B. 1 / 3 nie enden
    private static final int MAX_DECIMAL_PLACES = 10;

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
        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(BigNumber.INTERNAL_LOCALE);

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
                BigNumber.INTERNAL_LOCALE
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
        final String decimalSeparatorOfTargetLocale = NumberFormatLookup.getDecimalSeparatorForLocale(BigNumber.INTERNAL_LOCALE);

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
            currentResult = add(currentResult, new BigNumber(currentRow.toString(), BigNumber.INTERNAL_LOCALE));
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
                BigNumber.INTERNAL_LOCALE
        );
    }

    /**
     * Divides two numbers. The result is cut off after 10 decimal places.
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
        // 1. Sonderfälle zuerst abfangen
        if (divisor.isEqualTo(BigNumbers.ZERO)) {
            throw new DivisionByZeroException();
        }
        if (dividend.isEqualTo(BigNumbers.ZERO)) {
            return BigNumbers.ZERO;
        }

        final String decimalSeparator = NumberFormatLookup.getDecimalSeparatorForLocale(BigNumber.INTERNAL_LOCALE);

        // 2. Vorzeichen des Ergebnisses merken, danach nur noch mit den Beträgen rechnen
        final String resultSign = MathTools.signForDivisionResult(dividend.isNegative(), divisor.isNegative());

        // 3. Beide Zahlen zu ganzen Zahlen machen
        //    - Beide werden mit derselben Zehnerpotenz multipliziert, der Quotient bleibt also gleich.
        //      Beispiel: 1.5 / 0.25  ->  "1.50" / "0.25"  ->  150 / 25
        //    - Die zusätzlichen Nullen am Dividenden liefern die Nachkommastellen (siehe Schritt 6).
        //    - Der Divisor ist eine BigNumber, weil subtract und isSmallerThan eine BigNumber brauchen.
        //      Der Dividend bleibt ein String, weil die Schleife ihn Ziffer für Ziffer durchgeht.
        final String cleanedDividend = MathTools.makeNumbersSameLength(dividend.abs(), divisor.abs(), BigNumber.INTERNAL_LOCALE)
                .replace(decimalSeparator, "") + MathTools.getZeroNTimes(MAX_DECIMAL_PLACES);
        final BigNumber cleanedDivisor = new BigNumber(
                MathTools.makeNumbersSameLength(divisor.abs(), dividend.abs(), BigNumber.INTERNAL_LOCALE)
                        .replace(decimalSeparator, ""),
                BigNumber.INTERNAL_LOCALE
        );

        // 4. Variablen anlegen
        BigNumber rest = BigNumbers.ZERO;
        final StringBuilder quotient = new StringBuilder();

        // 5. Schriftlich dividieren: Schleife über jede Ziffer des Dividenden, von links nach rechts
        //    Beispiel 7 / 2  ->  "70" + zehn Nullen geteilt durch 20
        //      Ziffer 7: rest =   7, 0-mal abziehen, quotient = "0",   rest danach =  7
        //      Ziffer 0: rest =  70, 3-mal abziehen, quotient = "03",  rest danach = 10
        //      Ziffer 0: rest = 100, 5-mal abziehen, quotient = "035", rest danach =  0
        //      restliche Nullen: je 0-mal abziehen, am Ende quotient = "035000000000"
        for (int i = 0; i < cleanedDividend.length(); i++) {
            // a) Ziffer hinten an den Rest anhängen ("herunterholen")
            final int currentDigitOfDividend = MathTools.charAsInt(cleanedDividend.charAt(i));
            rest = new BigNumber(rest.toString() + currentDigitOfDividend, BigNumber.INTERNAL_LOCALE);

            // b) Zähler für diese Stelle des Quotienten, am Ende immer 0..9
            int subtractionCount = 0;

            // TODO 5c) solange rest NICHT kleiner als cleanedDivisor ist:
            //              rest = subtract(rest, cleanedDivisor)
            //              subtractionCount++

            // TODO 5d) subtractionCount hinten an quotient anhängen
        }

        // TODO 6. Dezimaltrenner einfügen, MAX_DECIMAL_PLACES Stellen von rechts (StringBuilder.insert)
        //         Beispiel: "035000000000"  ->  "03.5000000000"

        // Hinweise
        //    - Nur einzelne Ziffern (charAsInt) sind ein int, nie ganze Zahlen als Integer/Long/Double parsen
        //    - isGreaterThan ist strikt (gleiche Zahlen -> false), für "größer oder gleich" also !isSmallerThan

        // 7. Ergebnis zurückgeben: der Konstruktor entfernt führende und abschließende Nullen
        //    "+03.5000000000"  ->  3.5
        return new BigNumber(resultSign + quotient, BigNumber.INTERNAL_LOCALE);
    }

}
package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.core.NumberFormatLookup;
import io.github.lembergmax.justmath.core.BigNumberParser;

public class BasicMath {

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
     * Divides two numbers.
     *
     * @param dividend the dividend
     * @param divisor  the divisor
     * @return the quotient of dividend and divisor
     */
    public static BigNumber divide(
            final BigNumber dividend,
            final BigNumber divisor
    ) {
        // 1. Sonderfälle zuerst abfangen
        //    - Divisor ist 0            -> Exception werfen (vor allem anderen)
        //    - Dividend ist 0           -> Ergebnis 0 zurückgeben

        // 2. Vorzeichen merken und danach nur noch mit den Beträgen rechnen
        //    - Vorzeichen des Ergebnisses wie bei multiply bestimmen
        //      (MathTools.signForMultiplicationResult)
        //    - dividend.abs() und divisor.abs() verwenden

        // 3. Beide Zahlen zu ganzen Zahlen machen (Quotient bleibt gleich!)
        //    - beide auf gleich viele Nachkommastellen bringen (hinten 0 anhängen)
        //    - Dezimaltrenner entfernen
        //    - Beispiel: 1,5 / 0,25  ->  150 / 25

        // 4. Variablen anlegen
        //    - rest      : BigNumber/String, Startwert 0
        //    - quotient  : StringBuilder, Startwert leer
        //    - zähler    : int, wie oft der Divisor abgezogen wurde (immer 0..9)

        // 5. Ganzzahliger Teil: Schleife über jede Ziffer des Dividenden, von links nach rechts
        //    a) Ziffer hinten an den Rest anhängen
        //    b) zähler = 0
        //    c) solange rest NICHT kleiner als divisor ist (>= also "nicht isSmallerThan"):
        //         rest = subtract(rest, divisor)
        //         zähler++
        //    d) zähler hinten an den quotient anhängen

        // 6. Nachkommastellen: nur wenn nach Schritt 5 der Rest nicht 0 ist
        //    - Dezimaltrenner an den quotient anhängen
        //    - wiederholen, bis rest == 0 ODER die maximale Anzahl Nachkommastellen erreicht ist
        //      (sonst läuft z.B. 1 / 3 endlos!):
        //         statt einer Dividendenziffer eine "0" an den Rest anhängen,
        //         dann dieselbe Schleife wie in 5 b) bis d)
        //    - Entscheiden: Maximale Stellenzahl als Konstante oder als Parameter?
        //      Abschneiden oder runden?

        // 7. Ergebnis aufräumen und zurückgeben
        //    - führende Nullen im quotient entfernen (aber vor dem Komma mindestens eine 0 lassen)
        //    - Vorzeichen aus Schritt 2 voranstellen
        //    - Dezimaltrenner der targetLocale verwenden (siehe multiply)
        //    - new BigNumber(ergebnis, targetLocale)

        // Hinweise
        //    - Intern mit Locale.US rechnen, erst am Ende in targetLocale umwandeln
        //    - Nirgends Integer/Long/Double.parse... auf ganze Zahlen anwenden,
        //      nur einzelne Ziffern (charAsInt) sind ein int
        //    - Hat der Rest weniger Stellen als der Divisor, ist er automatisch kleiner
        //    - subtract kann eine führende 0 liefern: prüfen, ob der Rest dadurch falsch
        //      verglichen wird oder wächst
        //    - isGreaterThan ist strikt (gleiche Zahlen -> false), deshalb für "größer oder
        //      gleich" die Verneinung von isSmallerThan nehmen

        // Tests (in dieser Reihenfolge)
        //    12 / 12 = 1          144 / 12 = 12        987 / 123 = 8 Rest 3
        //    7 / 2 = 3,5          1,5 / 0,25 = 6       -8 / 2 = -4
        //    987654321987654321 / 123456789123456789 = 8,0000000729 (bei 10 Nachkommastellen)

        final BigNumber quotient = BigNumbers.ZERO;
        return quotient;
    }

}
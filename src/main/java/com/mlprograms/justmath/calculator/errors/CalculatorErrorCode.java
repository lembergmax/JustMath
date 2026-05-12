/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.calculator.errors;

import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.Getter;
import lombok.NonNull;

/**
 * Stark typisierter Katalog aller Fehlerursachen, die in der {@code CalculatorEngine}
 * auftreten können.
 *
 * <p>
 * Jeder Code besitzt:
 * </p>
 * <ul>
 *   <li>einen <em>Bundle-Schlüssel</em> für die lokalisierte Meldung in
 *       {@code i18n/calculator_errors*.properties} (über {@link #getBundleKey()}),</li>
 *   <li>eine <em>Kategorie</em> aus {@link CustomExceptionMessages}, die für
 *       Rückwärtskompatibilität mit dem bestehenden {@code Throwable#getMessage()}
 *       von {@code SyntaxErrorException}/{@code ProcessingErrorException} sorgt.</li>
 * </ul>
 *
 * <p>
 * Codes ersetzen das frühere "Stringly-typed" Werfen freier Detail-Strings: Werte können
 * jetzt strukturell ausgewertet werden, ohne englische Textfragmente zu parsen.
 * </p>
 */
@Getter
public enum CalculatorErrorCode {

    /**
     * Ungültiges Zeichen im Ausdruck (z. B. {@code "2#3"}). Parameter:
     * {@code character}, {@code position}.
     */
    SYNTAX_INVALID_CHARACTER("error.syntax.invalidCharacter", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Fehlende schließende Klammer einer Funktion oder eines Ausdrucks. Parameter:
     * {@code function} (optional).
     */
    SYNTAX_MISSING_RIGHT_PAREN("error.syntax.missingRightParen", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Nicht zueinander passende Klammern (z. B. überzählige öffnende oder schließende Klammer).
     */
    SYNTAX_UNMATCHED_PAREN("error.syntax.unmatchedParen", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Unbekannte Funktion oder unbekannter Operator. Parameter: {@code function}.
     */
    SYNTAX_UNKNOWN_FUNCTION("error.syntax.unknownFunction", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Verwendung einer Variablen, die nicht im Variablen-Map definiert wurde. Parameter:
     * {@code variable}.
     */
    SYNTAX_UNKNOWN_VARIABLE("error.syntax.unknownVariable", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Unvollständiger Ausdruck — z. B. ungerade Anzahl Betragsstriche {@code |...} oder
     * vorzeitiges Ende des Ausdrucks.
     */
    SYNTAX_INCOMPLETE_EXPRESSION("error.syntax.incompleteExpression", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Falsche Anzahl Funktionsargumente. Parameter: {@code function}, {@code expected},
     * {@code actual} (sofern bekannt).
     */
    SYNTAX_WRONG_ARGUMENT_COUNT("error.syntax.wrongArgumentCount", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Trennzeichen ({@code ;}) an falscher Position bzw. außerhalb einer Funktion.
     */
    SYNTAX_MISPLACED_SEPARATOR("error.syntax.misplacedSeparator", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Fakultätsoperator {@code !} an unzulässiger Position.
     */
    SYNTAX_INVALID_FACTORIAL("error.syntax.invalidFactorial", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Division durch Null.
     */
    PROCESSING_DIVISION_BY_ZERO("error.processing.divisionByZero", CustomExceptionMessages.PROCESSING_ERROR),

    /**
     * Definitionsbereichsfehler (z. B. {@code sqrt(-4)} im reellen Bereich).
     */
    PROCESSING_DOMAIN_ERROR("error.processing.domainError", CustomExceptionMessages.PROCESSING_ERROR),

    /**
     * Interner, nicht weiter klassifizierbarer Verarbeitungsfehler. Fallback-Code.
     */
    PROCESSING_INTERNAL("error.processing.internal", CustomExceptionMessages.PROCESSING_ERROR);

    /**
     * Schlüssel im Ressourcen-Bundle {@code i18n/calculator_errors*.properties}.
     */
    @NonNull
    private final String bundleKey;

    /**
     * Kategorie, mit der dieser Code auf das alte {@link CustomExceptionMessages}-Modell
     * abgebildet wird. Bestimmt den {@code Throwable#getMessage()} stabilen Hauptstring.
     */
    @NonNull
    private final CustomExceptionMessages category;

    CalculatorErrorCode(@NonNull final String bundleKey, @NonNull final CustomExceptionMessages category) {
        this.bundleKey = bundleKey;
        this.category = category;
    }
}

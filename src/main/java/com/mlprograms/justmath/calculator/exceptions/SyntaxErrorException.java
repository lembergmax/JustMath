/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.calculator.exceptions;

import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.NonNull;

import java.util.Map;

/**
 * Exception für syntaktische Fehler im Ausdruck (z. B. ungültige Zeichen, Klammerfehler,
 * unbekannte Funktionen oder Variablen).
 *
 * <p>
 * Existierende String-Konstruktoren bleiben aus Gründen der Rückwärtskompatibilität erhalten;
 * neuer Code sollte die typisierten Konstruktoren mit {@link CalculatorErrorCode} bevorzugen,
 * damit die Engine lokalisierte Meldungen ableiten kann.
 * </p>
 */
public class SyntaxErrorException extends CalculatorException {

    /**
     * Erstellt eine Exception mit Standard-Detailmeldung.
     */
    public SyntaxErrorException() {
        super(CustomExceptionMessages.SYNTAX_ERROR, "Detailed Message was not specified.");
    }

    /**
     * Legacy-Konstruktor: erzeugt eine Exception mit freier englischer Detailmeldung.
     *
     * @param detailedMessage technische Detailbeschreibung
     */
    public SyntaxErrorException(@NonNull final String detailedMessage) {
        super(CustomExceptionMessages.SYNTAX_ERROR, detailedMessage);
    }

    /**
     * Konstruktor mit strukturiertem Fehlercode ohne Parameter.
     *
     * @param code             strukturierter Fehlercode
     * @param technicalDetail technische, englische Detailmeldung
     */
    public SyntaxErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final String technicalDetail
    ) {
        super(code, technicalDetail);
    }

    /**
     * Konstruktor mit strukturiertem Fehlercode, Parametern und optionaler Position.
     *
     * @param code             strukturierter Fehlercode
     * @param params           benannte Platzhalter (z. B. {@code character}, {@code function})
     * @param technicalDetail technische, englische Detailmeldung
     * @param position         optionale Position im Ausdruck
     */
    public SyntaxErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final Map<String, String> params,
            @NonNull final String technicalDetail,
            final Integer position
    ) {
        super(new CalculatorError(code, params, technicalDetail, position));
    }
}

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
 * Exception für Verarbeitungsfehler bei der Auswertung eines bereits geparsten Ausdrucks
 * (z. B. Division durch Null, Definitionsbereichsverletzungen, interne Inkonsistenzen).
 *
 * <p>
 * Die legacy String-Konstruktoren bleiben erhalten; neuer Code sollte die typisierten
 * Konstruktoren mit {@link CalculatorErrorCode} bevorzugen.
 * </p>
 */
public class ProcessingErrorException extends CalculatorException {

    /**
     * Erstellt eine Exception mit Standard-Detailmeldung.
     */
    public ProcessingErrorException() {
        super(CustomExceptionMessages.PROCESSING_ERROR, "Detailed Message was not specified.");
    }

    /**
     * Legacy-Konstruktor: erzeugt eine Exception mit freier englischer Detailmeldung.
     *
     * @param detailedMessage technische Detailbeschreibung
     */
    public ProcessingErrorException(@NonNull final String detailedMessage) {
        super(CustomExceptionMessages.PROCESSING_ERROR, detailedMessage);
    }

    /**
     * Konstruktor mit strukturiertem Fehlercode ohne Parameter.
     *
     * @param code             strukturierter Fehlercode
     * @param technicalDetail technische, englische Detailmeldung
     */
    public ProcessingErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final String technicalDetail
    ) {
        super(code, technicalDetail);
    }

    /**
     * Konstruktor mit strukturiertem Fehlercode und benannten Parametern.
     *
     * @param code             strukturierter Fehlercode
     * @param params           benannte Platzhalter
     * @param technicalDetail technische, englische Detailmeldung
     */
    public ProcessingErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final Map<String, String> params,
            @NonNull final String technicalDetail
    ) {
        super(new CalculatorError(code, params, technicalDetail));
    }
}

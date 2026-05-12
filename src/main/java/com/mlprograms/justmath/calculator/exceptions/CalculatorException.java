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

package com.mlprograms.justmath.calculator.exceptions;

import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.exceptions.CustomErrorException;
import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.Getter;
import lombok.NonNull;

/**
 * Gemeinsame Basisklasse aller von der {@code CalculatorEngine} geworfenen Exceptions.
 *
 * <p>
 * Erweitert {@link CustomErrorException} um einen strukturierten {@link CalculatorError}.
 * Dieser trägt Code, benannte Parameter und optionale Position und erlaubt nutzerfreundliche,
 * lokalisierte Formatierung via {@link CalculatorError#format}.
 * </p>
 *
 * <p>
 * Bestehende Konsumenten, die nur {@link Throwable#getMessage()} oder
 * {@link CustomErrorException#getCustomExceptionMessages()} auswerten, bleiben kompatibel:
 * die Hauptmeldung entspricht weiterhin dem Kategorie-Default
 * (z. B. {@code "Syntax Error"}/{@code "Processing Error"}).
 * </p>
 */
@Getter
public abstract class CalculatorException extends CustomErrorException {

    /**
     * Strukturierte Fehlerbeschreibung, falls vorhanden. {@code null} wenn die Exception
     * über die alten String-Konstruktoren erzeugt wurde.
     */
    private final CalculatorError calculatorError;

    /**
     * Konstruktor für legacy String-basierte Erzeugung.
     *
     * @param category        Kategorie für {@link Throwable#getMessage()}
     * @param detailedMessage technische Detailbeschreibung
     */
    protected CalculatorException(
            @NonNull final CustomExceptionMessages category,
            @NonNull final String detailedMessage
    ) {
        super(category, detailedMessage);
        this.calculatorError = null;
    }

    /**
     * Konstruktor mit strukturiertem Fehler.
     *
     * @param calculatorError strukturierte Fehlerbeschreibung
     */
    protected CalculatorException(@NonNull final CalculatorError calculatorError) {
        super(calculatorError.code().getCategory(), calculatorError.technicalDetail());
        this.calculatorError = calculatorError;
    }

    /**
     * Convenience-Konstruktor mit Code und technischer Meldung — keine Parameter.
     *
     * @param code             strukturierter Fehlercode
     * @param technicalDetail technische, englische Detailmeldung
     */
    protected CalculatorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final String technicalDetail
    ) {
        this(new CalculatorError(code, technicalDetail));
    }
}

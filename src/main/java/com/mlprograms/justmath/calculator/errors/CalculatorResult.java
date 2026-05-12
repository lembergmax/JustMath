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

import com.mlprograms.justmath.calculator.exceptions.CalculatorException;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;

/**
 * Ergebnis einer sicheren Auswertung — entweder {@link Success} mit dem Wert,
 * oder {@link Failure} mit einem {@link CalculatorError}.
 *
 * <p>
 * Dieser Typ erlaubt Aufrufern, Fehler ohne Exceptions zu behandeln. Beispiel:
 * </p>
 * <pre>{@code
 * CalculatorResult<BigNumber> result = engine.evaluateSafe("5/0");
 * if (result.isFailure()) {
 *     CalculatorError err = result.error().orElseThrow();
 *     String localized = err.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY);
 * }
 * }</pre>
 *
 * @param <T> Typ des Erfolgswerts
 */
public sealed interface CalculatorResult<T> permits CalculatorResult.Success, CalculatorResult.Failure {

    /**
     * Erstellt ein Erfolgsergebnis.
     *
     * @param value der berechnete Wert
     * @param <T>   Typ des Werts
     * @return Erfolgsergebnis
     */
    static <T> CalculatorResult<T> success(@NonNull final T value) {
        return new Success<>(value);
    }

    /**
     * Erstellt ein Fehlerergebnis.
     *
     * @param error Fehlerbeschreibung
     * @param <T>   Typ des erwarteten Werts
     * @return Fehlerergebnis
     */
    static <T> CalculatorResult<T> failure(@NonNull final CalculatorError error) {
        return new Failure<>(error);
    }

    /**
     * @return {@code true}, wenn dieses Ergebnis ein {@link Success} ist
     */
    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    /**
     * @return {@code true}, wenn dieses Ergebnis ein {@link Failure} ist
     */
    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    /**
     * Gibt den Erfolgswert zurück, falls vorhanden.
     *
     * @return {@link Optional} mit dem Wert oder leer
     */
    default Optional<T> value() {
        return this instanceof Success<T> s ? Optional.of(s.successValue()) : Optional.empty();
    }

    /**
     * Gibt den Fehler zurück, falls vorhanden.
     *
     * @return {@link Optional} mit dem Fehler oder leer
     */
    default Optional<CalculatorError> error() {
        return this instanceof Failure<T> f ? Optional.of(f.failureError()) : Optional.empty();
    }

    /**
     * Wirft im Fehlerfall eine passende {@link CalculatorException}, sonst liefert den Wert.
     *
     * @return der Erfolgswert
     */
    default T valueOrThrow() {
        if (this instanceof Success<T> s) {
            return s.successValue();
        }
        CalculatorError err = ((Failure<T>) this).failureError();
        throw new ProcessingErrorException(err.technicalDetail());
    }

    /**
     * Mappt den Erfolgswert. Im Fehlerfall bleibt das Ergebnis ein {@link Failure}.
     *
     * @param mapper Abbildungsfunktion
     * @param <R>    neuer Wertetyp
     * @return neues {@link CalculatorResult}
     */
    default <R> CalculatorResult<R> map(@NonNull final Function<? super T, ? extends R> mapper) {
        if (this instanceof Success<T> s) {
            return new Success<>(mapper.apply(s.successValue()));
        }
        return new Failure<>(((Failure<T>) this).failureError());
    }

    /**
     * Liefert die {@link CustomExceptionMessages}-Kategorie im Fehlerfall — nützlich für
     * Konsumenten, die das alte Kategorien-Modell nutzen.
     *
     * @return Optional mit der Kategorie oder leer
     */
    default Optional<CustomExceptionMessages> category() {
        return error().map(e -> e.code().getCategory());
    }

    /**
     * Erfolgsergebnis.
     *
     * @param successValue der berechnete Wert
     * @param <T>          Typ des Werts
     */
    record Success<T>(@NonNull T successValue) implements CalculatorResult<T> {
    }

    /**
     * Fehlerergebnis.
     *
     * @param failureError Fehlerbeschreibung
     * @param <T>          Typ des erwarteten Werts
     */
    record Failure<T>(@NonNull CalculatorError failureError) implements CalculatorResult<T> {
    }
}

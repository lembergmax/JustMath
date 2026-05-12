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

package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NonNull;

import java.math.MathContext;

/**
 * Immutable reziproke Umrechnungsformel der Form:
 *
 * <pre>
 * base = scale / value
 * </pre>
 *
 * <p>
 * Die Umkehrung ist symmetrisch:
 * </p>
 *
 * <pre>
 * value = scale / base
 * </pre>
 *
 * <p>
 * Diese Formel wird für Einheiten benötigt, bei denen die Quelleinheit der Kehrwert der
 * Basiseinheit ist. Typisches Beispiel: Kraftstoffverbrauch — die Basis ist {@code m/L}
 * (zurückgelegte Strecke pro Liter), eine Quelleinheit wie {@code L/100 km} ist deren
 * Kehrwert. Der affine Standardansatz {@code base = value * scale + offset} kann das
 * mathematisch nicht ausdrücken; nur für {@code value = 1} liefert er zufällig den
 * richtigen Wert.
 * </p>
 *
 * <p>
 * Die Klasse ist absichtlich package-private, damit die öffentliche API klein bleibt.
 * Externe Konsumenten können Instanzen über {@link ConversionFormulas#reciprocal(BigNumber)}
 * erhalten.
 * </p>
 *
 * <p>
 * Diese Implementierung ist unveränderlich und thread-sicher.
 * </p>
 */
final class ReciprocalConversionFormula implements ConversionFormula {

    /**
     * Multiplikativer Faktor: {@code base = scale / value}.
     *
     * <p>
     * Darf nicht null sein, sonst wäre {@code toBase} immer null und die Umkehrung undefiniert.
     * </p>
     */
    private final BigNumber scale;

    /**
     * Erzeugt eine neue reziproke Umrechnungsformel.
     *
     * @param scale Skalierungsfaktor; darf weder {@code null} noch {@code 0} sein
     * @throws IllegalArgumentException wenn {@code scale == 0}
     */
    ReciprocalConversionFormula(@NonNull final BigNumber scale) {
        if (scale.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale must not be zero");
        }
        this.scale = scale;
    }

    /**
     * Rechnet einen Wert in der konkreten Einheit in die Basiseinheit um:
     *
     * <pre>
     * base = scale / value
     * </pre>
     *
     * @param value       Eingabewert in der konkreten Einheit; darf nicht {@code null} sein
     * @param mathContext Mathekontext für Präzision/Rundung; darf nicht {@code null} sein
     * @return Wert in der Basiseinheit; niemals {@code null}
     * @throws ArithmeticException wenn {@code value == 0}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return scale.divide(value, mathContext);
    }

    /**
     * Rechnet einen Wert in der Basiseinheit zurück in die konkrete Einheit:
     *
     * <pre>
     * value = scale / base
     * </pre>
     *
     * @param baseValue   Eingabewert in der Basiseinheit; darf nicht {@code null} sein
     * @param mathContext Mathekontext für Präzision/Rundung; darf nicht {@code null} sein
     * @return Wert in der konkreten Einheit; niemals {@code null}
     * @throws ArithmeticException wenn {@code baseValue == 0}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return scale.divide(baseValue, mathContext);
    }

}

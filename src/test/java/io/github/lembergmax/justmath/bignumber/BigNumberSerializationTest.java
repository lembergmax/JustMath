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
package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.lembergmax.justmath.calculator.internal.CoordinateType;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * {@link BigNumber} is {@link java.io.Serializable} through {@link Number}. Its lazily created
 * {@code CalculatorEngine} is not serializable, so a number whose engine had been created used to fail with
 * a {@link java.io.NotSerializableException}. The engine is derived state and is not part of the serialized form.
 */
class BigNumberSerializationTest {

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(final T value) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            return (T) input.readObject();
        }
    }

    @Test
    @DisplayName("a BigNumber survives serialization after its calculator engine was created")
    void roundTripAfterEngineAccess() throws Exception {
        final BigNumber original = new BigNumber("1234.5678", Locale.GERMANY, new MathContext(42, RoundingMode.HALF_EVEN), TrigonometricMode.RAD);
        original.getCalculatorEngine();

        final BigNumber copy = roundTrip(original);

        assertEquals(original, copy);
        assertEquals(original.toString(), copy.toString());
        assertEquals(Locale.GERMANY, copy.getLocale());
        assertEquals(new MathContext(42, RoundingMode.HALF_EVEN), copy.getMathContext());
        assertEquals(TrigonometricMode.RAD, copy.getTrigonometricMode());
    }

    @Test
    @DisplayName("the calculator engine of a deserialized BigNumber is created again on demand")
    void engineIsRecreatedAfterDeserialization() throws Exception {
        final BigNumber original = new BigNumber("2");
        original.getCalculatorEngine();

        final BigNumber copy = roundTrip(original);

        assertEquals("3", copy.getCalculatorEngine().evaluateToString("1+2"));
    }

    @Test
    @DisplayName("a BigNumberCoordinate survives serialization")
    void coordinateRoundTrip() throws Exception {
        final BigNumberCoordinate original = new BigNumberCoordinate(new BigNumber("3"), new BigNumber("4.5"), CoordinateType.POLAR, Locale.US);
        original.getCalculatorEngine();

        final BigNumberCoordinate copy = roundTrip(original);

        assertEquals(original, copy);
        assertEquals(CoordinateType.POLAR, copy.getType());
    }
}

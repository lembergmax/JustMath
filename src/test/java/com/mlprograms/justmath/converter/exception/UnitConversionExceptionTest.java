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

package com.mlprograms.justmath.converter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UnitConversionException}.
 *
 * <p>
 * These tests validate that the exception is a specialized {@link ConversionException}
 * and behaves like a consistent "unit-conversion invalid" error type.
 * </p>
 */
final class UnitConversionExceptionTest {

    /**
     * Ensures {@link UnitConversionException} is a {@link ConversionException}.
     */
    @Test
    void isSubclassOfConversionException() {
        final UnitConversionException ex = new UnitConversionException("x");
        assertTrue(ex instanceof ConversionException);
    }

    /**
     * Ensures the message constructor stores the provided message.
     */
    @Test
    void messageConstructorStoresMessage() {
        final UnitConversionException ex = new UnitConversionException("message");
        assertEquals("message", ex.getMessage());
    }

}
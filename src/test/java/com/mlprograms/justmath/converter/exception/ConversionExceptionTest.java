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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConversionException}.
 *
 * <p>
 * These tests verify that the exception behaves like a consistent base exception type
 * for conversion-related failures.
 * </p>
 */
final class ConversionExceptionTest {

    /**
     * Verifies that the message-only constructor stores the provided message.
     */
    @Test
    void messageConstructorStoresMessage() {
        final ConversionException ex = new ConversionException("message");
        assertEquals("message", ex.getMessage());
        assertNull(ex.getCause());
    }

    /**
     * Verifies that the message+cause constructor stores both message and cause.
     */
    @Test
    void messageAndCauseConstructorStoresBoth() {
        final RuntimeException cause = new RuntimeException("cause");
        final ConversionException ex = new ConversionException("message", cause);

        assertEquals("message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

}
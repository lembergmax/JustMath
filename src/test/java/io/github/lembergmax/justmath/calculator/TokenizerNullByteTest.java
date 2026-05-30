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
package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;

/**
 * Audit fix K5: the tokenizer injects an internal control-character sentinel
 * ({@code WHITESPACE_BOUNDARY}) into the character stream to mark deliberate token
 * boundaries. If user input itself contains that sentinel (or any other non-printable
 * C0 control character that is not standard whitespace) it must be rejected as an
 * invalid character — never silently skipped, which would let an attacker smuggle a
 * boundary past a naive input filter.
 *
 * <p>The test is sentinel-value-agnostic: it asserts that <em>every</em> non-whitespace
 * C0 control character is rejected, so it keeps protecting the contract even if the
 * concrete sentinel value changes.</p>
 */
class TokenizerNullByteTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    @DisplayName("Every non-whitespace C0 control char is rejected, never silently dropped")
    void controlCharactersRejected() {
        for (int codePoint = 0x00; codePoint <= 0x1F; codePoint++) {
            final char control = (char) codePoint;
            if (Character.isWhitespace(control)) {
                // Tab, newline, CR, form feed and the ASCII separators are legitimate
                // whitespace and handled by removeWhitespace; skip them here.
                continue;
            }
            final String input = "1" + control + "2";
            final int cp = codePoint;
            assertThrows(SyntaxErrorException.class, () -> tokenizer.tokenize(input),
                    () -> "control char U+" + String.format("%04X", cp) + " must be rejected");
        }
    }

    @Test
    @DisplayName("The SOH sentinel (U+0001) specifically cannot be smuggled in")
    void sentinelSohRejected() {
        final String input = "1" + (char) 0x01 + "2";
        assertThrows(SyntaxErrorException.class, () -> tokenizer.tokenize(input));
    }

    @Test
    @DisplayName("Ordinary expressions still tokenize unchanged")
    void ordinaryExpressionUnaffected() {
        assertEquals(3, tokenizer.tokenize("1+2").size());
    }
}

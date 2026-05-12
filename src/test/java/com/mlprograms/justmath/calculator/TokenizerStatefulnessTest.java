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
package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.calculator.internal.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerStatefulnessTest {

    @Test
    void reuseProducesIdenticalTokensForAbsoluteBars() {
        Tokenizer tokenizer = new Tokenizer();
        String input = CalculatorEngineUtils.replaceAbsSigns("|3-7|");

        List<Token> first = tokenizer.tokenize(input);
        List<Token> second = tokenizer.tokenize(input);

        assertEquals(first.size(), second.size(), "token counts differ between calls");
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getType(), second.get(i).getType(),
                    "type differs at index " + i);
            assertEquals(first.get(i).getValue(), second.get(i).getValue(),
                    "value differs at index " + i);
        }
    }

    @Test
    void mixedExpressionsBetweenAbsoluteCallsDoNotCorruptState() {
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(CalculatorEngineUtils.replaceAbsSigns("|5|"));
        List<Token> tokens = tokenizer.tokenize("2+3");
        assertFalse(tokens.isEmpty());
        // verify tokens for second simple input are sensible
        assertEquals(Token.Type.NUMBER, tokens.get(0).getType());
    }
}

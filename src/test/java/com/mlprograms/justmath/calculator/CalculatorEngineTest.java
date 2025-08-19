/*
 * Copyright (c) 2025 Max Lemberg
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

import com.mlprograms.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorEngineTest {

    private final CalculatorEngine calculatorEngine = new CalculatorEngine();

    @ParameterizedTest
    @CsvSource(value = {
            "|-5|+3;8",
            "0;0",
            "0;0",
            "0;0"
    }, delimiter = ';')
    void evaluationResultTest(String calculationString, String expectedResult) {
        BigNumber result = calculatorEngine.evaluate(calculationString);
        assertEquals(expectedResult, result.toString());
    }

    @Test
    void justATest() {
        // TODO
        System.out.println(new Tokenizer().tokenize("|-5|+3"));
        System.out.println(new Parser().toPostfix(new Tokenizer().tokenize("|-5|+3")));
        System.out.println(new Evaluator().evaluate(new Parser().toPostfix(new Tokenizer().tokenize("|-5|+3"))));
        System.out.println(new Evaluator().evaluate(new Parser().toPostfix(new Tokenizer().tokenize("2*-3"))));
        System.out.println(new Evaluator().evaluate(new Parser().toPostfix(new Tokenizer().tokenize("-3*-2"))));
    }

    // TODO: variables test

}

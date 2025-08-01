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

package com.mlprograms.justmath.bignumber;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigNumberParserTest {

	private final BigNumberParser parser = new BigNumberParser();

	@ParameterizedTest(name = "[{index}] parse valid US-numbers ‘{0}’ → ‘{1}’")
	@CsvSource({
		"123.45, 123.45",
		"-7.89, -7.89",
		"0, 0",
		"1000, 1000",
		"  42.0  , 42"
	})
	void parseValidUSNumbers(String input, String expected) {
		BigNumber result = parser.parse(input, Locale.US);
		assertEquals(expected, result.toString());
	}

	@ParameterizedTest(name = "[{index}] parse invalid input ‘{0}’ → zero ‘{1}’")
	@CsvSource({
		"'', 0",
		"'   ', 0",
		"'abc', 0",
		"'1..23', 0",
		"'--5', 0"
	})
	void parseInvalidReturnsZero(String input, String expected) {
		BigNumber result = parser.parse(input, Locale.US);
		assertEquals(expected, result.toString());
	}

	// TODO
	@ParameterizedTest(name = "[{index}] parseAndFormat ‘{0}’ to {1} → ‘{2}’")
	@CsvSource(value = {
		"1.234,56; 1234,56",
		"1,23456; 1,23456",
		"-7.890,12; -7890,12",
		"1000,00; 1000"
	}, delimiter = ';')
	void parseAndFormatNormalizesAcrossLocales(String input, String expected) {
		BigNumber result = parser.parseAndFormat(input, Locale.GERMAN);
		assertEquals(expected, result.toString());
	}

}

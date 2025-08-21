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

package com.mlprograms.justmath.bignumber.internal;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class NumberChecker {

	/**
	 * Checks whether the given string represents a valid number according to the specified locale.
	 * This method supports different formats with thousands separators and decimal points or commas,
	 * based on the locale conventions (e.g., "1,234.56" for US, "1.234,56" for Germany).
	 *
	 * @param input
	 * 	the string to check
	 * @param locale
	 * 	the locale that determines the number format
	 *
	 * @return true if the string is a valid number in the given locale, false otherwise
	 */
	public static boolean isNumber(String input, Locale locale) {
		// Get a NumberFormat instance for the specified locale
		NumberFormat format = NumberFormat.getInstance(locale);

		// Allow both integer and decimal numbers
		format.setParseIntegerOnly(false);

		// Parse the string from the beginning
		ParsePosition pos = new ParsePosition(0);

		// Try to parse the input string
		format.parse(input.trim(), pos);

		// Parsing is valid only if the whole string was successfully consumed
		return pos.getIndex() == input.trim().length();
	}

}


package com.mlprograms.number;

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


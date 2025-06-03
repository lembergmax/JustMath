package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.api.BigNumber;
import com.mlprograms.justmath.bignumber.internal.LocalesConfig;
import com.mlprograms.justmath.bignumber.internal.NumberChecker;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class responsible for parsing numeric strings into {@link BigNumber} instances,
 * considering locale-specific grouping and decimal separators.
 * <p>
 * It supports parsing any arbitrary numeric string and normalizes it into a BigNumber in US format.
 * It also supports converting numbers from one locale format to another.
 */
public class BigNumberParser {

	/**
	 * Parses a number string using the specified locale.
	 * <p>
	 * Returns a {@link BigNumber} representing the parsed value.
	 * Returns zero if the input is null, empty, or invalid.
	 *
	 * @param input
	 * 	the raw numeric string to parse
	 * @param locale
	 * 	the locale defining grouping and decimal separators for the input string
	 *
	 * @return the parsed {@link BigNumber}
	 */
	BigNumber parse(String input, Locale locale) {
		Objects.requireNonNull(locale, "Locale must not be null");

		if (input == null || input.isBlank() || !NumberChecker.isNumber(input, locale)) {
			return defaultBigNumber();
		}

		String normalized = normalize(input.trim(), locale);
		return extractParts(normalized, locale);
	}

	/**
	 * Attempts to parse a number string using a wide range of known locales.
	 * <p>
	 * Tries to detect the correct number format by checking against common locales.
	 * Returns a {@link BigNumber} normalized to US format if parsing is successful.
	 * Falls back to zero if none match.
	 *
	 * @param input
	 * 	the raw numeric string
	 *
	 * @return parsed {@link BigNumber} or default zero BigNumber if parsing fails
	 */
	BigNumber parseAutoDetect(String input) {
		if (input == null || input.isBlank()) {
			return defaultBigNumber();
		}

		for (Locale locale : LocalesConfig.getSupportedLocales()) {
			if (NumberChecker.isNumber(input, locale)) {
				return parse(input, locale);
			}
		}

		// Fallback to zero
		return defaultBigNumber();
	}


	/**
	 * Parses a number string from one locale and returns it as a string formatted for another locale.
	 * <p>
	 * Example: parseAndFormat("1.234,56", Locale.GERMANY, Locale.US) returns "1234.56"
	 *
	 * @param input
	 * 	the raw numeric string to parse
	 * @param fromLocale
	 * 	the locale of the input string
	 * @param targetLocale
	 * 	the locale to format the output string
	 *
	 * @return a numeric string formatted for the target locale
	 */
	String parseAndFormat(String input, Locale fromLocale, Locale targetLocale) {
		BigNumber number = parse(input, fromLocale);
		return format(number, targetLocale);
	}

	/**
	 * Formats a BigNumber as a string using locale-specific grouping and decimal separators.
	 *
	 * @param number
	 * 	the BigNumber to format
	 * @param locale
	 * 	the target locale
	 *
	 * @return formatted string with grouping and locale decimal separator
	 */
	public String format(BigNumber number, Locale locale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		char decimalSeparator = symbols.getDecimalSeparator();
		char groupingSeparator = symbols.getGroupingSeparator();

		StringBuilder groupedBeforeDecimal = getGroupedBeforeDecimal(number.getValueBeforeDecimal(), groupingSeparator);

		StringBuilder result = new StringBuilder();
		if (number.isNegative()) {
			result.append("-");
		}
		result.append(groupedBeforeDecimal);

		if (number.hasDecimal()) {
			result.append(decimalSeparator).append(number.getValueAfterDecimal());
		}
		return result.toString();
	}

	/**
	 * Inserts grouping separators every 3 digits from right to left for the integer part.
	 *
	 * @param integerPart
	 * 	the string of digits before decimal
	 * @param groupingSeparator
	 * 	the grouping separator character (e.g. ',' or '.')
	 *
	 * @return string with grouping separators inserted
	 */
	private StringBuilder getGroupedBeforeDecimal(String integerPart, char groupingSeparator) {
		StringBuilder grouped = new StringBuilder();

		int len = integerPart.length();
		int count = 0;
		for (int i = len - 1; i >= 0; i--) {
			grouped.insert(0, integerPart.charAt(i));
			count++;
			if (count == 3 && i != 0) {
				grouped.insert(0, groupingSeparator);
				count = 0;
			}
		}
		return grouped;
	}


	/**
	 * Removes all grouping separators from the input string according to the locale.
	 *
	 * @param value
	 * 	the raw number string
	 * @param locale
	 * 	the locale specifying the grouping separator
	 *
	 * @return the input string without grouping separators
	 */
	private String normalize(String value, Locale locale) {
		char groupingSeparator = DecimalFormatSymbols.getInstance(locale).getGroupingSeparator();
		return value.replace(String.valueOf(groupingSeparator), "");
	}

	/**
	 * Extracts sign, integer part, and fractional part from a normalized string.
	 *
	 * @param value
	 * 	the numeric string without grouping separators
	 * @param locale
	 * 	the locale defining the decimal separator
	 *
	 * @return a {@link BigNumber} object with parsed parts
	 */
	private BigNumber extractParts(String value, Locale locale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		char decimalSeparator = symbols.getDecimalSeparator();

		boolean isNegative = value.startsWith("-");
		if (isNegative) {
			value = value.substring(1);
		}

		String[] parts = value.split(Pattern.quote(String.valueOf(decimalSeparator)), 2);
		String beforeDecimal = parts[ 0 ];
		String afterDecimal = (parts.length > 1) ? parts[ 1 ] : "0";
		boolean hasDecimal = parts.length > 1;

		return BigNumber.builder()
			       .locale(locale)
			       .valueBeforeDecimal(beforeDecimal)
			       .valueAfterDecimal(afterDecimal)
			       .isNegative(isNegative)
			       .hasDecimal(hasDecimal)
			       .build();
	}

	/**
	 * Returns a default BigNumber representing zero.
	 *
	 * @return BigNumber with zero values
	 */
	private BigNumber defaultBigNumber() {
		return BigNumber.builder()
			       .locale(Locale.US)
			       .valueBeforeDecimal("0")
			       .valueAfterDecimal("0")
			       .isNegative(false)
			       .hasDecimal(true)
			       .build();
	}

}
package com.mlprograms.number;

import com.mlprograms.locales.LocalesConfig;

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
	 * Formats a {@link BigNumber} into a string according to the specified locale.
	 * <p>
	 * This method adds grouping separators and uses the locale-specific decimal separator.
	 *
	 * @param number
	 * 	the {@link BigNumber} to format
	 * @param locale
	 * 	the locale defining grouping and decimal separators
	 *
	 * @return the formatted string representation of the number
	 */
	public String format(BigNumber number, Locale locale) {
		Objects.requireNonNull(number, "BigNumber must not be null");
		Objects.requireNonNull(locale, "Locale must not be null");

		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		char decimalSeparator = symbols.getDecimalSeparator();
		StringBuilder groupedBeforeDecimal = getGroupedBeforeDecimal(number, symbols);

		StringBuilder formattedNumber = new StringBuilder();

		if (number.isNegative()) {
			formattedNumber.append("-");
		}
		formattedNumber.append(groupedBeforeDecimal);

		if (number.hasDecimal()) {
			formattedNumber.append(decimalSeparator).append(number.getValueAfterDecimal());
		}

		return formattedNumber.toString();
	}

	/**
	 * Groups the integer part of a {@link BigNumber} using the locale-specific grouping separator.
	 * <p>
	 * Inserts grouping separators (e.g., commas or periods) every three digits from right to left,
	 * according to the provided {@link DecimalFormatSymbols}.
	 *
	 * @param number
	 * 	the {@link BigNumber} whose integer part will be grouped
	 * @param symbols
	 * 	the {@link DecimalFormatSymbols} defining the grouping separator
	 *
	 * @return a {@link StringBuilder} containing the grouped integer part
	 */
	private StringBuilder getGroupedBeforeDecimal(BigNumber number, DecimalFormatSymbols symbols) {
		char groupingSeparator = symbols.getGroupingSeparator();

		StringBuilder groupedBeforeDecimal = new StringBuilder();

		String integerPart = number.getValueBeforeDecimal();
		int length = integerPart.length();

		// Insert grouping separators every 3 digits from right to left
		int count = 0;
		for (int i = length - 1; i >= 0; i--) {
			groupedBeforeDecimal.insert(0, integerPart.charAt(i));
			count++;
			if (count == 3 && i != 0) {
				groupedBeforeDecimal.insert(0, groupingSeparator);
				count = 0;
			}
		}
		return groupedBeforeDecimal;
	}

	/**
	 * Removes all grouping separators according to the locale from the input string.
	 *
	 * @param value
	 * 	the input numeric string
	 * @param locale
	 * 	the locale defining the grouping separator
	 *
	 * @return normalized string with grouping separators removed
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
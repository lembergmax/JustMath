package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.bignumber.internal.LocalesConfig;
import com.mlprograms.justmath.bignumber.internal.NumberChecker;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;

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
	BigNumber parse(@NonNull final String input, @NonNull final Locale locale) {
		if (input.isBlank() || !NumberChecker.isNumber(input, locale)) {
			return getDefaultBigNumber();
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
	BigNumber parseAutoDetect(@NonNull final String input) {
		if (input.isBlank()) {
			return getDefaultBigNumber();
		}

		for (Locale locale : LocalesConfig.getSupportedLocales()) {
			if (NumberChecker.isNumber(input, locale)) {
				return parse(input, locale);
			}
		}

		return getDefaultBigNumber();
	}

	/**
	 * Formats a BigNumber to the targetLocale.
	 *
	 * @param number
	 * 	the BigNumber to format
	 * @param targetLocale
	 * 	the target targetLocale
	 *
	 * @return formatted string with grouping and targetLocale decimal separator
	 */
	BigNumber format(@NonNull final BigNumber number, @NonNull final Locale targetLocale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(targetLocale);
		char groupingSeparator = symbols.getGroupingSeparator();
		char decimalSeparator = symbols.getDecimalSeparator();

		String beforeDecimal = number.getValueBeforeDecimal();
		String afterDecimal = number.getValueAfterDecimal();
		boolean isNegative = number.isNegative();

		StringBuilder groupedBeforeDecimal = getGroupedBeforeDecimal(beforeDecimal, groupingSeparator);

		StringBuilder formattedNumber = new StringBuilder();
		if (isNegative) {
			formattedNumber.append("-");
		}

		formattedNumber.append(groupedBeforeDecimal);
		if (!afterDecimal.equals("0") && !afterDecimal.isEmpty()) {
			formattedNumber.append(decimalSeparator).append(afterDecimal);
		}

		return parse(formattedNumber.toString(), targetLocale);
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
	StringBuilder getGroupedBeforeDecimal(@NonNull final String integerPart, final char groupingSeparator) {
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
	 * Normalizes the input by removing grouping separators
	 * and converting the decimal separator to '.' (US format).
	 */
	private String normalize(@NonNull final String value, @NonNull final Locale fromLocale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(fromLocale);
		char groupingSeparator = symbols.getGroupingSeparator();
		char decimalSeparator = symbols.getDecimalSeparator();

		String noGrouping = value.replace(String.valueOf(groupingSeparator), "");

		return noGrouping.replace(decimalSeparator, '.');
	}

	/**
	 * Extracts the integer and fractional parts from a normalized numeric string,
	 * determines if the value is negative, and constructs a {@link BigNumber} instance.
	 *
	 * @param normalizedValue
	 * 	the numeric string in normalized (US) format, possibly starting with '-'
	 * @param originalLocale
	 * 	the locale to associate with the resulting BigNumber
	 *
	 * @return a BigNumber representing the parsed value, with correct sign and parts
	 */
	private BigNumber extractParts(@NonNull final String normalizedValue, @NonNull final Locale originalLocale) {
		String nomalizedString = normalizedValue;
		boolean isNegative = nomalizedString.startsWith("-");
		if (isNegative) {
			nomalizedString = nomalizedString.substring(1);
		}

		String[] parts = nomalizedString.split("\\.", 2);
		String beforeDecimal = parts[ 0 ];
		String afterDecimal = (parts.length > 1) ? parts[ 1 ] : "0";

		return BigNumber.builder()
			       .mathContext(BigNumbers.DEFAULT_MATH_CONTEXT)
			       .trigonometricMode(TrigonometricMode.DEG)
			       .locale(originalLocale)
			       .valueBeforeDecimal(beforeDecimal)
			       .valueAfterDecimal(afterDecimal)
			       .isNegative(isNegative)
			       .build();
	}

	/**
	 * Returns a default BigNumber representing zero.
	 *
	 * @return BigNumber with zero values
	 */
	private BigNumber getDefaultBigNumber() {
		return ZERO;
	}

}
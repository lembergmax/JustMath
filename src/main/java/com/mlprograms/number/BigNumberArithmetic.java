package com.mlprograms.number;

import java.util.Locale;

/**
 * Utility class that performs arithmetic operations on BigNumber instances.
 * <p>
 * All operations assume non-null inputs. This class handles normalization of numbers,
 * alignment of decimal places, and sign management where necessary.
 */
public final class BigNumberArithmetic {

	/**
	 * Adds two BigNumber instances.
	 * Supports addition of same-sign numbers; for different signs use subtraction logic.
	 *
	 * @param a
	 * 	first BigNumber
	 * @param b
	 * 	second BigNumber
	 *
	 * @return sum of a and b as a new BigNumber
	 *
	 * @throws UnsupportedOperationException
	 * 	if signs differ (not implemented here)
	 */
	public static BigNumber add(BigNumber a, BigNumber b) {
		if (a.isNegative() != b.isNegative()) {
			// Sign difference means addition is subtraction under the hood, not handled here
			throw new UnsupportedOperationException("Addition of numbers with different signs not implemented.");
		}

		int maxDecimalLength = Math.max(a.getValueAfterDecimal().length(), b.getValueAfterDecimal().length());

		String normalizedA = normalizeNumber(a.getValueBeforeDecimal(), a.getValueAfterDecimal(), maxDecimalLength);
		String normalizedB = normalizeNumber(b.getValueBeforeDecimal(), b.getValueAfterDecimal(), maxDecimalLength);

		String sum = addNumericStrings(normalizedA, normalizedB);

		// Split result into integer and fractional parts
		String integerPart = sum.substring(0, sum.length() - maxDecimalLength);
		String fractionalPart = sum.substring(sum.length() - maxDecimalLength);

		return BigNumber.builder()
			       .locale(a.getLocale())
			       .valueBeforeDecimal(stripLeadingZeros(integerPart))
			       .valueAfterDecimal(stripTrailingZeros(fractionalPart))
			       .hasDecimal(maxDecimalLength > 0)
			       .isNegative(a.isNegative())
			       .build();
	}

	/**
	 * Subtracts BigNumber b from a (a - b).
	 * Handles sign, borrowing, and normalization.
	 *
	 * @param a
	 * 	minuend BigNumber
	 * @param b
	 * 	subtrahend BigNumber
	 *
	 * @return difference a - b as a new BigNumber
	 */
	public static BigNumber subtract(BigNumber a, BigNumber b) {
		if (a.isNegative() != b.isNegative()) {
			return add(a, negate(b));
		}

		int compareAbs = compareAbsolute(a, b);
		if (compareAbs == 0) {
			return zero(a.getLocale());
		}

		BigNumber positive;
		BigNumber negative;
		boolean resultNegative;

		if (compareAbs > 0) {
			positive = a;
			negative = b;
			resultNegative = a.isNegative();
		} else {
			positive = b;
			negative = a;
			resultNegative = !a.isNegative();
		}

		int maxDecimalLength = Math.max(positive.getValueAfterDecimal().length(), negative.getValueAfterDecimal().length());

		String normalizedPositive = normalizeNumber(positive.getValueBeforeDecimal(), positive.getValueAfterDecimal(), maxDecimalLength);
		String normalizedNegative = normalizeNumber(negative.getValueBeforeDecimal(), negative.getValueAfterDecimal(), maxDecimalLength);

		String diff = subtractNumericStrings(normalizedPositive, normalizedNegative);

		// Fix hier: diff muss mindestens maxDecimalLength+1 lang sein
		if (diff.length() <= maxDecimalLength) {
			diff = padLeftWithZeros(diff, maxDecimalLength + 1);
		}

		String integerPart = diff.substring(0, diff.length() - maxDecimalLength);
		String fractionalPart = diff.substring(diff.length() - maxDecimalLength);

		return BigNumber.builder()
			       .locale(a.getLocale())
			       .valueBeforeDecimal(stripLeadingZeros(integerPart))
			       .valueAfterDecimal(stripTrailingZeros(fractionalPart))
			       .hasDecimal(maxDecimalLength > 0)
			       .isNegative(resultNegative)
			       .build();
	}

	/**
	 * Multiplies two BigNumber instances.
	 *
	 * @param a
	 * 	first BigNumber
	 * @param b
	 * 	second BigNumber
	 *
	 * @return product of a and b as a new BigNumber
	 */
	public static BigNumber multiply(BigNumber a, BigNumber b) {
		if (isZero(a) || isZero(b)) {
			return zero(a.getLocale());
		}

		String numA = a.getValueBeforeDecimal() + a.getValueAfterDecimal();
		String numB = b.getValueBeforeDecimal() + b.getValueAfterDecimal();

		String product = multiplyNumericStrings(numA, numB);

		int decimalPlaces = a.getValueAfterDecimal().length() + b.getValueAfterDecimal().length();

		if (product.length() <= decimalPlaces) {
			product = padLeftWithZeros(product, decimalPlaces + 1);
		}

		String integerPart = product.substring(0, product.length() - decimalPlaces);
		String fractionalPart = product.substring(product.length() - decimalPlaces);

		boolean resultNegative = a.isNegative() != b.isNegative();

		return BigNumber.builder()
			       .locale(a.getLocale())
			       .valueBeforeDecimal(stripLeadingZeros(integerPart))
			       .valueAfterDecimal(stripTrailingZeros(fractionalPart))
			       .hasDecimal(decimalPlaces > 0)
			       .isNegative(resultNegative && !isZeroFromParts(integerPart, fractionalPart))
			       .build();
	}

	private static boolean isZeroFromParts(String integerPart, String fractionalPart) {
		return stripLeadingZeros(integerPart).equals("0") && stripTrailingZeros(fractionalPart).equals("0");
	}

	/**
	 * Divides BigNumber a by b (a / b) with fixed precision.
	 * Throws if divisor is zero.
	 *
	 * @param a
	 * 	dividend BigNumber
	 * @param b
	 * 	divisor BigNumber
	 *
	 * @return quotient as a new BigNumber
	 */
	public static BigNumber divide(BigNumber a, BigNumber b) {
		if (isZero(b)) {
			throw new ArithmeticException("Division by zero");
		}

		// Prepare normalized strings without decimals
		int scaleA = a.getValueAfterDecimal().length();
		int scaleB = b.getValueAfterDecimal().length();

		String dividend = (a.getValueBeforeDecimal() + a.getValueAfterDecimal());
		String divisor = (b.getValueBeforeDecimal() + b.getValueAfterDecimal());

		// Adjust dividend by scaling to preserve decimal places after division
		int scaleDiff = scaleB - scaleA;
		if (scaleDiff > 0) {
			dividend = padRightWithZeros(dividend, dividend.length() + scaleDiff);
		} else if (scaleDiff < 0) {
			divisor = padRightWithZeros(divisor, divisor.length() - scaleDiff);
		}

		// To handle precision, append extra zeros to dividend
		int precision = 10; // fixed precision digits after decimal point
		dividend = padRightWithZeros(dividend, dividend.length() + precision);

		// Perform integer division
		String quotient = divideNumericStrings(dividend, divisor);

		// Insert decimal point precision digits from the right
		if (quotient.length() <= precision) {
			quotient = padLeftWithZeros(quotient, precision + 1);
		}

		String integerPart = quotient.substring(0, quotient.length() - precision);
		String fractionalPart = quotient.substring(quotient.length() - precision);

		boolean resultNegative = a.isNegative() != b.isNegative();

		return BigNumber.builder()
			       .locale(a.getLocale())
			       .valueBeforeDecimal(stripLeadingZeros(integerPart))
			       .valueAfterDecimal(stripTrailingZeros(fractionalPart))
			       .hasDecimal(precision > 0)
			       .isNegative(resultNegative)
			       .build();
	}

	/**
	 * Compares absolute values of two BigNumbers.
	 *
	 * @param a
	 * 	first BigNumber
	 * @param b
	 * 	second BigNumber
	 *
	 * @return positive if |a| > |b|, zero if equal, negative if |a| < |b|
	 */
	private static int compareAbsolute(BigNumber a, BigNumber b) {
		int maxDecimalLength = Math.max(a.getValueAfterDecimal().length(), b.getValueAfterDecimal().length());

		String normalizedA = normalizeNumber(a.getValueBeforeDecimal(), a.getValueAfterDecimal(), maxDecimalLength);
		String normalizedB = normalizeNumber(b.getValueBeforeDecimal(), b.getValueAfterDecimal(), maxDecimalLength);

		if (normalizedA.length() > normalizedB.length()) return 1;
		if (normalizedA.length() < normalizedB.length()) return -1;

		return normalizedA.compareTo(normalizedB);
	}

	/**
	 * Negates a BigNumber by toggling its sign.
	 *
	 * @param num
	 * 	BigNumber to negate
	 *
	 * @return new BigNumber with negated sign
	 */
	private static BigNumber negate(BigNumber num) {
		return BigNumber.builder()
			       .locale(num.getLocale())
			       .valueBeforeDecimal(num.getValueBeforeDecimal())
			       .valueAfterDecimal(num.getValueAfterDecimal())
			       .hasDecimal(num.hasDecimal())
			       .isNegative(!num.isNegative())
			       .build();
	}

	/**
	 * Checks if the BigNumber is zero.
	 *
	 * @param num
	 * 	BigNumber to check
	 *
	 * @return true if zero, else false
	 */
	private static boolean isZero(BigNumber num) {
		return stripLeadingZeros(num.getValueBeforeDecimal()).equals("0") &&
			       stripTrailingZeros(num.getValueAfterDecimal()).equals("0");
	}

	private static String normalizeNumber(String integerPart, String fractionalPart, int desiredFracLength) {
		return integerPart + padRightWithZeros(fractionalPart, desiredFracLength);
	}

	private static String padRightWithZeros(String input, int length) {
		StringBuilder sb = new StringBuilder(input);
		while (sb.length() < length) {
			sb.append('0');
		}
		return sb.toString();
	}

	private static String padLeftWithZeros(String input, int length) {
		StringBuilder sb = new StringBuilder(input);
		while (sb.length() < length) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}

	private static String stripLeadingZeros(String input) {
		int i = 0;
		while (i < input.length() - 1 && input.charAt(i) == '0') {
			i++;
		}
		return input.substring(i);
	}

	private static String stripTrailingZeros(String input) {
		int i = input.length();
		while (i > 1 && input.charAt(i - 1) == '0') {
			i--;
		}
		String result = input.substring(0, i);
		return result.isEmpty() ? "0" : result;
	}

	/**
	 * Adds two numeric strings representing non-negative integers.
	 *
	 * @param num1
	 * 	first numeric string
	 * @param num2
	 * 	second numeric string
	 *
	 * @return sum as string
	 */
	private static String addNumericStrings(String num1, String num2) {
		StringBuilder result = new StringBuilder();

		int carry = 0;
		int i = num1.length() - 1;
		int j = num2.length() - 1;

		while (i >= 0 || j >= 0 || carry > 0) {
			int digit1 = (i >= 0) ? num1.charAt(i) - '0' : 0;
			int digit2 = (j >= 0) ? num2.charAt(j) - '0' : 0;

			int sum = digit1 + digit2 + carry;
			carry = sum / 10;
			result.insert(0, sum % 10);

			i--;
			j--;
		}

		return result.toString();
	}

	/**
	 * Subtracts num2 from num1. Both are numeric strings representing non-negative integers,
	 * and num1 >= num2.
	 *
	 * @param num1
	 * 	minuend string
	 * @param num2
	 * 	subtrahend string
	 *
	 * @return difference as string
	 */
	private static String subtractNumericStrings(String num1, String num2) {
		StringBuilder result = new StringBuilder();

		int borrow = 0;
		int i = num1.length() - 1;
		int j = num2.length() - 1;

		while (i >= 0) {
			int digit1 = num1.charAt(i) - '0' - borrow;
			int digit2 = (j >= 0) ? num2.charAt(j) - '0' : 0;

			if (digit1 < digit2) {
				digit1 += 10;
				borrow = 1;
			} else {
				borrow = 0;
			}

			result.insert(0, digit1 - digit2);

			i--;
			j--;
		}

		String resStr = stripLeadingZeros(result.toString());
		return resStr.isEmpty() ? "0" : resStr;
	}

	/**
	 * Multiplies two numeric strings representing non-negative integers.
	 *
	 * @param num1
	 * 	first numeric string
	 * @param num2
	 * 	second numeric string
	 *
	 * @return product as string
	 */
	private static String multiplyNumericStrings(String num1, String num2) {
		int len1 = num1.length();
		int len2 = num2.length();
		int[] product = new int[ len1 + len2 ];

		// Multiply each digit
		for (int i = len1 - 1; i >= 0; i--) {
			int n1 = num1.charAt(i) - '0';
			for (int j = len2 - 1; j >= 0; j--) {
				int n2 = num2.charAt(j) - '0';
				int sum = n1 * n2 + product[ i + j + 1 ];
				product[ i + j + 1 ] = sum % 10;
				product[ i + j ] += sum / 10;
			}
		}

		// Convert product array to string
		StringBuilder sb = new StringBuilder();
		for (int p : product) {
			sb.append(p);
		}

		// Strip leading zeros
		while (sb.length() > 1 && sb.charAt(0) == '0') {
			sb.deleteCharAt(0);
		}

		return sb.toString();
	}

	/**
	 * Performs integer division of dividend by divisor, both numeric strings.
	 * Returns the quotient as string.
	 * Assumes divisor != "0".
	 *
	 * @param dividend
	 * 	dividend numeric string
	 * @param divisor
	 * 	divisor numeric string
	 *
	 * @return quotient numeric string
	 */
	private static String divideNumericStrings(String dividend, String divisor) {
		if (divisor.equals("0")) {
			throw new ArithmeticException("Division by zero");
		}

		if (compareStrings(dividend, divisor) < 0) {
			return "0";
		}

		StringBuilder quotient = new StringBuilder();

		String remainder = "";

		for (int i = 0; i < dividend.length(); i++) {
			remainder += dividend.charAt(i);
			remainder = stripLeadingZeros(remainder);

			int count = 0;
			while (compareStrings(remainder, divisor) >= 0) {
				remainder = subtractNumericStrings(remainder, divisor);
				count++;
			}

			quotient.append(count);
		}

		// Strip leading zeros
		return stripLeadingZeros(quotient.toString());
	}

	/**
	 * Compares two numeric strings representing non-negative integers.
	 *
	 * @param num1
	 * 	first numeric string
	 * @param num2
	 * 	second numeric string
	 *
	 * @return positive if num1 > num2, 0 if equal, negative if num1 < num2
	 */
	private static int compareStrings(String num1, String num2) {
		if (num1.length() != num2.length()) {
			return num1.length() - num2.length();
		}
		return num1.compareTo(num2);
	}

	/**
	 * Returns a zero BigNumber with given locale.
	 *
	 * @param locale
	 * 	locale to assign
	 *
	 * @return zero BigNumber
	 */
	private static BigNumber zero(Locale locale) {
		return BigNumber.builder()
			       .locale(locale)
			       .valueBeforeDecimal("0")
			       .valueAfterDecimal("0")
			       .hasDecimal(false)
			       .isNegative(false)
			       .build();
	}

}
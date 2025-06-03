package com.mlprograms.justmath.api;

import com.mlprograms.justmath.bignumber.BigNumberArithmetic;
import com.mlprograms.justmath.bignumber.BigNumberParser;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable representation of a numeric value with optional decimal part and sign.
 * <p>
 * Use the constructors which internally rely on {@link BigNumberParser} to parse input strings.
 */
@Getter
public class BigNumber {

	private final BigNumberArithmetic arithmetic = new BigNumberArithmetic();

	/**
	 * The locale defining grouping and decimal separators used by this number.
	 */
	private final Locale locale;

	/**
	 * The numeric value before the decimal separator.
	 */
	private final String valueBeforeDecimal;

	/**
	 * The numeric value after the decimal separator. Defaults to "0" if absent.
	 */
	private final String valueAfterDecimal;

	/**
	 * Indicates whether the number is negative.
	 */
	private final boolean isNegative;

	/**
	 * Indicates whether the number contains a decimal separator.
	 */
	private final boolean hasDecimal;

	/**
	 * Private builder constructor used internally.
	 */
	@Builder(access = AccessLevel.PUBLIC)
	private BigNumber(Locale locale, String valueBeforeDecimal, String valueAfterDecimal,
	                  boolean isNegative, boolean hasDecimal) {
		this.locale = Objects.requireNonNull(locale, "Locale must not be null");
		this.valueBeforeDecimal = Objects.requireNonNull(valueBeforeDecimal);
		this.valueAfterDecimal = Objects.requireNonNull(valueAfterDecimal);
		this.isNegative = isNegative;
		this.hasDecimal = hasDecimal;
	}

	/**
	 * Constructs a BigNumber by parsing the given number string using the specified locale.
	 *
	 * @param number
	 * 	the input numeric string
	 * @param locale
	 * 	the locale specifying grouping and decimal separators
	 */
	public BigNumber(String number, Locale locale) {
		this(new BigNumberParser().parse(number, locale));
	}

	/**
	 * Constructs a BigNumber by auto-detecting the locale of the input string.
	 *
	 * @param number
	 * 	the input numeric string
	 */
	public BigNumber(String number) {
		this(new BigNumberParser().parseAutoDetect(number));
	}

	/**
	 * Constructs a BigNumber by parsing the input string using a source locale
	 * and converting it to the target locale.
	 *
	 * @param input
	 * 	the input numeric string
	 * @param fromLocale
	 * 	the locale of the input string
	 * @param targetLocale
	 * 	the locale to convert/format the number to
	 */
	public BigNumber(String input, Locale fromLocale, Locale targetLocale) {
		BigNumber parsed = new BigNumberParser().parse(input, fromLocale);
		BigNumber formatted = new BigNumberParser().parse(parsed.toString(), targetLocale);
		this.locale = formatted.locale;
		this.valueBeforeDecimal = formatted.valueBeforeDecimal;
		this.valueAfterDecimal = formatted.valueAfterDecimal;
		this.isNegative = formatted.isNegative;
		this.hasDecimal = formatted.hasDecimal;
	}

	/**
	 * Constructs a BigNumber by converting an existing BigNumber to a different locale.
	 *
	 * @param number
	 * 	the original BigNumber instance
	 * @param locale
	 * 	the target locale for conversion
	 */
	public BigNumber(BigNumber number, Locale locale) {
		BigNumber converted = new BigNumberParser().parse(number.toString(), locale);
		this.locale = converted.locale;
		this.valueBeforeDecimal = converted.valueBeforeDecimal;
		this.valueAfterDecimal = converted.valueAfterDecimal;
		this.isNegative = converted.isNegative;
		this.hasDecimal = converted.hasDecimal;
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 * 	the BigNumber instance to copy
	 */
	public BigNumber(BigNumber other) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.hasDecimal = other.hasDecimal;
	}

	public BigNumber add(BigNumber other) {
		return arithmetic.add(other);
	}

	public BigNumber subtract(BigNumber other) {
		return arithmetic.subtract(other);
	}

	public BigNumber multiply(BigNumber other) {
		return arithmetic.multiply(other);
	}

	public BigNumber divide(BigNumber other) {
		return arithmetic.divide(other);
	}

	public BigNumber powerOf(BigNumber exponent) {
		return arithmetic.pow(this, exponent);
	}

	public BigNumber root() {
		return arithmetic.root(this);
	}

	public BigNumber thirdRoot() {
		return arithmetic.thirdRoot(this);
	}

	public BigNumber factorial() {
		return arithmetic.factorial(this);
	}

	public BigNumber sin() {
		return arithmetic.sin(this);
	}

	public BigNumber cos() {
		return arithmetic.cos(this);
	}

	public BigNumber tan() {
		return arithmetic.tan(this);
	}

	public BigNumber sinh() {
		return arithmetic.sinh(this);
	}

	public BigNumber cosh() {
		return arithmetic.cosh(this);
	}

	public BigNumber tanh() {
		return arithmetic.tanh(this);
	}

	public BigNumber asin() {
		return arithmetic.asin(this);
	}

	public BigNumber acos() {
		return arithmetic.acos(this);
	}

	public BigNumber atan() {
		return arithmetic.atan(this);
	}

	public BigNumber asinh() {
		return arithmetic.asinh(this);
	}

	public BigNumber acosh() {
		return arithmetic.acosh(this);
	}

	public BigNumber atanh() {
		return arithmetic.atanh(this);
	}

	public BigNumber log10() {
		return arithmetic.log10(this);
	}

	public BigNumber ln() {
		return arithmetic.ln(this);
	}

	public BigNumber logBase(int base) {
		return arithmetic.logBase(this, base);
	}

	public boolean hasDecimal() {
		return hasDecimal;
	}

	/**
	 * Returns the string representation of this number in standard US format,
	 * using '.' as decimal separator and no grouping separators.
	 *
	 * @return string representation, e.g. "-1234.56"
	 */
	@Override
	public String toString() {
		String sign = isNegative ? "-" : "";
		String decimalPart = (hasDecimal && !"0".equals(valueAfterDecimal)) ? "." + valueAfterDecimal : "";
		return sign + valueBeforeDecimal + decimalPart;
	}

}

package com.mlprograms.justmath.bignumber;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.calculator.api.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE_HUNDRED_EIGHTY;

/**
 * Immutable representation of a numeric value with optional decimal part and sign.
 * <p>
 * Use the constructors which internally rely on {@link BigNumberParser} to parse input strings.
 */
@Getter
public class BigNumber extends Number implements Comparable<BigNumber> {

	/**
	 * Shared instance of the parser used to convert input strings into BigNumber objects.
	 * This static parser ensures consistent parsing logic across all BigNumber instances.
	 */
	private static final BigNumberParser bigNumberParser = new BigNumberParser();
	/**
	 * The locale defining grouping and decimal separators used by this number.
	 */
	@NonNull
	private final Locale locale;
	/**
	 * The numeric value before the decimal separator.
	 */
	@NonNull
	private final String valueBeforeDecimal;
	/**
	 * The numeric value after the decimal separator. Defaults to "0" if absent.
	 */
	@NonNull
	private final String valueAfterDecimal;
	/**
	 * Indicates whether the number is negative.
	 */
	private final boolean isNegative;
	/**
	 * Shared instance of the CalculatorEngine used for performing arithmetic operations.
	 * This static engine allows all BigNumber instances to use the same precision settings.
	 */
	@NonNull
	private CalculatorEngine calculatorEngine;
	/**
	 * The trigonometric mode (e.g., DEG, RAD, GRAD) used for trigonometric calculations.
	 * Defaults to DEG (degrees).
	 */
	@NonNull
	private TrigonometricMode trigonometricMode = TrigonometricMode.DEG;
	/**
	 * The MathContext used for arithmetic operations, defaulting to a precision of 1000 digits.
	 */
	@NonNull
	private MathContext mathContext = new MathContext(1000);

	/**
	 * Parses the given string in the default format and constructs a {@code BigNumber}.
	 *
	 * @param number
	 * 	the numeric string to parse (auto-detected format, non-null)
	 */
	public BigNumber(@NonNull String number) {
		this(bigNumberParser.parseAutoDetect(number));
	}

	/**
	 * Parses the given string in the specified locale and constructs a {@code BigNumber}.
	 *
	 * @param number
	 * 	the numeric string to parse (non-null)
	 * @param locale
	 * 	the locale to use for parsing decimal separators, grouping, etc. (non-null)
	 */
	public BigNumber(@NonNull String number, @NonNull Locale locale) {
		this(bigNumberParser.parse(number, locale));
	}

	/**
	 * Parses the given string in the default format and constructs a {@code BigNumber}
	 * with the provided precision and rounding settings.
	 *
	 * @param number
	 * 	the numeric string to parse (auto-detected format, non-null)
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(@NonNull String number, @NonNull MathContext mathContext) {
		this(bigNumberParser.parseAutoDetect(number), mathContext);
	}

	/**
	 * Parses the given string in the specified locale and constructs a {@code BigNumber}
	 * with the provided precision and rounding settings.
	 *
	 * @param number
	 * 	the numeric string to parse (non-null)
	 * @param locale
	 * 	the locale to use for parsing decimal separators, grouping, etc. (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(@NonNull String number, @NonNull Locale locale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(number, locale), mathContext);
	}

	/**
	 * Parses the given string in one locale and then converts it to another locale's format.
	 *
	 * @param number
	 * 	the numeric string to parse (non-null)
	 * @param fromLocale
	 * 	the locale to interpret the input string (non-null)
	 * @param toLocale
	 * 	the locale to re-format the parsed value (non-null)
	 */
	public BigNumber(@NonNull String number, @NonNull Locale fromLocale, @NonNull Locale toLocale) {
		this(bigNumberParser.parse(
			bigNumberParser.parse(number, fromLocale).toString(),
			toLocale
		));
	}

	/**
	 * Parses the given string in one locale, converts it to another locale's format,
	 * and applies the provided precision/rounding settings.
	 *
	 * @param number
	 * 	the numeric string to parse (non-null)
	 * @param fromLocale
	 * 	the locale to interpret the input string (non-null)
	 * @param toLocale
	 * 	the locale to re-format the parsed value (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(@NonNull String number, @NonNull Locale fromLocale, @NonNull Locale toLocale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(
			bigNumberParser.parse(number, fromLocale).toString(),
			toLocale
		), mathContext);
	}

	/**
	 * Copy constructor: duplicates all settings (locale, value, sign, precision, mode) from the other {@code BigNumber}.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 */
	public BigNumber(@NonNull BigNumber other) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.calculatorEngine = other.calculatorEngine;
		this.trigonometricMode = other.trigonometricMode;
		this.mathContext = other.mathContext;
	}

	/**
	 * Copy constructor with overridden precision/rounding settings.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(@NonNull BigNumber other, @NonNull MathContext mathContext) {
		this(other);
		this.mathContext = mathContext;
	}

	/**
	 * Copy constructor with overridden locale.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 * @param newLocale
	 * 	the locale to apply to the copied value (non-null)
	 */
	public BigNumber(@NonNull BigNumber other, @NonNull Locale newLocale) {
		this(bigNumberParser.parse(other.toString(), newLocale));
	}

	/**
	 * Copy constructor with overridden locale and precision/rounding settings.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 * @param newLocale
	 * 	the locale to apply (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(@NonNull BigNumber other, @NonNull Locale newLocale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(other.toString(), newLocale), mathContext);
	}

	/**
	 * Constructs a {@code BigNumber} from explicitly provided internal components.
	 *
	 * @param locale
	 * 	the locale used for formatting and parsing (non-null)
	 * @param valueBeforeDecimal
	 * 	the digits before the decimal point as a string (non-null)
	 * @param valueAfterDecimal
	 * 	the digits after the decimal point as a string (non-null)
	 * @param isNegative
	 * 	whether the number is negative
	 * @param mathContext
	 * 	the precision and rounding mode to apply (non-null)
	 */
	public BigNumber(
		@NonNull Locale locale,
		@NonNull String valueBeforeDecimal,
		@NonNull String valueAfterDecimal,
		boolean isNegative,
		@NonNull MathContext mathContext
	) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.mathContext = mathContext;
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	/**
	 * Builder-style constructor allowing partial overrides of locale, value, sign, precision, and mode.
	 *
	 * @param locale
	 * 	the locale for formatting and parsing (non-null)
	 * @param valueBeforeDecimal
	 * 	the digits before the decimal point (non-null)
	 * @param valueAfterDecimal
	 * 	the digits after the decimal point (non-null)
	 * @param isNegative
	 * 	whether the number is negative
	 * @param mathContext
	 * 	the precision and rounding mode (nullable; defaults if null)
	 * @param trigonometricMode
	 * 	the angle unit mode for trigonometric operations (nullable; defaults if null)
	 */
	@Builder
	public BigNumber(
		@NonNull Locale locale,
		@NonNull String valueBeforeDecimal,
		@NonNull String valueAfterDecimal,
		boolean isNegative,
		MathContext mathContext,
		TrigonometricMode trigonometricMode
	) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		if (mathContext != null) this.mathContext = mathContext;
		if (trigonometricMode != null) this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(this.trigonometricMode);
	}

	/**
	 * Constructs a {@code BigNumber} from a {@link BigDecimal} and a locale.
	 *
	 * @param bigDecimal
	 * 	the source {@code BigDecimal} (non-null)
	 * @param locale
	 * 	the locale to apply for formatting (non-null)
	 */
	public BigNumber(@NonNull BigDecimal bigDecimal, @NonNull Locale locale) {
		this(bigDecimal.toPlainString(), locale);
	}

	/**
	 * Constructs a {@code BigNumber} from explicitly provided components and a trigonometric mode,
	 * using a default very-high-precision {@link MathContext}.
	 *
	 * @param locale
	 * 	the locale for formatting and parsing (non-null)
	 * @param valueBeforeDecimal
	 * 	digits before the decimal point (non-null)
	 * @param valueAfterDecimal
	 * 	digits after the decimal point (non-null)
	 * @param isNegative
	 * 	whether the number is negative
	 * @param trigonometricMode
	 * 	the angle unit mode (non-null)
	 */
	public BigNumber(
		@NonNull Locale locale,
		@NonNull String valueBeforeDecimal,
		@NonNull String valueAfterDecimal,
		boolean isNegative,
		@NonNull TrigonometricMode trigonometricMode
	) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.trigonometricMode = trigonometricMode;
		this.mathContext = new MathContext(1000);
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	/**
	 * Copy constructor with overridden precision and trigonometric mode.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 * @param mathContext
	 * 	the new precision and rounding mode (non-null)
	 * @param trigonometricMode
	 * 	the new angle unit mode (non-null)
	 */
	public BigNumber(
		@NonNull BigNumber other,
		@NonNull MathContext mathContext,
		@NonNull TrigonometricMode trigonometricMode
	) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.mathContext = mathContext;
		this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	/**
	 * Parses the given string in the specified locale, then constructs a {@code BigNumber}
	 * with the provided precision and trigonometric mode.
	 *
	 * @param number
	 * 	the numeric string to parse (non-null)
	 * @param locale
	 * 	the locale to use for parsing (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode (non-null)
	 * @param trigonometricMode
	 * 	the angle unit mode (non-null)
	 */
	public BigNumber(
		@NonNull String number,
		@NonNull Locale locale,
		@NonNull MathContext mathContext,
		@NonNull TrigonometricMode trigonometricMode
	) {
		this(bigNumberParser.parse(number, locale), mathContext, trigonometricMode);
	}

	/**
	 * Parses and locale-converts from another {@code BigNumber}, then applies precision and mode.
	 *
	 * @param other
	 * 	the instance to copy (non-null)
	 * @param newLocale
	 * 	the locale to apply (non-null)
	 * @param mathContext
	 * 	the precision and rounding mode (non-null)
	 * @param trigonometricMode
	 * 	the angle unit mode (non-null)
	 */
	public BigNumber(
		@NonNull BigNumber other,
		@NonNull Locale newLocale,
		@NonNull MathContext mathContext,
		@NonNull TrigonometricMode trigonometricMode
	) {
		this(bigNumberParser.parse(other.toString(), newLocale), mathContext, trigonometricMode);
	}

	/**
	 * Constructs a zero-valued {@code BigNumber} in the specified locale with default high precision.
	 *
	 * @param locale
	 * 	the locale to apply for formatting (non-null)
	 */
	public BigNumber(@NonNull Locale locale) {
		this.locale = locale;
		this.valueBeforeDecimal = "0";
		this.valueAfterDecimal = "0";
		this.isNegative = false;
		this.mathContext = new MathContext(1000);
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	/**
	 * Constructs a {@code BigNumber} directly from a {@link BigDecimal} using the default locale.
	 *
	 * @param bigDecimal
	 * 	the source {@code BigDecimal} (non-null)
	 */
	public BigNumber(@NonNull BigDecimal bigDecimal) {
		this(bigDecimal, Locale.getDefault());
	}

	/**
	 * Adds the specified {@link BigNumber} to this number.
	 *
	 * <p>This method converts both operands to {@link BigDecimal}, performs the addition using
	 * {@link BigDecimal#add(BigDecimal)}, and wraps the result in a new {@code BigNumber}.</p>
	 *
	 * @param other
	 * 	the number to add to this number
	 *
	 * @return a new {@code BigNumber} representing the sum
	 */
	public BigNumber add(BigNumber other) {
		return new BigNumber(toBigDecimal().add(other.toBigDecimal()).toPlainString());
	}

	/**
	 * Subtracts the specified {@link BigNumber} from this number.
	 *
	 * <p>The method converts the operand to a {@link BigDecimal} and performs the subtraction using
	 * {@link BigDecimal#subtract(BigDecimal)}. The result is then wrapped in a new {@code BigNumber}.</p>
	 *
	 * @param other
	 * 	the number to subtract from this number
	 *
	 * @return a new {@code BigNumber} representing the difference
	 */
	public BigNumber subtract(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().subtract(bigDecimal).toPlainString());
	}

	/**
	 * Multiplies this number by the specified {@link BigNumber}.
	 *
	 * <p>Converts the operand to a {@link BigDecimal}, multiplies it with this number using
	 * {@link BigDecimal#multiply(BigDecimal)}, and returns the result as a new {@code BigNumber}.</p>
	 *
	 * @param other
	 * 	the number to multiply with this number
	 *
	 * @return a new {@code BigNumber} representing the product
	 */
	public BigNumber multiply(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().multiply(bigDecimal).toPlainString());
	}

	/**
	 * Divides this {@code BigNumber} by the specified {@code BigNumber}
	 * using the default {@link MathContext} configured in this instance.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #divide(BigNumber, MathContext)} using the instance's default context.</p>
	 *
	 * @param other
	 * 	the divisor
	 *
	 * @return a new {@code BigNumber} representing the result of the division
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(BigNumber other) {
		return divide(other, mathContext);
	}

	/**
	 * Divides this number by the specified {@link BigNumber} using the provided {@link MathContext}.
	 *
	 * <p>Performs division with the specified precision and rounding mode, using
	 * {@link BigDecimal#divide(BigDecimal, MathContext)}.
	 * The result is wrapped in a new {@code BigNumber}.</p>
	 *
	 * @param other
	 * 	the divisor
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the quotient
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(BigNumber other, MathContext mathContext) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().divide(bigDecimal, mathContext).toPlainString());
	}

	/**
	 * Raises this {@code BigNumber} to the specified exponent using the default
	 * {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * <p>This method delegates to {@link #power(BigNumber, MathContext, Locale)}
	 * using the default settings.</p>
	 *
	 * @param exponent
	 * 	the exponent
	 *
	 * @return a new {@code BigNumber} representing the result of the exponentiation
	 */
	public BigNumber power(BigNumber exponent) {
		return power(exponent, mathContext, locale);
	}

	/**
	 * Raises this number to the power of the specified {@link BigNumber} exponent using the given {@link MathContext}.
	 *
	 * <p>This method delegates the computation to {@link BigDecimalMath#pow(BigDecimal, BigDecimal, MathContext)}
	 * for high-precision exponentiation, and returns the result as a localized {@code BigNumber}.</p>
	 *
	 * @param exponent
	 * 	the exponent
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale to apply for formatting or localization (if relevant)
	 *
	 * @return a new {@code BigNumber} representing the result of the exponentiation
	 */
	public BigNumber power(BigNumber exponent, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.pow(toBigDecimal(), new BigDecimal(exponent.toString()), mathContext).toPlainString(), locale);
	}

	/**
	 * Computes the square root of this {@code BigNumber} using the default
	 * {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * <p>This method delegates to {@link #squareRoot(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing the square root
	 */
	public BigNumber squareRoot() {
		return squareRoot(mathContext, locale);
	}

	/**
	 * Computes the square root of this number using the specified {@link MathContext}.
	 *
	 * <p>This method delegates to {@link BigDecimalMath#root(BigDecimal, BigDecimal, MathContext)} with 2 as the root,
	 * and returns the localized result.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale to apply for formatting or localization (if relevant)
	 *
	 * @return a new {@code BigNumber} representing the square root
	 */
	public BigNumber squareRoot(MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), BigDecimal.TWO, mathContext).toPlainString(), locale);
	}

	/**
	 * Computes the cube root of this {@code BigNumber} using the default
	 * {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * <p>This method delegates to {@link #cubicRoot(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing the cube root
	 */
	public BigNumber cubicRoot() {
		return cubicRoot(mathContext, locale);
	}

	/**
	 * Computes the cube root of this number using the specified {@link MathContext}.
	 *
	 * <p>This method uses {@link BigDecimalMath#root(BigDecimal, BigDecimal, MathContext)} with 3 as the root,
	 * and returns the result in a new {@code BigNumber} with localization support.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale to apply for formatting or localization (if relevant)
	 *
	 * @return a new {@code BigNumber} representing the cube root
	 */
	public BigNumber cubicRoot(MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), new BigDecimal("3"), mathContext).toPlainString(), locale);
	}

	/**
	 * Computes the <i>n</i>th root of this {@code BigNumber} using the default
	 * {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * <p>This method delegates to {@link #nthRoot(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param n
	 * 	the degree of the root (must be non-negative)
	 *
	 * @return a new {@code BigNumber} representing the <i>n</i>th root
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} is negative
	 */
	public BigNumber nthRoot(BigNumber n) {
		return nthRoot(n, mathContext, locale);
	}

	/**
	 * Computes the <i>n</i>th root of this number using the specified {@link MathContext}.
	 *
	 * <p>If {@code n} is negative, an {@link IllegalArgumentException} is thrown.
	 * Otherwise, the root is calculated via {@link BigDecimalMath#root(BigDecimal, BigDecimal, MathContext)}.</p>
	 *
	 * @param n
	 * 	the degree of the root (must be non-negative)
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale to apply for formatting or localization (if relevant)
	 *
	 * @return a new {@code BigNumber} representing the <i>n</i>th root
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} is negative
	 */
	public BigNumber nthRoot(BigNumber n, MathContext mathContext, Locale locale) {
		if (n.isNegative()) {
			throw new IllegalArgumentException("Cannot calculate nth root with negative n.");
		}
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), n.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Computes the factorial of this {@code BigNumber} using the default
	 * {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * <p>This method delegates to {@link #factorial(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing the factorial
	 *
	 * @throws ArithmeticException
	 * 	if the number is not a non-negative integer or too large for computation
	 */
	public BigNumber factorial() {
		return factorial(mathContext, locale);
	}

	/**
	 * Computes the factorial of this number using the specified {@link MathContext}.
	 *
	 * <p>This method uses {@link BigDecimalMath#factorial(BigDecimal, MathContext)} to compute the factorial
	 * with arbitrary precision and returns the result as a localized {@code BigNumber}.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale to apply for formatting or localization (if relevant)
	 *
	 * @return a new {@code BigNumber} representing the factorial
	 */
	public BigNumber factorial(MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.factorial(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Computes the base-2 logarithm of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #log2(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing log₂(this)
	 */
	public BigNumber log2() {
		return log2(mathContext, locale);
	}

	/**
	 * Computes the base-2 logarithm of this number with the given precision and locale.
	 *
	 * <p>This method delegates to {@link BigDecimalMath#log2(BigDecimal, MathContext)} for the computation,
	 * then wraps the result in a new {@code BigNumber} with the specified {@code locale}.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing log₂(this)
	 */
	public BigNumber log2(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.log2(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the base-10 logarithm of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #log10(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing log₁₀(this)
	 */
	public BigNumber log10() {
		return log10(mathContext, locale);
	}

	/**
	 * Computes the base-10 logarithm of this number with the given precision and locale.
	 *
	 * <p>This method delegates to {@link BigDecimalMath#log10(BigDecimal, MathContext)} for the computation,
	 * then wraps the result in a new {@code BigNumber} with the specified {@code locale}.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing log₁₀(this)
	 */
	public BigNumber log10(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.log10(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the natural logarithm (base e) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #ln(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing ln(this)
	 */
	public BigNumber ln() {
		return ln(mathContext, locale);
	}

	/**
	 * Computes the natural logarithm (base e) of this number with the given precision and locale.
	 *
	 * <p>This method delegates to {@link BigDecimalMath#log(BigDecimal, MathContext)} for the computation,
	 * then wraps the result in a new {@code BigNumber} with the specified {@code locale}.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing ln(this)
	 */
	public BigNumber ln(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.log(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the logarithm of this number in the given base using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #logBase(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param base
	 * 	the logarithm base (must be > 0)
	 *
	 * @return a new {@code BigNumber} representing log₍base₎(this)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code base} is zero or negative
	 */
	public BigNumber logBase(BigNumber base) {
		return logBase(base, mathContext, locale);
	}

	/**
	 * Computes the logarithm of this number in an arbitrary positive base.
	 *
	 * <p>Performs: log_base(this) = ln(this) / ln(base), using {@link BigDecimalMath#log} for
	 * each natural logarithm. Throws if {@code base} is zero or negative.</p>
	 *
	 * @param base
	 * 	the logarithm base (must be > 0)
	 * @param mathContext
	 * 	the precision and rounding settings for both ln calculations and division
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing log₍base₎(this)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code base} is zero or negative
	 */
	public BigNumber logBase(BigNumber base, MathContext mathContext, Locale locale) {
		if (base.isNegative() || base.isEqualsTo(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("Base must be positive and non-zero.");
		}
		BigDecimal lnThis = BigDecimalMath.log(toBigDecimal(), mathContext);
		BigDecimal lnBase = BigDecimalMath.log(base.toBigDecimal(), mathContext);
		return new BigNumber(
			lnThis.divide(lnBase, mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the sine of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #sin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing sin(this)
	 */
	public BigNumber sin() {
		return sin(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the sine of this number using the specified {@link TrigonometricMode} and the default {@link MathContext}
	 * and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #sin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the input is in degrees or radians
	 *
	 * @return a new {@code BigNumber} representing sin(this)
	 */
	public BigNumber sin(TrigonometricMode trigonometricMode) {
		return sin(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the sine of this number, interpreting the input in degrees or radians as specified.
	 *
	 * <p>If {@code trigonometricMode} is {@link TrigonometricMode#DEG}, the input is converted:
	 * radians = this × π / 180. Uses {@link BigDecimalMath#sin} for the calculation.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether the input is in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing sin(this)
	 */
	public BigNumber sin(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = (trigonometricMode == TrigonometricMode.DEG)
			                     ? toBigDecimal().multiply(BigDecimalMath.pi(mathContext))
				                       .divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext)
			                     : toBigDecimal();
		return new BigNumber(
			BigDecimalMath.sin(radians, mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the cosine of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cos(this)
	 */
	public BigNumber cos() {
		return cos(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the cosine of this number using the specified {@link TrigonometricMode} and the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the input is in degrees or radians
	 *
	 * @return a new {@code BigNumber} representing cos(this)
	 */
	public BigNumber cos(TrigonometricMode trigonometricMode) {
		return cos(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the cosine of this number, interpreting the input in degrees or radians as specified.
	 *
	 * <p>If {@code trigonometricMode} is {@link TrigonometricMode#DEG}, converts input to radians.
	 * Uses {@link BigDecimalMath#cos} for the calculation.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether the input is in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing cos(this)
	 */
	public BigNumber cos(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = (trigonometricMode == TrigonometricMode.DEG)
			                     ? toBigDecimal().multiply(BigDecimalMath.pi(mathContext))
				                       .divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext)
			                     : toBigDecimal();
		return new BigNumber(
			BigDecimalMath.cos(radians, mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the tangent of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #tan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing tan(this)
	 */
	public BigNumber tan() {
		return tan(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the tangent of this number in the specified angle unit using the default {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #tan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the input is in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing tan(this)
	 */
	public BigNumber tan(TrigonometricMode trigonometricMode) {
		return tan(mathContext, trigonometricMode, locale);
	}


	/**
	 * Computes the tangent of this number, interpreting the input in degrees or radians as specified.
	 *
	 * <p>If {@code trigonometricMode} is {@link TrigonometricMode#DEG}, converts input to radians.
	 * Uses {@link BigDecimalMath#tan} for the calculation.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether the input is in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing tan(this)
	 */
	public BigNumber tan(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = (trigonometricMode == TrigonometricMode.DEG)
			                     ? toBigDecimal().multiply(BigDecimalMath.pi(mathContext))
				                       .divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext)
			                     : toBigDecimal();
		return new BigNumber(
			BigDecimalMath.tan(radians, mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the hyperbolic sine of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #sinh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing sinh(this)
	 */
	public BigNumber sinh() {
		return sinh(mathContext, locale);
	}

	/**
	 * Computes the hyperbolic sine (sinh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#sinh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing sinh(this)
	 */
	public BigNumber sinh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.sinh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the hyperbolic cosine of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cosh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cosh(this)
	 */
	public BigNumber cosh() {
		return cosh(mathContext, locale);
	}

	/**
	 * Computes the hyperbolic cosine (cosh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#cosh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing cosh(this)
	 */
	public BigNumber cosh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.cosh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the hyperbolic tangent of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #tanh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing tanh(this)
	 */
	public BigNumber tanh() {
		return tanh(mathContext, locale);
	}

	/**
	 * Computes the hyperbolic tangent (tanh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#tanh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing tanh(this)
	 */
	public BigNumber tanh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.tanh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the inverse sine (arcsin) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #asin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing asin(this)
	 */
	public BigNumber asin() {
		return asin(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse sine (arcsin) of this number in the specified angle unit using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #asin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing asin(this)
	 */
	public BigNumber asin(TrigonometricMode trigonometricMode) {
		return asin(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse sine (arcsin) of this number with specified units and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#asin} to get radians. If {@code DEG} mode is selected,
	 * converts the result: degrees = radians × 180 / π.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether to return result in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing asin(this)
	 */
	public BigNumber asin(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.asin(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal())
				         .divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Computes the inverse cosine (arccos) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acos(this)
	 */
	public BigNumber acos() {
		return acos(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse cosine (arccos) of this number in the specified angle unit using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing acos(this)
	 */
	public BigNumber acos(TrigonometricMode trigonometricMode) {
		return acos(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse cosine (arccos) of this number with specified units and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#acos} to get radians. If {@code DEG} mode is selected,
	 * converts the result: degrees = radians × 180 / π.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether to return result in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing acos(this)
	 */
	public BigNumber acos(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.acos(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal())
				         .divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Computes the inverse tangent (arctan) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing atan(this)
	 */
	public BigNumber atan() {
		return atan(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse tangent (arctan) of this number in the specified angle unit using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing atan(this)
	 */
	public BigNumber atan(TrigonometricMode trigonometricMode) {
		return atan(mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse tangent (arctan) of this number with specified units and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#atan} to get radians. If {@code DEG} mode is selected,
	 * converts the result: degrees = radians × 180 / π.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param trigonometricMode
	 * 	whether to return result in degrees ({@code DEG}) or radians ({@code RAD})
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing atan(this)
	 */
	public BigNumber atan(MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.atan(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal())
				         .divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Computes the inverse hyperbolic sine (asinh) of this number using the default {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #asinh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing asinh(this)
	 */
	public BigNumber asinh() {
		return asinh(mathContext, locale);
	}

	/**
	 * Computes the inverse hyperbolic sine (asinh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#asinh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing asinh(this)
	 */
	public BigNumber asinh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.asinh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the inverse hyperbolic cosine (acosh) of this number using the default {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acosh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acosh(this)
	 */
	public BigNumber acosh() {
		return acosh(mathContext, locale);
	}

	/**
	 * Computes the inverse hyperbolic cosine (acosh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#acosh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing acosh(this)
	 */
	public BigNumber acosh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.acosh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the inverse hyperbolic tangent (atanh) of this number using the default {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atanh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing atanh(this)
	 */
	public BigNumber atanh() {
		return atanh(mathContext, locale);
	}

	/**
	 * Computes the inverse hyperbolic tangent (atanh) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#atanh} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing atanh(this)
	 */
	public BigNumber atanh(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.atanh(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the two-argument arctangent of this number and the specified other number using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan2(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param other
	 * 	the second coordinate for atan2(this, other)
	 *
	 * @return a new {@code BigNumber} representing atan2(this, other)
	 */
	public BigNumber atan2(BigNumber other) {
		return atan2(other, mathContext, locale);
	}

	/**
	 * Computes the two-argument arctangent of this number and the specified other number.
	 *
	 * <p>Delegates to {@link BigDecimalMath#atan2(BigDecimal, BigDecimal, MathContext)} for quadrant-aware result,
	 * then wraps the result in a new {@code BigNumber} with the specified {@code locale}.</p>
	 *
	 * @param other
	 * 	the y-coordinate component for atan2(this, other)
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing atan2(this, other)
	 */
	public BigNumber atan2(BigNumber other, MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.atan2(toBigDecimal(), other.toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the cotangent (cot) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cot(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cot(this)
	 */
	public BigNumber cot() {
		return cot(mathContext, locale);
	}

	/**
	 * Computes the cotangent of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#cot} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing cot(this)
	 */
	public BigNumber cot(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.cot(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the inverse cotangent (arccot) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acot(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acot(this)
	 */
	public BigNumber acot() {
		return acot(mathContext, locale);
	}

	/**
	 * Computes the inverse cotangent (arccot) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#acot} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing acot(this)
	 */
	public BigNumber acot(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.acot(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the inverse hyperbolic cotangent (acoth) of this number using the default {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acoth(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acoth(this)
	 */
	public BigNumber acoth() {
		return acoth(mathContext, locale);
	}

	/**
	 * Computes the inverse hyperbolic cotangent (acoth) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#acoth} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing acoth(this)
	 */
	public BigNumber acoth(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.acoth(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Computes the hyperbolic cotangent (coth) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #coth(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing coth(this)
	 */
	public BigNumber coth() {
		return coth(mathContext, locale);
	}

	/**
	 * Computes the hyperbolic cotangent (coth) of this number with the given precision and locale.
	 *
	 * <p>Delegates to {@link BigDecimalMath#coth} and wraps the result.</p>
	 *
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing coth(this)
	 */
	public BigNumber coth(MathContext mathContext, Locale locale) {
		return new BigNumber(
			BigDecimalMath.coth(toBigDecimal(), mathContext).toPlainString(),
			locale
		);
	}

	/**
	 * Calculates the number of combinations (n choose k) using the default {@link MathContext}.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #combination(BigNumber, MathContext)}.</p>
	 *
	 * @param k
	 * 	the number of items to choose (must be between 0 and n inclusive)
	 *
	 * @return a {@code BigNumber} representing C(n, k)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either number has a decimal part
	 */
	public BigNumber combination(BigNumber k) {
		return combination(k, mathContext);
	}

	/**
	 * Calculates the number of combinations (also known as "n choose k").
	 * <p>
	 * A combination is the number of ways to choose {@code k} elements from a set of {@code n} elements (this object),
	 * regardless of order. It is denoted as {@code C(n, k)} or {@code nCr} and defined as:
	 *
	 * <pre>
	 *     C(n, k) = n! / (k! * (n - k)!)
	 * </pre>
	 *
	 * <p>This implementation uses an iterative method to avoid computing large factorials directly,
	 * which greatly improves performance and reduces risk of overflow.
	 * The result is always a non-negative integer value.
	 *
	 * @param k
	 * 	the number of items to choose (must be between 0 and n inclusive)
	 * @param mathContext
	 * 	the context for precision and rounding
	 *
	 * @return the number of combinations (n choose k) as a {@code BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either number has a decimal part
	 */
	public BigNumber combination(BigNumber k, MathContext mathContext) {
		if (hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Combination requires integer values for both n and k.");
		}

		if (k.compareTo(this) > 0) {
			throw new IllegalArgumentException("Cannot calculate combinations: k cannot be greater than n.");
		}

		if (k.isEqualsTo(BigNumbers.ZERO) || k.isEqualsTo(this)) {
			return BigNumbers.ONE;
		}

		k = k.min(subtract(k));
		BigNumber c = BigNumbers.ONE;
		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(k); i = i.add(BigNumbers.ONE)) {
			c = c.multiply(subtract(i)).divide(i.add(BigNumbers.ONE), mathContext);
		}

		return c;
	}

	/**
	 * Calculates the number of permutations (nPk) using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #permutation(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param k
	 * 	the number of elements to select and order (must be between 0 and n inclusive)
	 *
	 * @return a {@code BigNumber} representing P(n, k)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either value has a decimal part
	 */
	public BigNumber permutation(BigNumber k) {
		return permutation(k, mathContext, locale);
	}

	/**
	 * Calculates the number of permutations of {@code k} elements from a set of {@code n} elements (this object).
	 * <p>
	 * A permutation considers both selection and order. It is defined as:
	 *
	 * <pre>
	 *     P(n, k) = n! / (n - k)!
	 * </pre>
	 *
	 * <p>This method uses factorial calculations and respects the specified {@link Locale}
	 * for any locale-sensitive formatting inside internal calculations.
	 *
	 * @param k
	 * 	the number of elements to select and order (must be between 0 and n inclusive)
	 * @param mathContext
	 * 	the context for precision and rounding
	 * @param locale
	 * 	the locale to use for formatting or parsing
	 *
	 * @return the number of permutations (nPk) as a {@code BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either value has a decimal part
	 */
	public BigNumber permutation(BigNumber k, MathContext mathContext, Locale locale) {
		if (hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Permutations requires integer values for both n and k.");
		}

		if (k.compareTo(this) > 0) {
			throw new IllegalArgumentException("Cannot calculate permutations: k cannot be greater than n.");
		}

		BigNumber nFactorial = factorial(mathContext, locale);
		BigNumber nMinusKFactorial = subtract(k).factorial(mathContext, locale);
		return nFactorial.divide(nMinusKFactorial, mathContext);
	}

	/**
	 * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y) using the default settings.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #polarToCartesianCoordinates(BigNumber, MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param theta
	 * 	the angle (in degrees or radians, depending on this object's {@code trigonometricMode})
	 *
	 * @return a {@code BigNumberCoordinate} representing (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(BigNumber theta) {
		return polarToCartesianCoordinates(theta, mathContext, trigonometricMode, locale);
	}

	/**
	 * Converts polar coordinates to Cartesian coordinates.
	 * <p>
	 * This object is interpreted as the radius {@code r}, and {@code theta} is the angle.
	 * The result is a coordinate pair (x, y), where:
	 *
	 * <pre>
	 *     x = r * cos(theta)
	 *     y = r * sin(theta)
	 * </pre>
	 * <p>
	 * The angle {@code theta} is interpreted based on the provided {@link TrigonometricMode} (degrees or radians).
	 *
	 * @param theta
	 * 	the angle (in degrees or radians, depending on {@code trigonometricMode})
	 * @param mathContext
	 * 	the context for precision and rounding
	 * @param trigonometricMode
	 * 	whether the angle is in degrees or radians
	 * @param locale
	 * 	the locale for formatting or localization (used internally)
	 *
	 * @return a {@code BigNumberCoordinate} representing the Cartesian coordinates (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(BigNumber theta, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber x = multiply(theta.cos(mathContext, trigonometricMode, locale));
		BigNumber y = multiply(theta.sin(mathContext, trigonometricMode, locale));
		return new BigNumberCoordinate(x, y);
	}

	/**
	 * Converts Cartesian coordinates (x, y) to polar coordinates (r, θ in degrees) using the default settings.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #cartesianToPolarCoordinates(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param y
	 * 	the y-coordinate
	 *
	 * @return a {@code BigNumberCoordinate} representing (r, θ) with θ in degrees
	 */
	public BigNumberCoordinate cartesianToPolarCoordinates(BigNumber y) {
		return cartesianToPolarCoordinates(y, mathContext, locale);
	}

	/**
	 * Converts Cartesian coordinates to polar coordinates.
	 * <p>
	 * This object represents the x-coordinate, and the parameter {@code y} is the y-coordinate.
	 * The result is a polar coordinate pair (r, theta), where:
	 *
	 * <pre>
	 *     r     = √(x² + y²)
	 *     theta = atan2(y, x)
	 * </pre>
	 * <p>
	 * The returned angle {@code theta} is converted to degrees.
	 *
	 * @param y
	 * 	the y-coordinate
	 * @param mathContext
	 * 	the context for precision and rounding
	 * @param locale
	 * 	the locale for formatting or localization (used internally)
	 *
	 * @return a {@code BigNumberCoordinate} representing the polar coordinates (r, theta in degrees)
	 */
	public BigNumberCoordinate cartesianToPolarCoordinates(BigNumber y, MathContext mathContext, Locale locale) {
		BigNumber r = power(BigNumbers.TWO, mathContext, locale).add(y.power(BigNumbers.TWO, mathContext, locale)).squareRoot(mathContext, locale);
		BigNumber theta = y.atan2(this, mathContext, locale);
		BigNumber thetaDeg = theta.toDegrees();

		return new BigNumberCoordinate(r, thetaDeg);
	}

	/**
	 * Parses this BigNumber into a new targetLocale and mutates the current object.
	 *
	 * @param targetLocale
	 * 	the new targetLocale to apply
	 */
	public BigNumber formatToLocale(@NonNull Locale targetLocale) {
		return bigNumberParser.format(this, targetLocale);
	}

	/**
	 * Returns a new BigNumber with grouping separators applied to the integer part,
	 * according to the current locale's grouping separator.
	 * <br><br>
	 * If you want to set format the number in a specific locale, use {@link #formatToLocale(Locale)} first and then this
	 * method.
	 *
	 * @return a BigNumber with grouped valueBeforeDecimal
	 */
	public BigNumber formatWithGrouping() {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		char groupingSeparator = symbols.getGroupingSeparator();

		return BigNumber.builder()
			       .locale(this.locale)
			       .valueBeforeDecimal(bigNumberParser.getGroupedBeforeDecimal(valueBeforeDecimal, groupingSeparator).toString())
			       .valueAfterDecimal(this.valueAfterDecimal)
			       .isNegative(this.isNegative)
			       .build();
	}

	/**
	 * Sets the precision for the division in the CalculatorEngine used by this BigNumber. But be careful, because this
	 * will overwrite
	 * the current CalculatorEngine instance.
	 *
	 * @param precision
	 * 	the number of decimal places to use in calculations
	 */
	public void setCalculatorEngineDivisionPrecision(int precision) {
		this.calculatorEngine = new CalculatorEngine(precision);
	}

	/**
	 * Sets the CalculatorEngine instance for this BigNumber.
	 *
	 * @param calculatorEngine
	 * 	the CalculatorEngine to use; must not be null
	 *
	 * @throws NullPointerException
	 * 	if calculatorEngine is null
	 */
	public void setCalculatorEngine(@NonNull CalculatorEngine calculatorEngine) {
		this.calculatorEngine = calculatorEngine;
	}

	/**
	 * Sets the trigonometric mode for this BigNumber instance.
	 *
	 * @param trigonometricMode
	 * 	the trigonometric mode to set (e.g., DEG, RAD)
	 */
	public void setTrigonometricMode(@NonNull TrigonometricMode trigonometricMode) {
		this.trigonometricMode = trigonometricMode;
	}

	/**
	 * Converts this BigNumber to a BigDecimal using its string representation.
	 * <p>
	 * Note: The conversion uses the current locale's formatting, so ensure the string
	 * representation is compatible with BigDecimal parsing (typically US/standard format).
	 *
	 * @return a BigDecimal representation of this BigNumber
	 */
	private BigDecimal toBigDecimal() {
		return new BigDecimal(this.toString());
	}

	/**
	 * Converts this BigNumber from radians to degrees.
	 *
	 * @return a new BigNumber representing the value in degrees
	 */
	public BigNumber toDegrees() {
		return new BigNumber(
			BigDecimalMath.toDegrees(toBigDecimal(), mathContext).toPlainString(),
			locale, mathContext
		);
	}

	/**
	 * Converts this BigNumber from degrees to radians.
	 *
	 * @return a new BigNumber representing the value in radians
	 */
	public BigNumber toRadians() {
		return new BigNumber(
			BigDecimalMath.toRadians(toBigDecimal(), mathContext).toPlainString(),
			locale, mathContext
		);
	}

	/**
	 * Compares this BigNumber to another for equality.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if both numbers are equal, false otherwise
	 */
	public boolean isEqualsTo(BigNumber other) {
		if (other == null) {
			return false;
		}

		return this.toBigDecimal().compareTo(other.toBigDecimal()) == 0;
	}

	/**
	 * Checks if this BigNumber is less than the specified BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if this is less than other, false otherwise
	 */
	public boolean isLessThan(BigNumber other) {
		if (other == null) {
			return false;
		}

		return this.toBigDecimal().compareTo(other.toBigDecimal()) < 0;
	}

	/**
	 * Checks if this BigNumber is less than or equal to the specified BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if this is less than or equal to other, false otherwise
	 */
	public boolean isLessThanOrEqualsTo(BigNumber other) {
		return this.isLessThan(other) || this.isEqualsTo(other);
	}

	/**
	 * Checks if this BigNumber is greater than the specified BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if this is greater than other, false otherwise
	 */
	public boolean isGreaterThan(BigNumber other) {
		if (other == null) {
			return false;
		}

		return this.toBigDecimal().compareTo(other.toBigDecimal()) > 0;
	}

	/**
	 * Checks if this BigNumber is greater than or equal to the specified BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if this is greater than or equal to other, false otherwise
	 */
	public boolean isGreaterThanOrEqualsTo(BigNumber other) {
		return this.isGreaterThan(other) || this.isEqualsTo(other);
	}

	@Override
	public int intValue() {
		String numStr = valueBeforeDecimal.length() > 10 ? valueBeforeDecimal.substring(0, 10) : valueBeforeDecimal;
		int result;
		try {
			result = Integer.parseInt(numStr);
		} catch (NumberFormatException e) {
			result = Integer.MAX_VALUE;
		}
		return isNegative ? -result : result;
	}

	@Override
	public long longValue() {
		String numStr = valueBeforeDecimal.length() > 19 ? valueBeforeDecimal.substring(0, 19) : valueBeforeDecimal;
		long result;
		try {
			result = Long.parseLong(numStr);
		} catch (NumberFormatException e) {
			result = Long.MAX_VALUE;
		}
		return isNegative ? -result : result;
	}

	@Override
	public float floatValue() {
		try {
			java.math.BigDecimal bd = new java.math.BigDecimal((isNegative ? "-" : "") + valueBeforeDecimal);
			return bd.floatValue();
		} catch (NumberFormatException e) {
			return isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		}
	}

	@Override
	public double doubleValue() {
		try {
			java.math.BigDecimal bd = new java.math.BigDecimal((isNegative ? "-" : "") + valueBeforeDecimal);
			return bd.doubleValue();
		} catch (NumberFormatException e) {
			return isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Returns the minimum of this BigNumber and the specified other BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return the smaller of this and other; if other is null, returns this
	 */
	public BigNumber min(BigNumber other) {
		if (other == null) {
			return this;
		}
		return this.isLessThan(other) ? this : other;
	}

	/**
	 * Returns the maximum of this BigNumber and the specified other BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return the greater of this and other; if other is null, returns this
	 */
	public BigNumber max(BigNumber other) {
		if (other == null) {
			return this;
		}
		return this.isGreaterThan(other) ? this : other;
	}

	/**
	 * Checks if this BigNumber has a non-zero and non-empty decimal part.
	 *
	 * @return true if there are decimals, false otherwise
	 */
	public boolean hasDecimals() {
		return !valueAfterDecimal.equals("0") && !valueAfterDecimal.isEmpty();
	}

	/**
	 * Returns the string representation of this number in standard US format,
	 * using '.' as decimal separator and no grouping separators.
	 *
	 * @return string representation, e.g. "-1234.56"
	 */
	@Override
	public String toString() {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		String decimalSeparator = String.valueOf(symbols.getDecimalSeparator());
		String newValueAfterDecimal;

		if (valueAfterDecimal.isBlank() || valueAfterDecimal.equals("0")) {
			newValueAfterDecimal = "";
			decimalSeparator = "";
		} else {
			newValueAfterDecimal = valueAfterDecimal;
		}

		String localized = valueBeforeDecimal + decimalSeparator + newValueAfterDecimal;
		return isNegative ? "-" + localized : localized;
	}

	@Override
	public int compareTo(BigNumber other) {
		return this.toBigDecimal().compareTo(other.toBigDecimal());
	}

}

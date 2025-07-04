package com.mlprograms.justmath.bignumber;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.math.*;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumberValues.DEFAULT_MATH_CONTEXT;
import static com.mlprograms.justmath.bignumber.BigNumberValues.ONE_HUNDRED_EIGHTY;

/**
 * Represents a locale-aware, high-precision numerical value supporting advanced mathematical operations.
 * <p>
 * The {@code BigNumber} class encapsulates decimal numbers with arbitrarily high precision and supports a wide
 * range of mathematical functions, including:
 * </p>
 * <ul>
 *   <li>Basic arithmetic (add, subtract, multiply, divide, modulo)</li>
 *   <li>Exponentiation and root operations (power, square root, cube root, nth root)</li>
 *   <li>Logarithmic functions (base-10, base-e, base-2, arbitrary base)</li>
 *   <li>Trigonometric and inverse trigonometric functions in degrees/radians/grad</li>
 *   <li>Hyperbolic and inverse hyperbolic functions</li>
 *   <li>Coordinate system conversions (polar ↔ cartesian)</li>
 *   <li>Combinatorics (factorial, combinations, permutations)</li>
 *   <li>Percentages, GCD, LCM, random integers</li>
 * </ul>
 *
 * <p>This class supports locale-specific formatting, configurable {@link MathContext} for precision,
 * and flexible angle measurement via {@link TrigonometricMode}. Input strings can be automatically
 * parsed according to locale or explicitly specified.</p>
 *
 * <p>{@code BigNumber} is immutable in behavior and uses {@link CalculatorEngine} for computational logic.
 * Internally, most computations are delegated to utility classes like {@code BasicMath}, {@code RadicalMath},
 * or {@code LogarithmicMath}, ensuring modularity and clean separation of concerns.</p>
 *
 * <p>Instances of this class are ideal for applications requiring precise decimal arithmetic,
 * such as financial systems, scientific calculations, or custom calculators.</p>
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
	private String valueBeforeDecimal;
	/**
	 * The numeric value after the decimal separator. Defaults to "0" if absent.
	 */
	@NonNull
	private String valueAfterDecimal;
	/**
	 * Indicates whether the number is negative.
	 */
	private boolean isNegative;
	/**
	 * Shared instance of the CalculatorEngine used for performing arithmetic operations.
	 * This static engine allows all BigNumber instances to use the same precision settings.
	 */
	@NonNull
	@Setter
	private CalculatorEngine calculatorEngine;
	/**
	 * The trigonometric mode (e.g., DEG, RAD, GRAD) used for trigonometric calculations.
	 * Defaults to DEG (degrees).
	 */
	@NonNull
	@Setter
	private TrigonometricMode trigonometricMode;
	/**
	 * The MathContext used for arithmetic operations, defaulting to a precision of
	 * CalculatorEngine.DEFAULT_DIVISION_PRECISION digits.
	 */
	@NonNull
	@Setter
	private MathContext mathContext;

	/**
	 * Constructs a BigNumber from a string using the default locale.
	 *
	 * @param number
	 * 	the string representation of the number
	 */
	public BigNumber(@NonNull final String number) {
		this(number, Locale.getDefault());
	}

	/**
	 * Constructs a BigNumber from a string and trigonometric mode using the default locale and math context.
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 */
	public BigNumber(@NonNull final String number, @NonNull final TrigonometricMode trigonometricMode) {
		this(number, Locale.getDefault(), DEFAULT_MATH_CONTEXT, trigonometricMode);
	}

	/**
	 * Constructs a BigNumber from a string and locale using the default math context.
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param targetLocale
	 * 	the locale to use for parsing and formatting
	 */
	public BigNumber(@NonNull final String number, @NonNull final Locale targetLocale) {
		this(number, targetLocale, DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Constructs a BigNumber from a string, trigonometric mode, and locale using the default math context.
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 * @param targetLocale
	 * 	the locale to use for parsing and formatting
	 */
	public BigNumber(@NonNull final String number, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale targetLocale) {
		this(number, targetLocale, DEFAULT_MATH_CONTEXT, trigonometricMode);
	}

	/**
	 * Constructs a BigNumber from a string and math context using the default locale.
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 */
	public BigNumber(@NonNull final String number, @NonNull final MathContext mathContext) {
		this(number, Locale.getDefault(), mathContext);
	}

	/**
	 * Constructs a BigNumber from a string, locale, and math context using the default trigonometric mode (DEG).
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param targetLocale
	 * 	the locale to use for parsing and formatting
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 */
	public BigNumber(@NonNull final String number, @NonNull final Locale targetLocale, @NonNull final MathContext mathContext) {
		this(number, targetLocale, mathContext, TrigonometricMode.DEG);
	}

	/**
	 * Constructs a BigNumber from a string, locale, math context, and trigonometric mode.
	 * Parses and formats the input string according to the specified locale.
	 *
	 * @param number
	 * 	the string representation of the number
	 * @param targetLocale
	 * 	the locale to use for parsing and formatting
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 */
	public BigNumber(@NonNull final String number, @NonNull final Locale targetLocale, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		BigNumber parsedAndFormatted = bigNumberParser.parseAndFormat(number, targetLocale);
		this.locale = targetLocale;
		this.valueBeforeDecimal = parsedAndFormatted.valueBeforeDecimal;
		this.valueAfterDecimal = parsedAndFormatted.valueAfterDecimal;
		this.isNegative = parsedAndFormatted.isNegative;
		this.mathContext = mathContext;
		this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(this.trigonometricMode);
	}

	/**
	 * Constructs a BigNumber from a BigDecimal using the default locale.
	 *
	 * @param bigDecimal
	 * 	the BigDecimal value to convert
	 */
	public BigNumber(@NonNull final BigDecimal bigDecimal) {
		this(bigDecimal, Locale.getDefault());
	}

	/**
	 * Constructs a BigNumber from a BigDecimal and locale.
	 *
	 * @param bigDecimal
	 * 	the BigDecimal value to convert
	 * @param targetLocale
	 * 	the locale to use for parsing and formatting
	 */
	public BigNumber(@NonNull final BigDecimal bigDecimal, @NonNull final Locale targetLocale) {
		this(bigDecimal.toPlainString(), targetLocale);
	}

	/**
	 * Constructs a BigNumber from another BigNumber and a new locale using the default math context.
	 *
	 * @param bigNumber
	 * 	the source BigNumber
	 * @param targetLocale
	 * 	the new locale to use
	 */
	public BigNumber(@NonNull final BigNumber bigNumber, @NonNull final Locale targetLocale) {
		this(bigNumber, targetLocale, DEFAULT_MATH_CONTEXT);
	}

	/**
	 * Constructs a BigNumber from another BigNumber, a new locale, and a math context using the default trigonometric
	 * mode (DEG).
	 *
	 * @param bigNumber
	 * 	the source BigNumber
	 * @param targetLocale
	 * 	the new locale to use
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 */
	public BigNumber(@NonNull final BigNumber bigNumber, @NonNull final Locale targetLocale, @NonNull final MathContext mathContext) {
		this(bigNumber, targetLocale, mathContext, TrigonometricMode.DEG);
	}

	/**
	 * Constructs a BigNumber from another BigNumber, a new locale, a math context, and a trigonometric mode.
	 *
	 * @param bigNumber
	 * 	the source BigNumber
	 * @param targetLocale
	 * 	the new locale to use
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 */
	public BigNumber(@NonNull final BigNumber bigNumber, @NonNull final Locale targetLocale, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		this.locale = targetLocale;
		this.valueBeforeDecimal = bigNumber.valueBeforeDecimal;
		this.valueAfterDecimal = bigNumber.valueAfterDecimal;
		this.isNegative = bigNumber.isNegative;
		this.mathContext = mathContext;
		this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(this.trigonometricMode);
	}

	/**
	 * Copy constructor. Constructs a BigNumber from another BigNumber, copying all properties.
	 *
	 * @param other
	 * 	the BigNumber to copy
	 */
	public BigNumber(@NonNull final BigNumber other) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.mathContext = other.mathContext;
		this.trigonometricMode = other.trigonometricMode;
		this.calculatorEngine = other.calculatorEngine;
	}

	/**
	 * Builder constructor for BigNumber.
	 *
	 * @param locale
	 * 	the locale to use for parsing and formatting
	 * @param valueBeforeDecimal
	 * 	the integer part of the number
	 * @param valueAfterDecimal
	 * 	the decimal part of the number
	 * @param isNegative
	 * 	whether the number is negative
	 * @param mathContext
	 * 	the math context to use for precision and rounding
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 */
	@Builder
	public BigNumber(
		@NonNull final Locale locale,
		@NonNull final String valueBeforeDecimal,
		@NonNull final String valueAfterDecimal,
		final boolean isNegative,
		@NonNull final MathContext mathContext,
		@NonNull final TrigonometricMode trigonometricMode
	) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.mathContext = mathContext;
		this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(this.trigonometricMode);
	}

	/**
	 * Constructs a BigNumber with the specified locale and default value of zero.
	 *
	 * @param locale
	 * 	the locale to use for parsing and formatting
	 */
	public BigNumber(@NonNull final Locale locale) {
		this(locale, "0", "0", false, DEFAULT_MATH_CONTEXT, TrigonometricMode.DEG);
	}

	/**
	 * Adds the specified {@link BigNumber} to this number using the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #add(BigNumber, Locale)}.</p>
	 *
	 * @param addend
	 * 	the number to add to this number
	 *
	 * @return a new {@code BigNumber} representing the sum
	 */
	public BigNumber add(@NonNull final BigNumber addend) {
		return add(addend, locale);
	}

	/**
	 * Adds the specified {@link BigNumber} to this number.
	 *
	 * <p>This method converts both operands to {@link BigDecimal}, performs the addition using
	 * {@link BigDecimal#add(BigDecimal)}, and wraps the result in a new {@code BigNumber}.</p>
	 *
	 * @param addend
	 * 	the number to add to this number
	 * @param locale
	 * 	the result, which should be parsed to a specific locale
	 *
	 * @return a new {@code BigNumber} representing the sum
	 */
	public BigNumber add(@NonNull final BigNumber addend, @NonNull final Locale locale) {
		return BasicMath.add(this, addend, locale);
	}

	/**
	 * Subtracts the specified {@link BigNumber} from this number using the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #subtract(BigNumber, Locale)}.</p>
	 *
	 * @param subtrahend
	 * 	the number to subtract from this number
	 *
	 * @return a new {@code BigNumber} representing the difference
	 */
	public BigNumber subtract(@NonNull final BigNumber subtrahend) {
		return subtract(subtrahend, locale);
	}

	/**
	 * Subtracts the specified {@link BigNumber} from this number.
	 *
	 * <p>The method converts the operand to a {@link BigDecimal} and performs the subtraction using
	 * {@link BigDecimal#subtract(BigDecimal)}. The result is then wrapped in a new {@code BigNumber}.</p>
	 *
	 * @param subtrahend
	 * 	the number to subtract from this number
	 * @param locale
	 * 	the result, which should be parsed to a specific locale
	 *
	 * @return a new {@code BigNumber} representing the difference
	 */
	public BigNumber subtract(@NonNull final BigNumber subtrahend, @NonNull final Locale locale) {
		return BasicMath.subtract(this, subtrahend, locale);
	}

	/**
	 * Multiplies this {@code BigNumber} by the specified {@code multiplier} using the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #multiply(BigNumber, Locale)}.</p>
	 *
	 * @param multiplier
	 * 	the number to multiply with this number
	 *
	 * @return a new {@code BigNumber} representing the product
	 */
	public BigNumber multiply(@NonNull final BigNumber multiplier) {
		return multiply(multiplier, locale);
	}

	/**
	 * Multiplies this number by the specified {@link BigNumber}.
	 *
	 * <p>Converts the operand to a {@link BigDecimal}, multiplies it with this number using
	 * {@link BigDecimal#multiply(BigDecimal)}, and returns the result as a new {@code BigNumber}.</p>
	 *
	 * @param multiplier
	 * 	the number to multiply with this number
	 *
	 * @return a new {@code BigNumber} representing the product
	 */
	public BigNumber multiply(@NonNull final BigNumber multiplier, @NonNull final Locale locale) {
		return BasicMath.multiply(this, multiplier, locale);
	}

	/**
	 * Divides this {@code BigNumber} by the specified {@code BigNumber}
	 * using the default {@link MathContext} configured in this instance.
	 *
	 * <p>This is a convenience method that delegates to
	 * {@link #divide(BigNumber, MathContext)} using the instance's default context.</p>
	 *
	 * @param divisor
	 * 	the divisor
	 *
	 * @return a new {@code BigNumber} representing the result of the division
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(@NonNull final BigNumber divisor) {
		return divide(divisor, mathContext);
	}

	/**
	 * Divides this number by the specified {@link BigNumber} using the provided {@link MathContext}.
	 *
	 * <p>Performs division with the specified precision and rounding mode, using
	 * {@link BigDecimal#divide(BigDecimal, MathContext)}.
	 * The result is wrapped in a new {@code BigNumber}.</p>
	 *
	 * @param divisor
	 * 	the divisor
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the quotient
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(@NonNull final BigNumber divisor, @NonNull final MathContext mathContext) {
		return divide(divisor, mathContext, locale);
	}

	/**
	 * Divides this {@code BigNumber} by the specified {@code divisor} using the provided {@link MathContext} and
	 * {@link Locale}.
	 *
	 * <p>This method delegates the division operation to
	 * {@link BasicMath#divide(BigNumber, BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param divisor
	 * 	the divisor to divide this number by
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale used for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the result of the division
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(@NonNull final BigNumber divisor, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return BasicMath.divide(this, divisor, mathContext, locale);
	}

	/**
	 * Computes the modulo (remainder) of this number divided by the specified {@link BigNumber}
	 * using the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #modulo(BigNumber, Locale)}.</p>
	 *
	 * @param divisor
	 * 	the divisor to compute the remainder with
	 *
	 * @return a new {@code BigNumber} representing the result of {@code this % divisor}
	 */
	public BigNumber modulo(@NonNull final BigNumber divisor) {
		return modulo(divisor, locale);
	}

	/**
	 * Computes the modulo (remainder) of this number divided by the specified {@link BigNumber}.
	 *
	 * <p>This implementation uses integer subtraction and returns the result as a new {@code BigNumber}.</p>
	 *
	 * @param divisor
	 * 	the divisor (must not be zero or negative)
	 *
	 * @return a new {@code BigNumber} representing {@code this % divisor}
	 *
	 * @throws IllegalArgumentException
	 * 	if the divisor is zero or if either number is negative
	 */
	public BigNumber modulo(@NonNull final BigNumber divisor, @NonNull final Locale locale) {
		return BasicMath.modulo(this, divisor, locale);
	}

	// TODO: füge in jeder methode, in einer mathe klasse, den locale parameter hinzu

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
	public BigNumber power(@NonNull final BigNumber exponent) {
		return power(exponent, mathContext);
	}

	/**
	 * Raises this {@code BigNumber} to the specified exponent using the given {@link MathContext}.
	 *
	 * <p>This method delegates to {@link #power(BigNumber, MathContext, Locale)} using the current locale.</p>
	 *
	 * @param exponent
	 * 	the exponent to raise this number to
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the result of the exponentiation
	 */
	public BigNumber power(@NonNull final BigNumber exponent, @NonNull final MathContext mathContext) {
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
	 * 	the locale to apply for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the result of the exponentiation
	 */
	public BigNumber power(@NonNull final BigNumber exponent, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return BasicMath.power(this, exponent, mathContext, locale);
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
		return factorial(mathContext);
	}

	/**
	 * Computes the factorial of this {@code BigNumber} using the specified {@link MathContext}.
	 *
	 * <p>This method delegates to {@link #factorial(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the factorial
	 *
	 * @throws ArithmeticException
	 * 	if the number is not a non-negative integer or too large for computation
	 */
	public BigNumber factorial(@NonNull final MathContext mathContext) {
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
	 * 	the locale to apply for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the factorial
	 */
	public BigNumber factorial(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return BasicMath.factorial(this, mathContext, locale);
	}

	/**
	 * Computes the exponential function \(e^x\), where \(x\) is this {@code BigNumber} instance,
	 * using the default {@link MathContext} and {@link Locale} configured in this instance.
	 *
	 * @return a new {@code BigNumber} representing \(e^{this}\)
	 */
	// TODO: test
	public BigNumber exp() {
		return exp(mathContext);
	}

	/**
	 * Computes the exponential function \(e^x\), where \(x\) is this {@code BigNumber} instance,
	 * using the specified {@link MathContext} and the current locale.
	 *
	 * @param mathContext
	 * 	the context defining precision and rounding
	 *
	 * @return a new {@code BigNumber} representing \(e^{this}\)
	 */
	public BigNumber exp(@NonNull final MathContext mathContext) {
		return exp(mathContext, locale);
	}

	/**
	 * Computes the exponential function e^x, where x is this {@link BigNumber} instance.
	 *
	 * @param mathContext
	 * 	the context defining precision and rounding
	 * @param locale
	 * 	the locale for result formatting
	 *
	 * @return a new {@link BigNumber} representing e^this
	 */
	public BigNumber exp(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return BasicMath.exp(this, mathContext, locale);
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
		return squareRoot(mathContext);
	}

	/**
	 * Computes the square root of this {@code BigNumber} using the specified {@link MathContext}.
	 *
	 * <p>This method delegates to {@link #squareRoot(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the square root
	 */
	public BigNumber squareRoot(@NonNull final MathContext mathContext) {
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
	 * 	the locale to apply for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the square root
	 */
	public BigNumber squareRoot(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return RadicalMath.squareRoot(this, mathContext, locale);
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
		return cubicRoot(mathContext);
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
	 * 	the locale to apply for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the cube root
	 */
	public BigNumber cubicRoot(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return RadicalMath.cubicRoot(this, mathContext, locale);
	}

	/**
	 * Computes the cube root of this {@code BigNumber} using the specified {@link MathContext}.
	 *
	 * <p>This method delegates to {@link RadicalMath#cubicRoot(BigNumber, MathContext, Locale)} using the current
	 * locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the cube root
	 */
	public BigNumber cubicRoot(@NonNull final MathContext mathContext) {
		return RadicalMath.cubicRoot(this, mathContext, locale);
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
	public BigNumber nthRoot(@NonNull final BigNumber n) {
		return nthRoot(n, mathContext);
	}

	/**
	 * Computes the <i>n</i>th root of this {@code BigNumber} using the specified {@link MathContext}.
	 * <p>
	 * This is a convenience method that delegates to {@link #nthRoot(BigNumber, MathContext, Locale)}
	 * using the current locale.
	 *
	 * @param n
	 * 	the degree of the root (must be non-negative)
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the <i>n</i>th root
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} is negative
	 */
	public BigNumber nthRoot(@NonNull final BigNumber n, @NonNull final MathContext mathContext) {
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
	 * 	the locale to apply for formatting or localization
	 *
	 * @return a new {@code BigNumber} representing the <i>n</i>th root
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} is negative
	 */
	public BigNumber nthRoot(@NonNull final BigNumber n, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return RadicalMath.nthRoot(this, n, mathContext, locale);
	}

	/**
	 * Computes the base-2 logarithm of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #log2(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing log₂(this)
	 */
	// TODO: test
	public BigNumber log2() {
		return log2(mathContext);
	}

	/**
	 * Computes the base-2 logarithm of this number using the specified {@link MathContext} and the current locale.
	 *
	 * <p>This method delegates to {@link #log2(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing log₂(this)
	 */
	public BigNumber log2(@NonNull final MathContext mathContext) {
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
	public BigNumber log2(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return LogarithmicMath.log2(this, mathContext, locale);
	}

	/**
	 * Computes the base-10 logarithm of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #log10(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing log₁₀(this)
	 */
	// TODO: test
	public BigNumber log10() {
		return log10(mathContext);
	}

	/**
	 * Computes the base-10 logarithm of this number using the specified {@link MathContext} and the current locale.
	 *
	 * <p>This method delegates to {@link #log10(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing log₁₀(this)
	 */
	public BigNumber log10(@NonNull final MathContext mathContext) {
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
	public BigNumber log10(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return LogarithmicMath.log10(this, mathContext, locale);
	}

	/**
	 * Computes the natural logarithm (base e) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #ln(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing ln(this)
	 */
	// TODO: test
	public BigNumber ln() {
		return ln(mathContext);
	}

	/**
	 * Computes the natural logarithm (base \(e\)) of this number using the specified {@link MathContext}.
	 *
	 * <p>This method delegates to {@link #ln(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing ln(this)
	 */
	public BigNumber ln(@NonNull final MathContext mathContext) {
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
	public BigNumber ln(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return LogarithmicMath.ln(this, mathContext, locale);
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
	// TODO: test
	public BigNumber logBase(@NonNull final BigNumber base) {
		return logBase(base, mathContext);
	}

	/**
	 * Computes the logarithm of this number in the given base using the specified {@link MathContext}
	 * and the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #logBase(BigNumber, MathContext, Locale)}
	 * using the current locale.</p>
	 *
	 * @param base
	 * 	the logarithm base (must be > 0)
	 * @param mathContext
	 * 	the precision and rounding settings for both ln calculations and division
	 *
	 * @return a new {@code BigNumber} representing log₍base₎(this)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code base} is zero or negative
	 */
	public BigNumber logBase(@NonNull final BigNumber base, @NonNull final MathContext mathContext) {
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
	public BigNumber logBase(@NonNull final BigNumber base, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return LogarithmicMath.logBase(this, base, mathContext, locale);
	}

	/**
	 * Computes the sine of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #sin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing sin(this)
	 */
	// TODO: test
	public BigNumber sin() {
		return sin(trigonometricMode);
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
	public BigNumber sin(@NonNull final TrigonometricMode trigonometricMode) {
		return sin(mathContext, trigonometricMode);
	}

	/**
	 * Computes the sine of this number using the specified {@link MathContext} and {@link TrigonometricMode},
	 * and the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #sin(MathContext, TrigonometricMode, Locale)}
	 * using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a new {@code BigNumber} representing sin(this)
	 */
	public BigNumber sin(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
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
	public BigNumber sin(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return TrigonometricMath.sin(this, mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the cosine of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cos(this)
	 */
	// TODO: test
	public BigNumber cos() {
		return cos(trigonometricMode);
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
	public BigNumber cos(@NonNull final TrigonometricMode trigonometricMode) {
		return cos(mathContext, trigonometricMode);
	}

	/**
	 * Computes the cosine of this number using the specified {@link MathContext} and {@link TrigonometricMode},
	 * and the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #cos(MathContext, TrigonometricMode, Locale)}
	 * using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a new {@code BigNumber} representing cos(this)
	 */
	public BigNumber cos(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
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
	public BigNumber cos(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return TrigonometricMath.cos(this, mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the tangent of this number using the default {@link MathContext}, {@link TrigonometricMode}, and
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #tan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing tan(this)
	 */
	// TODO: test
	public BigNumber tan() {
		return tan(trigonometricMode);
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
	public BigNumber tan(@NonNull final TrigonometricMode trigonometricMode) {
		return tan(mathContext, trigonometricMode);
	}

	/**
	 * Computes the tangent of this number using the specified {@link MathContext} and {@link TrigonometricMode},
	 * and the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #tan(MathContext, TrigonometricMode, Locale)}
	 * using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a new {@code BigNumber} representing tan(this)
	 */
	public BigNumber tan(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
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
	public BigNumber tan(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return TrigonometricMath.tan(this, mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the cotangent (cot) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cot(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cot(this)
	 */
	public BigNumber cot() {
		return cot(trigonometricMode);
	}

	/**
	 * Computes the cotangent of this number using the specified {@link TrigonometricMode} and the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cot(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the input is in degrees or radians
	 *
	 * @return a new {@code BigNumber} representing cot(this)
	 */
	public BigNumber cot(@NonNull final TrigonometricMode trigonometricMode) {
		return cot(mathContext, trigonometricMode);
	}

	/**
	 * Computes the cotangent of this number using the specified {@link MathContext} and {@link TrigonometricMode},
	 * and the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #cot(MathContext, TrigonometricMode, Locale)}
	 * using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a new {@code BigNumber} representing cot(this)
	 */
	public BigNumber cot(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		return cot(mathContext, trigonometricMode, locale);
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
	public BigNumber cot(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return TrigonometricMath.cot(this, mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the hyperbolic sine of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #sinh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing sinh(this)
	 */
	public BigNumber sinh() {
		return sinh(mathContext);
	}

	/**
	 * Computes the hyperbolic sine (sinh) of this number using the specified {@link MathContext} and the current locale.
	 *
	 * <p>This method delegates to {@link #sinh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing sinh(this)
	 */
	public BigNumber sinh(@NonNull final MathContext mathContext) {
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
	public BigNumber sinh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return HyperbolicTrigonometricMath.sinh(this, mathContext, locale);
	}

	/**
	 * Computes the hyperbolic cosine of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #cosh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing cosh(this)
	 */
	public BigNumber cosh() {
		return cosh(mathContext);
	}

	/**
	 * Computes the hyperbolic cosine (cosh) of this number using the specified {@link MathContext} and the current
	 * locale.
	 *
	 * <p>This method delegates to {@link #cosh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing cosh(this)
	 */
	public BigNumber cosh(@NonNull final MathContext mathContext) {
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
	public BigNumber cosh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return HyperbolicTrigonometricMath.cosh(this, mathContext, locale);
	}

	/**
	 * Computes the hyperbolic tangent of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #tanh(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing tanh(this)
	 */
	public BigNumber tanh() {
		return tanh(mathContext);
	}

	/**
	 * Computes the hyperbolic tangent (tanh) of this number using the specified {@link MathContext} and the current
	 * locale.
	 *
	 * <p>This method delegates to {@link #tanh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing tanh(this)
	 */
	public BigNumber tanh(@NonNull final MathContext mathContext) {
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
	public BigNumber tanh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return HyperbolicTrigonometricMath.tanh(this, mathContext, locale);
	}

	/**
	 * Computes the hyperbolic cotangent (coth) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #coth(MathContext, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing coth(this)
	 */
	public BigNumber coth() {
		return coth(mathContext);
	}

	/**
	 * Computes the hyperbolic cotangent (coth) of this number using the specified {@link MathContext} and the current
	 * locale.
	 *
	 * <p>This method delegates to {@link #coth(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing coth(this)
	 */
	public BigNumber coth(@NonNull final MathContext mathContext) {
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
	public BigNumber coth(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return HyperbolicTrigonometricMath.coth(this, mathContext, locale);
	}

	/**
	 * Computes the inverse sine (arcsin) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #asin(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing asin(this)
	 */
	/*
	// TODO
	public BigNumber asin() {
		return asin(trigonometricMode);
	}
	 */

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
	/*
	// TODO
	public BigNumber asin(@NonNull final TrigonometricMode trigonometricMode) {
		return asin(mathContext, trigonometricMode);
	}
	 */

	/**
	 * Computes the inverse sine (arcsin) of this number using the specified {@link MathContext} and
	 * {@link TrigonometricMode},
	 * returning the result in the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #asin(MathContext, TrigonometricMode, Locale)} using the
	 * current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing asin(this)
	 */
	/*
	// TODO
	public BigNumber asin(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		return asin(mathContext, trigonometricMode, locale);
	}
	 */

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
	/*
	public BigNumber asin(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return InverseTrigonometricMath.asin(this, mathContext, trigonometricMode, locale);
	}
	 */

	/**
	 * Computes the inverse cosine (arccos) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acos(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acos(this)
	 */
	/*
	// TODO
	public BigNumber acos() {
		return acos(trigonometricMode);
	}
	 */

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
	/*
	// TODO
	public BigNumber acos(@NonNull final TrigonometricMode trigonometricMode) {
		return acos(mathContext, trigonometricMode);
	}
	 */

	/**
	 * Computes the inverse cosine (arccos) of this number using the specified {@link MathContext} and
	 * {@link TrigonometricMode},
	 * returning the result in the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #acos(MathContext, TrigonometricMode, Locale)} using the
	 * current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing acos(this)
	 */
	/*
	// TODO
	public BigNumber acos(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		return acos(mathContext, trigonometricMode, locale);
	}
	 */

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
	/*
	// TODO
	public BigNumber acos(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return InverseTrigonometricMath.acos(this, mathContext, trigonometricMode, locale);
	}
	 */

	/**
	 * Computes the inverse tangent (arctan) of this number using the default {@link MathContext},
	 * {@link TrigonometricMode}, and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing atan(this)
	 */
	public BigNumber atan() {
		return atan(trigonometricMode);
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
	public BigNumber atan(@NonNull final TrigonometricMode trigonometricMode) {
		return atan(mathContext, trigonometricMode);
	}

	/**
	 * Computes the inverse tangent (arctan) of this number using the specified {@link MathContext} and
	 * {@link TrigonometricMode},
	 * returning the result in the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan(MathContext, TrigonometricMode, Locale)} using the
	 * current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing atan(this)
	 */
	public BigNumber atan(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
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
	public BigNumber atan(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return InverseTrigonometricMath.atan(this, mathContext, trigonometricMode, locale);
	}

	/**
	 * Computes the inverse cotangent (arccot) of this number using the default {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acot(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @return a new {@code BigNumber} representing acot(this)
	 */
	public BigNumber acot() {
		return acot(TrigonometricMode.DEG);
	}

	/**
	 * Computes the inverse cotangent (arccot) of this number in the specified angle unit using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #acot(MathContext, TrigonometricMode, Locale)}.</p>
	 *
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing acot(this)
	 */
	public BigNumber acot(@NonNull final TrigonometricMode trigonometricMode) {
		return acot(mathContext, trigonometricMode);
	}

	/**
	 * Computes the inverse cotangent (arccot) of this number using the specified {@link MathContext} and
	 * {@link TrigonometricMode},
	 * returning the result in the current locale.
	 *
	 * <p>This is a convenience method that delegates to {@link #acot(MathContext, TrigonometricMode, Locale)} using the
	 * current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	whether the result should be in degrees ({@code DEG}) or radians ({@code RAD})
	 *
	 * @return a new {@code BigNumber} representing acot(this)
	 */
	public BigNumber acot(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
		return acot(mathContext, trigonometricMode, locale);
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
	public BigNumber acot(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return InverseTrigonometricMath.acot(this, mathContext, trigonometricMode, locale);
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
		return asinh(mathContext);
	}

	/**
	 * Computes the inverse hyperbolic sine (asinh) of this number using the specified {@link MathContext}
	 * and the current locale.
	 *
	 * <p>This method delegates to {@link #asinh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing asinh(this)
	 */
	public BigNumber asinh(@NonNull final MathContext mathContext) {
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
	public BigNumber asinh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return InverseHyperbolicTrigonometricMath.asinh(this, mathContext, locale);
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
		return acosh(mathContext);
	}

	/**
	 * Computes the inverse hyperbolic cosine (acosh) of this number using the specified {@link MathContext} and the
	 * current locale.
	 *
	 * <p>This method delegates to {@link #acosh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing acosh(this)
	 */
	public BigNumber acosh(@NonNull final MathContext mathContext) {
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
	public BigNumber acosh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return InverseHyperbolicTrigonometricMath.acosh(this, mathContext, locale);
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
		return atanh(mathContext);
	}

	/**
	 * Computes the inverse hyperbolic tangent (atanh) of this number using the specified {@link MathContext}
	 * and the current locale.
	 *
	 * <p>This method delegates to {@link #atanh(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing atanh(this)
	 */
	public BigNumber atanh(@NonNull final MathContext mathContext) {
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
	public BigNumber atanh(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return InverseHyperbolicTrigonometricMath.atanh(this, mathContext, locale);
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
		return acoth(mathContext);
	}

	/**
	 * Computes the inverse hyperbolic cotangent (acoth) of this number using the specified {@link MathContext}
	 * and the current locale.
	 *
	 * <p>This method delegates to {@link #acoth(MathContext, Locale)} using the current locale.</p>
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing acoth(this)
	 */
	public BigNumber acoth(@NonNull final MathContext mathContext) {
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
	public BigNumber acoth(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return InverseHyperbolicTrigonometricMath.acoth(this, mathContext, locale);
	}

	/**
	 * Computes the two-argument arctangent of this number and the specified x number using the default
	 * {@link MathContext} and {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan2(BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param x
	 * 	the second coordinate for atan2(this, x)
	 *
	 * @return a new {@code BigNumber} representing atan2(this, x)
	 */
	public BigNumber atan2(@NonNull final BigNumber x) {
		return atan2(x, mathContext);
	}

	/**
	 * Computes the two-argument arctangent (atan2) of this number and the specified x number
	 * using the provided {@link MathContext} and the current {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #atan2(BigNumber, MathContext, Locale)} using the current
	 * locale.</p>
	 *
	 * @param x
	 * 	the second coordinate for atan2(this, x)
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing atan2(this, x)
	 */
	public BigNumber atan2(@NonNull final BigNumber x, @NonNull final MathContext mathContext) {
		return atan2(x, mathContext, locale);
	}

	/**
	 * Computes the two-argument arctangent of this number and the specified x number.
	 *
	 * <p>Delegates to {@link BigDecimalMath#atan2(BigDecimal, BigDecimal, MathContext)} for quadrant-aware result,
	 * then wraps the result in a new {@code BigNumber} with the specified {@code locale}.</p>
	 *
	 * @param x
	 * 	the y-coordinate component for atan2(this, x)
	 * @param mathContext
	 * 	the precision and rounding settings for the calculation
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing atan2(this, x)
	 */
	public BigNumber atan2(@NonNull final BigNumber x, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return TwoDimensionalMath.atan2(this, x, mathContext, locale);
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
	public BigNumber combination(@NonNull final BigNumber k) {
		return combination(k, mathContext);
	}


	/**
	 * Calculates the number of combinations (n choose k) using the specified {@link MathContext} and the current
	 * {@link Locale}.
	 *
	 * <p>This is a convenience method that delegates to {@link #combination(BigNumber, MathContext, Locale)} using the
	 * current locale.</p>
	 *
	 * @param k
	 * 	the number of items to choose (must be between 0 and n inclusive)
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a {@code BigNumber} representing C(n, k)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either number has a decimal part
	 */
	public BigNumber combination(@NonNull final BigNumber k, @NonNull final MathContext mathContext) {
		return combination(k, mathContext, locale);
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
	public BigNumber combination(@NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return CombinatoricsMath.combination(this, k, mathContext, locale);
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
	public BigNumber permutation(@NonNull final BigNumber k) {
		return permutation(k, mathContext);
	}

	/**
	 * Calculates the number of permutations (nPk) for this number and the specified k,
	 * using the provided {@link MathContext} and the current {@link Locale}.
	 *
	 * <p>A permutation is the number of ways to arrange {@code k} elements out of {@code n} (this object),
	 * where order matters. It is defined as:</p>
	 *
	 * <pre>
	 *     P(n, k) = n! / (n - k)!
	 * </pre>
	 *
	 * <p>This implementation delegates to
	 * {@link CombinatoricsMath#permutation(BigNumber, BigNumber, MathContext, Locale)}.</p>
	 *
	 * @param k
	 * 	the number of elements to select and order (must be between 0 and n inclusive)
	 * @param mathContext
	 * 	the context for precision and rounding
	 *
	 * @return a {@code BigNumber} representing P(n, k)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code k > n} or either value has a decimal part
	 */
	public BigNumber permutation(@NonNull final BigNumber k, @NonNull final MathContext mathContext) {
		return CombinatoricsMath.permutation(this, k, mathContext, locale);
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
	public BigNumber permutation(@NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return CombinatoricsMath.permutation(this, k, mathContext, locale);
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
	public BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber theta) {
		return polarToCartesianCoordinates(theta, mathContext);
	}

	/**
	 * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y) using the specified {@link MathContext}
	 * and the current {@link TrigonometricMode} and {@link Locale}.
	 *
	 * @param theta
	 * 	the angle (in degrees or radians, depending on {@code trigonometricMode})
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a {@code BigNumberCoordinate} representing (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber theta, @NonNull final MathContext mathContext) {
		return polarToCartesianCoordinates(theta, mathContext, trigonometricMode);
	}

	/**
	 * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y) using the specified {@link TrigonometricMode}
	 * and the current {@link MathContext} and {@link Locale}.
	 *
	 * @param theta
	 * 	the angle (in degrees or radians, depending on {@code trigonometricMode})
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a {@code BigNumberCoordinate} representing (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber theta, @NonNull final TrigonometricMode trigonometricMode) {
		return polarToCartesianCoordinates(theta, mathContext, trigonometricMode);
	}

	/**
	 * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y) using the specified {@link MathContext},
	 * {@link TrigonometricMode}, and the current {@link Locale}.
	 *
	 * @param theta
	 * 	the angle (in degrees or radians, depending on {@code trigonometricMode})
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (degrees, radians, or grads)
	 *
	 * @return a {@code BigNumberCoordinate} representing (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber theta, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
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
	public BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber theta, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return CoordinateConversionMath.polarToCartesianCoordinates(this, theta, mathContext, trigonometricMode, locale);
	}

	// TODO: polar and cartesian coordinates in 3d

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
	public BigNumberCoordinate cartesianToPolarCoordinates(@NonNull final BigNumber y) {
		return cartesianToPolarCoordinates(y, mathContext);
	}

	/**
	 * Converts Cartesian coordinates (x, y) to polar coordinates (r, θ in degrees) using the specified
	 * {@link MathContext}
	 * and the current {@link Locale}.
	 *
	 * <p>This method delegates to {@link #cartesianToPolarCoordinates(BigNumber, MathContext, Locale)} using the current
	 * locale.</p>
	 *
	 * @param y
	 * 	the y-coordinate
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a {@code BigNumberCoordinate} representing (r, θ) with θ in degrees
	 */
	public BigNumberCoordinate cartesianToPolarCoordinates(@NonNull final BigNumber y, @NonNull final MathContext mathContext) {
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
	public BigNumberCoordinate cartesianToPolarCoordinates(@NonNull final BigNumber y, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return CoordinateConversionMath.cartesianToPolarCoordinates(this, y, mathContext, locale);
	}

	/**
	 * Generates a random integer {@link BigNumber} between this number (inclusive) and the given {@code max}
	 * (exclusive).
	 * <p>
	 * Mathematically: returns a value x such that this ≤ x < max, where x is an integer.
	 * <p>
	 * Both {@code this} and {@code max} must be integers without decimal places, and {@code this < max}.
	 *
	 * @param max
	 * 	the exclusive upper bound
	 *
	 * @return a random integer {@link BigNumber} in the range [this, max)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code this} ≥ {@code max}, or if either value has decimal places
	 */
	public BigNumber randomIntegerForRange(@NonNull final BigNumber max) {
		return MathUtils.randomIntegerBigNumberInRange(this, max, locale);
	}

	/**
	 * Calculates the percentage that this number represents of the given value {@code m}.
	 *
	 * <p>This is a convenience method that delegates to {@link #percentFromM(BigNumber, MathContext)}
	 * using the instance's default {@link MathContext}.</p>
	 *
	 * @param m
	 * 	the reference value to calculate the percentage from
	 *
	 * @return a new {@code BigNumber} representing (this / m) * 100
	 */
	public BigNumber percentFromM(@NonNull final BigNumber m) {
		return percentFromM(m, mathContext);
	}

	/**
	 * Calculates the percentage value of this number from the given value {@code m}.
	 * <p>
	 * For example, if this object represents 20 and {@code m} is 50, the result is 20% of 50 (i.e., 10).
	 *
	 * @param m
	 * 	the value from which to calculate the percentage
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing (this% of m)
	 */
	public BigNumber percentFromM(@NonNull final BigNumber m, @NonNull final MathContext mathContext) {
		return percentFromM(m, mathContext, locale);
	}

	/**
	 * Calculates the percentage that this number represents of the given value {@code m},
	 * using the specified {@link MathContext} and {@link Locale}.
	 *
	 * <p>For example, if this object is 20 and {@code m} is 50, the result is (20 / 50) * 100 = 40.</p>
	 *
	 * @param m
	 * 	the reference value to calculate the percentage from
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing (this / m) * 100
	 */
	public BigNumber percentFromM(@NonNull final BigNumber m, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return PercentageMath.nPercentFromM(this, m, mathContext, locale);
	}

	/**
	 * Calculates what percentage this number is of the given value {@code n}.
	 *
	 * <p>This is a convenience method that delegates to {@link #isXPercentOfN(BigNumber, MathContext)}
	 * using the instance's default {@link MathContext}.</p>
	 *
	 * @param n
	 * 	the reference value to calculate the percentage of
	 *
	 * @return a new {@code BigNumber} representing (this / n) * 100
	 */
	public BigNumber isXPercentOfN(@NonNull final BigNumber n) {
		return isXPercentOfN(n, mathContext);
	}

	/**
	 * Calculates what percentage this number is of the given value {@code n}.
	 * <p>
	 * For example, if this object represents 10 and {@code n} is 50, the result is 20 (since 10 is 20% of 50).
	 *
	 * @param n
	 * 	the value to compare against
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the percentage (this as a percent of n)
	 */
	public BigNumber isXPercentOfN(@NonNull final BigNumber n, @NonNull final MathContext mathContext) {
		return isXPercentOfN(n, mathContext, locale);
	}

	/**
	 * Calculates what percentage this number is of the given value {@code n}, using the specified {@link MathContext}
	 * and {@link Locale}.
	 *
	 * <p>For example, if this object is 10 and {@code n} is 50, the result is 20 (since 10 is 20% of 50).</p>
	 *
	 * @param n
	 * 	the value to compare against
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return a new {@code BigNumber} representing the percentage (this as a percent of n)
	 */
	public BigNumber isXPercentOfN(@NonNull final BigNumber n, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return PercentageMath.xIsNPercentOfN(this, n, mathContext, locale);
	}

	/**
	 * Computes the greatest common divisor (GCD) of this BigNumber and another.
	 * <p>
	 * Uses the Euclidean algorithm to find the largest positive BigNumber that divides
	 * both this and the other BigNumber without leaving a remainder.
	 * </p>
	 *
	 * @param other
	 * 	the other BigNumber to compute the GCD with
	 *
	 * @return the greatest common divisor of this and other
	 */
	public BigNumber gcd(@NonNull final BigNumber other) {
		return gcd(other, locale);
	}

	/**
	 * Computes the greatest common divisor (GCD) of this BigNumber and another, using the specified {@link Locale}.
	 * <p>
	 * Delegates to {@link NumberTheoryMath#gcd(BigNumber, BigNumber, Locale)} for the calculation.
	 * </p>
	 *
	 * @param other
	 * 	the other BigNumber to compute the GCD with
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return the greatest common divisor of this and other
	 */
	public BigNumber gcd(@NonNull final BigNumber other, @NonNull final Locale locale) {
		return NumberTheoryMath.gcd(this, other, locale);
	}

	/**
	 * Computes the least common multiple (LCM) of this BigNumber and another
	 * using the default MathContext.
	 * <p>
	 * This method delegates the calculation to {@link #lcm(BigNumber, MathContext)}
	 * using the instance's default MathContext.
	 * </p>
	 *
	 * @param other
	 * 	the other BigNumber to compute the LCM with
	 *
	 * @return the least common multiple of this and other
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs (e.g. if either number is zero)
	 */
	public BigNumber lcm(@NonNull final BigNumber other) {
		return lcm(other, mathContext);
	}

	/**
	 * Computes the least common multiple (LCM) of this BigNumber and another.
	 * <p>
	 * The LCM is calculated using the formula:
	 * <pre>
	 *     LCM(a, b) = (a * b) / GCD(a, b)
	 * </pre>
	 * where GCD is the greatest common divisor.
	 * </p>
	 *
	 * @param other
	 * 	the other BigNumber to compute the LCM with
	 *
	 * @return the least common multiple of this and other
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs (e.g. if either number is zero)
	 */
	public BigNumber lcm(@NonNull final BigNumber other, @NonNull final MathContext mathContext) {
		return lcm(other, mathContext, locale);
	}

	/**
	 * Computes the least common multiple (LCM) of this BigNumber and another, using the specified {@link MathContext}
	 * and {@link Locale}.
	 * <p>
	 * The LCM is calculated using the formula:
	 * <pre>
	 *     LCM(a, b) = (a * b) / GCD(a, b)
	 * </pre>
	 * where GCD is the greatest common divisor.
	 * </p>
	 *
	 * @param other
	 * 	the other BigNumber to compute the LCM with
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 * @param locale
	 * 	the locale used for any locale-specific formatting
	 *
	 * @return the least common multiple of this and other
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs (e.g. if either number is zero)
	 */
	public BigNumber lcm(@NonNull final BigNumber other, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return NumberTheoryMath.lcm(this, other, mathContext, locale);
	}

	/**
	 * Parses this BigNumber into a new targetLocale and mutates the current object.
	 *
	 * @param targetLocale
	 * 	the new targetLocale to apply
	 */
	public BigNumber formatToLocale(@NonNull final Locale targetLocale) {
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
	public BigNumber formatWithGroupingSeparators() {
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
	 * Converts this BigNumber to a BigDecimal using normalized US-format string.
	 * <p>
	 * Always uses '.' as decimal separator and removes grouping separators.
	 *
	 * @return a BigDecimal representation of this BigNumber
	 */
	public BigDecimal toBigDecimal() {
		StringBuilder sb = new StringBuilder();
		if (isNegative) sb.append('-');
		sb.append(valueBeforeDecimal);
		if (!valueAfterDecimal.equals("0") && !valueAfterDecimal.isEmpty()) {
			sb.append('.').append(valueAfterDecimal);
		}
		return new BigDecimal(sb.toString(), mathContext);
	}

	/**
	 * Converts this BigNumber from radians to degrees.
	 *
	 * @return a new BigNumber representing the value in degrees
	 */
	public BigNumber toDegrees(@NonNull final MathContext mathContext) {
		return multiply(ONE_HUNDRED_EIGHTY, locale).divide(MathUtils.pi(mathContext), mathContext, locale);
	}

	/**
	 * Converts this BigNumber from degrees to radians.
	 *
	 * @return a new BigNumber representing the value in radians
	 */
	public BigNumber toRadians(@NonNull final MathContext mathContext) {
		return multiply(MathUtils.pi(mathContext)).divide(ONE_HUNDRED_EIGHTY, mathContext);
	}

	/**
	 * Returns the absolute value of this {@code BigNumber}.
	 * <p>
	 * If this number is negative, a new {@code BigNumber} instance is returned with the same value but positive sign.
	 * If this number is already non-negative, the current instance is returned directly.
	 * </p>
	 *
	 * @return the absolute value of this {@code BigNumber}; either a new instance or {@code this} if already non-negative
	 */
	public BigNumber abs() {
		if (isNegative) {
			BigNumber abs = new BigNumber(toString());
			abs.isNegative = false;
			return abs;
		}
		return this;
	}

	/**
	 * Compares this BigNumber to another for equality.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if both numbers are equal, false otherwise
	 */
	public boolean isEqualTo(@NonNull final BigNumber other) {
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
	public boolean isLessThan(@NonNull final BigNumber other) {
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
	public boolean isLessThanOrEqualTo(@NonNull final BigNumber other) {
		return this.isLessThan(other) || this.isEqualTo(other);
	}

	/**
	 * Checks if this BigNumber is greater than the specified BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to compare with
	 *
	 * @return true if this is greater than other, false otherwise
	 */
	public boolean isGreaterThan(@NonNull final BigNumber other) {
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
	public boolean isGreaterThanOrEqualTo(@NonNull final BigNumber other) {
		return this.isGreaterThan(other) || this.isEqualTo(other);
	}

	/**
	 * Returns a new {@code BigNumber} instance that represents the negated value of this number.
	 * <p>
	 * This method does not modify the current object. Instead, it creates a clone of this number
	 * and inverts its sign. If the current number is negative, the result will be positive.
	 * If it is positive, the result will be negative.
	 *
	 * @return a new {@code BigNumber} with the opposite sign of this number
	 */
	public BigNumber negate() {
		return new BigNumber(this.clone().negateThis());
	}

	/**
	 * Negates this {@code BigNumber} instance in place by toggling its sign.
	 * <p>
	 * This method directly modifies the sign of the current object without creating a new instance.
	 * If the number was negative, it becomes positive. If it was positive, it becomes negative.
	 *
	 * @return this {@code BigNumber} instance with the sign toggled
	 */
	public BigNumber negateThis() {
		isNegative = !isNegative;
		return this;
	}

	/**
	 * Rounds this {@code BigNumber} to the precision specified by the given {@link MathContext}.
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} rounded according to the given {@link MathContext}
	 */
	public BigNumber round(@NonNull final MathContext mathContext) {
		return round(this, mathContext);
	}

	/**
	 * Rounds the value after the decimal point of this {@code BigNumber} using the specified {@link MathContext}.
	 *
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} representing the rounded value after the decimal
	 */
	public BigNumber roundAfterDecimals(@NonNull final MathContext mathContext) {
		return round(new BigNumber(getValueAfterDecimal()), mathContext);
	}

	/**
	 * Rounds the given {@code BigNumber} to the precision specified by the given {@link MathContext}.
	 *
	 * @param number
	 * 	the {@code BigNumber} to round
	 * @param mathContext
	 * 	the context specifying precision and rounding mode
	 *
	 * @return a new {@code BigNumber} rounded according to the given {@link MathContext}
	 */
	public BigNumber round(@NonNull final BigNumber number, @NonNull final MathContext mathContext) {
		BigDecimal bigDecimal = new BigDecimal(number.toString());
		BigDecimal rounded = bigDecimal.round(mathContext);
		return new BigNumber(rounded.toPlainString(), this.locale);
	}

	/**
	 * Removes insignificant leading and trailing zeros from the number representation.
	 * This includes leading zeros before the decimal point and trailing zeros after the decimal point.
	 *
	 * @return this {@code BigNumber} instance with trimmed parts
	 */
	public BigNumber trim() {
		valueAfterDecimal = trimTrailingZeros(valueAfterDecimal);
		valueBeforeDecimal = trimLeadingZeros(valueBeforeDecimal);
		return this;
	}

	/**
	 * Removes leading zeros from a numeric string. If the string only contains zeros,
	 * returns a single "0".
	 *
	 * @param string
	 * 	the string to trim leading zeros from
	 *
	 * @return the trimmed string with leading zeros removed
	 */
	private String trimLeadingZeros(@NonNull final String string) {
		if (string.isEmpty()) {
			return "0";
		}

		String cleaned = string.replace(" ", "");
		int index = 0;

		while (index < cleaned.length() && cleaned.charAt(index) == '0') {
			index++;
		}

		String trimmed = cleaned.substring(index);
		return trimmed.isEmpty() ? "0" : trimmed;
	}

	/**
	 * Removes trailing zeros from a numeric string (typically the decimal part).
	 * If the string only contains zeros, it returns an empty string.
	 *
	 * @param string
	 * 	the string to trim trailing zeros from
	 *
	 * @return the trimmed string with trailing zeros removed
	 */
	private String trimTrailingZeros(@NonNull final String string) {
		if (string.isEmpty()) {
			return "";
		}

		int index = string.length() - 1;

		while (index >= 0 && string.charAt(index) == '0') {
			index--;
		}

		return string.substring(0, index + 1);
	}

	/**
	 * Returns the value of this {@code BigNumber} as an {@code int}.
	 * <p>
	 * If the integer part exceeds 10 digits, only the first 10 digits are used.
	 * If parsing fails, {@code Integer.MAX_VALUE} is returned (with sign).
	 *
	 * @return the integer value represented by this object, or {@code Integer.MAX_VALUE} on error
	 */
	@Override
	public int intValue() {
		String valueBeforeDecimalAsString = valueBeforeDecimal.length() > 10 ? valueBeforeDecimal.substring(0, 10) : valueBeforeDecimal;
		int result;
		try {
			result = Integer.parseInt(valueBeforeDecimalAsString);
		} catch (NumberFormatException e) {
			result = Integer.MAX_VALUE;
		}
		return isNegative ? -result : result;
	}

	/**
	 * Returns the value of this {@code BigNumber} as a {@code long}.
	 * <p>
	 * If the integer part exceeds 19 digits, only the first 19 digits are used.
	 * If parsing fails, {@code Long.MAX_VALUE} is returned (with sign).
	 *
	 * @return the long value represented by this object, or {@code Long.MAX_VALUE} on error
	 */
	@Override
	public long longValue() {
		String valueBeforeDecimalAsString = valueBeforeDecimal.length() > 19 ? valueBeforeDecimal.substring(0, 19) : valueBeforeDecimal;
		long result;
		try {
			result = Long.parseLong(valueBeforeDecimalAsString);
		} catch (NumberFormatException e) {
			result = Long.MAX_VALUE;
		}
		return isNegative ? -result : result;
	}

	/**
	 * Returns the value of this {@code BigNumber} as a {@code float}.
	 * <p>
	 * Only the integer part is used for conversion. If parsing fails,
	 * returns {@code Float.POSITIVE_INFINITY} or {@code Float.NEGATIVE_INFINITY} depending on sign.
	 *
	 * @return the float value represented by this object, or infinity on error
	 */
	@Override
	public float floatValue() {
		try {
			BigDecimal bigDecimal = new BigDecimal((isNegative ? "-" : "") + valueBeforeDecimal);
			return bigDecimal.floatValue();
		} catch (NumberFormatException e) {
			return isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		}
	}

	/**
	 * Returns the value of this {@code BigNumber} as a {@code double}.
	 * <p>
	 * Only the integer part is used for conversion. If parsing fails,
	 * returns {@code Double.POSITIVE_INFINITY} or {@code Double.NEGATIVE_INFINITY} depending on sign.
	 *
	 * @return the double value represented by this object, or infinity on error
	 */
	@Override
	public double doubleValue() {
		try {
			BigDecimal bigDecimal = new BigDecimal((isNegative ? "-" : "") + valueBeforeDecimal);
			return bigDecimal.doubleValue();
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
	public BigNumber min(@NonNull final BigNumber other) {
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
	public BigNumber max(@NonNull final BigNumber other) {
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
	 * Checks if this BigNumber represents an integer value (i.e., has no decimal part).
	 *
	 * @return true if this number is an integer, false otherwise
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isInteger() {
		return !hasDecimals();
	}

	/**
	 * Returns a string representation of this {@code BigNumber} using the current locale.
	 *
	 * @return the string representation of this number
	 */
	@Override
	public String toString() {
		return formatToString(this.locale, false);
	}

	/**
	 * Returns the string representation of this number in the specified locale,
	 * using the locale's decimal and grouping separators.
	 *
	 * @param locale
	 * 	the locale to use for formatting
	 *
	 * @return string representation with grouping
	 */
	public String toString(@NonNull final Locale locale) {
		return formatToString(locale, false);
	}

	/**
	 * Returns the string representation of this number in the specified locale,
	 * optionally using grouping separators.
	 *
	 * @param locale
	 * 	the locale to use for formatting
	 * @param useGrouping
	 * 	whether grouping separators should be used
	 *
	 * @return string representation in the given locale
	 */
	public String toString(@NonNull final Locale locale, final boolean useGrouping) {
		return formatToString(locale, useGrouping);
	}

	/**
	 * Returns the string representation of this number using the current locale,
	 * optionally using grouping separators.
	 *
	 * @param useGrouping
	 * 	whether grouping separators should be used
	 *
	 * @return string representation in the current locale
	 */
	public String toString(final boolean useGrouping) {
		return formatToString(this.locale, useGrouping);
	}

	/**
	 * Returns the string representation of this number using its locale,
	 * optionally with grouping separators.
	 *
	 * @return string representation in the object's locale
	 */
	public String toStringWithGrouping() {
		return formatToString(this.locale, true);
	}

	/**
	 * Formats this {@code BigNumber} as a string according to the specified {@link Locale} and grouping option.
	 * <p>
	 * The method uses the locale's decimal and grouping separators. If {@code useGrouping} is true,
	 * grouping separators are applied to the integer part. If the decimal part is blank or zero,
	 * it is omitted from the result.
	 *
	 * @param locale
	 * 	the {@link Locale} to use for formatting
	 * @param useGrouping
	 * 	whether to use grouping separators in the integer part
	 *
	 * @return the localized string representation of this number
	 */
	private String formatToString(@NonNull final Locale locale, final boolean useGrouping) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		String decimalSeparator = String.valueOf(symbols.getDecimalSeparator());

		String newValueAfterDecimal = valueAfterDecimal.isBlank() || valueAfterDecimal.equals("0")
			                              ? ""
			                              : valueAfterDecimal;

		if (newValueAfterDecimal.isEmpty()) {
			decimalSeparator = "";
		}

		String integerPart = useGrouping
			                     ? bigNumberParser.getGroupedBeforeDecimal(valueBeforeDecimal, symbols.getGroupingSeparator()).toString()
			                     : valueBeforeDecimal;

		String localized = integerPart + decimalSeparator + newValueAfterDecimal;
		return isNegative ? "-" + localized : localized;
	}

	/**
	 * Compares this {@code BigNumber} with the specified {@code BigNumber} for order.
	 *
	 * @param other
	 * 	the {@code BigNumber} to be compared.
	 *
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 * 	specified object.
	 */
	@Override
	public int compareTo(@NonNull final BigNumber other) {
		return this.toBigDecimal().compareTo(other.toBigDecimal());
	}

	/**
	 * Creates and returns a copy of this BigNumber.
	 *
	 * @return a new BigNumber instance with the same value and properties as this one
	 */
	public BigNumber clone() {
		return new BigNumber(this);
	}

}

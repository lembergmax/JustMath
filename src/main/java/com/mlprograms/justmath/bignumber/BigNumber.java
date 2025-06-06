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

	public BigNumber(@NonNull String number) {
		this(bigNumberParser.parseAutoDetect(number));
	}

	public BigNumber(@NonNull String number, @NonNull Locale locale) {
		this(bigNumberParser.parse(number, locale));
	}

	public BigNumber(@NonNull String number, @NonNull MathContext mathContext) {
		this(bigNumberParser.parseAutoDetect(number), mathContext);
	}

	public BigNumber(@NonNull String number, @NonNull Locale locale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(number, locale), mathContext);
	}

	public BigNumber(@NonNull String number, @NonNull Locale fromLocale, @NonNull Locale toLocale) {
		this(bigNumberParser.parse(bigNumberParser.parse(number, fromLocale).toString(), toLocale));
	}

	public BigNumber(@NonNull String number, @NonNull Locale fromLocale, @NonNull Locale toLocale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(bigNumberParser.parse(number, fromLocale).toString(), toLocale), mathContext);
	}

	public BigNumber(@NonNull BigNumber other) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.calculatorEngine = other.calculatorEngine;
		this.trigonometricMode = other.trigonometricMode;
		this.mathContext = other.mathContext;
	}

	public BigNumber(@NonNull BigNumber other, @NonNull MathContext mathContext) {
		this(other);
		this.mathContext = mathContext;
	}

	public BigNumber(@NonNull BigNumber other, @NonNull Locale newLocale) {
		this(bigNumberParser.parse(other.toString(), newLocale));
	}

	public BigNumber(@NonNull BigNumber other, @NonNull Locale newLocale, @NonNull MathContext mathContext) {
		this(bigNumberParser.parse(other.toString(), newLocale), mathContext);
	}

	public BigNumber(@NonNull Locale locale, @NonNull String valueBeforeDecimal, @NonNull String valueAfterDecimal,
	                 boolean isNegative, @NonNull MathContext mathContext) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.mathContext = mathContext;
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	@Builder
	public BigNumber(@NonNull Locale locale, @NonNull String valueBeforeDecimal, @NonNull String valueAfterDecimal,
	                 boolean isNegative, MathContext mathContext, TrigonometricMode trigonometricMode) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		if (mathContext != null) this.mathContext = mathContext;
		if (trigonometricMode != null) this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(this.trigonometricMode);
	}

	public BigNumber(@NonNull BigDecimal bigDecimal, @NonNull Locale locale) {
		this(bigDecimal.toPlainString(), locale);
	}

	public BigNumber(@NonNull Locale locale, @NonNull String valueBeforeDecimal, @NonNull String valueAfterDecimal,
	                 boolean isNegative, @NonNull TrigonometricMode trigonometricMode) {
		this.locale = locale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.trigonometricMode = trigonometricMode;
		this.mathContext = new MathContext(1000);
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	public BigNumber(@NonNull BigNumber other, @NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.mathContext = mathContext;
		this.trigonometricMode = trigonometricMode;
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	public BigNumber(@NonNull String number, @NonNull Locale locale, @NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
		this(bigNumberParser.parse(number, locale), mathContext, trigonometricMode);
	}

	public BigNumber(@NonNull BigNumber other, @NonNull Locale newLocale, @NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
		this(bigNumberParser.parse(other.toString(), newLocale), mathContext, trigonometricMode);
	}

	public BigNumber(@NonNull Locale locale) {
		this.locale = locale;
		this.valueBeforeDecimal = "0";
		this.valueAfterDecimal = "0";
		this.isNegative = false;
		this.mathContext = new MathContext(1000);
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	public BigNumber(@NonNull BigDecimal bigDecimal) {
		this(bigDecimal, Locale.getDefault());
	}

	public BigNumber(@NonNull BigNumber other, boolean newSign) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = newSign;
		this.calculatorEngine = other.calculatorEngine;
		this.trigonometricMode = other.trigonometricMode;
		this.mathContext = other.mathContext;
	}

	/**
	 * Adds the given BigNumber to this BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to add
	 *
	 * @return a new BigNumber representing the sum
	 */
	public BigNumber add(BigNumber other) {
		return new BigNumber(toBigDecimal().add(other.toBigDecimal()).toPlainString());
	}

	/**
	 * Subtracts the given BigNumber from this BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to subtract
	 *
	 * @return a new BigNumber representing the difference
	 */
	public BigNumber subtract(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().subtract(bigDecimal).toPlainString());
	}

	/**
	 * Multiplies this BigNumber by the given BigNumber.
	 *
	 * @param other
	 * 	the BigNumber to multiply by
	 *
	 * @return a new BigNumber representing the product
	 */
	public BigNumber multiply(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().multiply(bigDecimal).toPlainString());
	}

	/**
	 * Divides this BigNumber by the given BigNumber.
	 *
	 * @param other
	 * 	the BigNumber divisor
	 *
	 * @return a new BigNumber representing the quotient
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().divide(bigDecimal, getMathContext()).toPlainString());
	}

	/**
	 * Raises this BigNumber to the power of the given exponent.
	 *
	 * @param exponent
	 * 	the exponent as a BigNumber
	 *
	 * @return a new BigNumber representing the result
	 */
	public BigNumber pow(BigNumber exponent) {
		return new BigNumber(BigDecimalMath.pow(toBigDecimal(), new BigDecimal(exponent.toString()), getMathContext()).toPlainString(), locale);
	}

	/**
	 * Calculates the square root of this BigNumber.
	 *
	 * @return a new BigNumber representing the square root
	 */
	public BigNumber squareRoot() {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), BigDecimal.TWO, mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the cubic root of this BigNumber.
	 *
	 * @return a new BigNumber representing the cubic root
	 */
	public BigNumber cubicRoot() {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), new BigDecimal("3"), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the nth root of this BigNumber.
	 *
	 * @param n
	 * 	the root degree as a BigNumber; must be non-negative
	 *
	 * @return a new BigNumber representing the nth root
	 *
	 * @throws IllegalArgumentException
	 * 	if n is negative
	 */
	public BigNumber nthRoot(BigNumber n) {
		if (n.isNegative()) {
			throw new IllegalArgumentException("Cannot calculate nth root with negative n.");
		}
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), n.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the factorial of this BigNumber.
	 *
	 * @return a new BigNumber representing the factorial
	 */
	public BigNumber factorial() {
		return new BigNumber(BigDecimalMath.factorial(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the base-2 logarithm of this BigNumber.
	 *
	 * @return a new BigNumber representing the log base 2
	 */
	public BigNumber log2() {
		return new BigNumber(BigDecimalMath.log2(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the base-10 logarithm of this BigNumber.
	 *
	 * @return a new BigNumber representing the log base 10
	 */
	public BigNumber log10() {
		return new BigNumber(BigDecimalMath.log10(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the natural logarithm (ln) of this BigNumber.
	 *
	 * @return a new BigNumber representing the natural logarithm
	 */
	public BigNumber ln() {
		return new BigNumber(BigDecimalMath.log(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the logarithm of this BigNumber with a given base.
	 *
	 * @param base
	 * 	the logarithm base; must be positive and non-zero
	 *
	 * @return a new BigNumber representing the logarithm with the given base
	 *
	 * @throws IllegalArgumentException
	 * 	if base is negative or zero
	 */
	public BigNumber logBase(BigNumber base) {
		if (base.isNegative() || base.equals(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("Base must be positive and non-zero.");
		}

		BigDecimal logBase = BigDecimalMath.log(toBigDecimal(), mathContext);
		BigDecimal logOfBase = BigDecimalMath.log(base.toBigDecimal(), mathContext);
		return new BigNumber(logBase.divide(logOfBase, mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the sine of this number.
	 * <p>
	 * If the trigonometric mode is DEG, interprets this value as degrees and converts to radians.
	 * If the mode is RAD, interprets this value directly as radians.
	 *
	 * @return the sine of this number
	 */
	public BigNumber sin() {
		BigDecimal radians = trigonometricMode == TrigonometricMode.DEG ?
			                     toBigDecimal().multiply(BigDecimalMath.pi(mathContext)).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext) :
			                     toBigDecimal();
		return new BigNumber(BigDecimalMath.sin(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the cosine of this number.
	 * <p>
	 * If the trigonometric mode is DEG, interprets this value as degrees and converts to radians.
	 * If the mode is RAD, interprets this value directly as radians.
	 *
	 * @return the cosine of this number
	 */
	public BigNumber cos() {
		BigDecimal radians = trigonometricMode == TrigonometricMode.DEG ?
			                     toBigDecimal().multiply(BigDecimalMath.pi(mathContext)).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext) :
			                     toBigDecimal();
		return new BigNumber(BigDecimalMath.cos(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the tangent of this number.
	 * <p>
	 * If the trigonometric mode is DEG, interprets this value as degrees and converts to radians.
	 * If the mode is RAD, interprets this value directly as radians.
	 *
	 * @return the tangent of this number
	 */
	public BigNumber tan() {
		BigDecimal radians = trigonometricMode == TrigonometricMode.DEG ?
			                     toBigDecimal().multiply(BigDecimalMath.pi(mathContext)).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext) :
			                     toBigDecimal();
		return new BigNumber(BigDecimalMath.tan(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the hyperbolic sine of this number (interpreted as radians).
	 *
	 * @return the hyperbolic sine of this number in radians
	 */
	public BigNumber sinh() {
		return new BigNumber(BigDecimalMath.sinh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the hyperbolic cosine of this number (interpreted as radians).
	 *
	 * @return the hyperbolic cosine of this number in radians
	 */
	public BigNumber cosh() {
		return new BigNumber(BigDecimalMath.cosh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the hyperbolic tangent of this number (interpreted as radians).
	 *
	 * @return the hyperbolic tangent of this number in radians
	 */
	public BigNumber tanh() {
		return new BigNumber(BigDecimalMath.tanh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the arcsine (inverse sine) of this number.
	 * <p>
	 * The returned value is in degrees if trigonometric mode is DEG, otherwise in radians.
	 *
	 * @return the arcsine of this number in the current trigonometric mode
	 */
	public BigNumber asin() {
		BigDecimal result = BigDecimalMath.asin(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal()).divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Returns the arccosine (inverse cosine) of this number.
	 * <p>
	 * The returned value is in degrees if trigonometric mode is DEG, otherwise in radians.
	 *
	 * @return the arccosine of this number in the current trigonometric mode
	 */
	public BigNumber acos() {
		BigDecimal result = BigDecimalMath.acos(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal()).divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Returns the arctangent (inverse tangent) of this number.
	 * <p>
	 * The returned value is in degrees if trigonometric mode is DEG, otherwise in radians.
	 *
	 * @return the arctangent of this number in the current trigonometric mode
	 */
	public BigNumber atan() {
		BigDecimal result = BigDecimalMath.atan(toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = result.multiply(ONE_HUNDRED_EIGHTY.toBigDecimal()).divide(BigDecimalMath.pi(mathContext), mathContext);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Returns the inverse hyperbolic sine of this number.
	 * The result is in radians.
	 *
	 * @return the inverse hyperbolic sine (asinh) in radians
	 */
	public BigNumber asinh() {
		return new BigNumber(BigDecimalMath.asinh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the inverse hyperbolic cosine of this number.
	 * The result is in radians.
	 *
	 * @return the inverse hyperbolic cosine (acosh) in radians
	 */
	public BigNumber acosh() {
		return new BigNumber(BigDecimalMath.acosh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the inverse hyperbolic tangent of this number.
	 * The result is in radians.
	 *
	 * @return the inverse hyperbolic tangent (atanh) in radians
	 */
	public BigNumber atanh() {
		return new BigNumber(BigDecimalMath.atanh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the angle (in radians) between the positive x-axis and the point (this, other).
	 *
	 * @param other
	 * 	the y-coordinate
	 *
	 * @return the angle in radians
	 */
	public BigNumber atan2(BigNumber other) {
		return new BigNumber(BigDecimalMath.atan2(toBigDecimal(), other.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the cotangent of this number (interpreted as radians).
	 *
	 * @return the cotangent of this number in radians
	 */
	public BigNumber cot() {
		return new BigNumber(BigDecimalMath.cot(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the inverse cotangent (arccot) of this number.
	 * The result is in radians.
	 *
	 * @return the inverse cotangent (arccot) in radians
	 */
	public BigNumber acot() {
		return new BigNumber(BigDecimalMath.acot(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the inverse hyperbolic cotangent of this number.
	 * The result is in radians.
	 *
	 * @return the inverse hyperbolic cotangent (acoth) in radians
	 */
	public BigNumber acoth() {
		return new BigNumber(BigDecimalMath.acoth(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Returns the hyperbolic cotangent of this number (interpreted as radians).
	 *
	 * @return the hyperbolic cotangent (coth) in radians
	 */
	public BigNumber coth() {
		return new BigNumber(BigDecimalMath.coth(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the combination, also known as "n choose k".
	 * <p>
	 * The combination is a mathematical expression that represents the number of ways to choose
	 * k items from a set of n items without regard to order. It's denoted as nCr or (n k) and is calculated as:
	 * <p>
	 * nCr = n! / (k! * (n-k)!)
	 * <p>
	 * This method uses an optimized approach to avoid calculating factorials directly, which can lead to overflow
	 * for large values of n.
	 *
	 * @param k
	 * 	The number of items to choose (0 <= k <= n).
	 *
	 * @return The combination nCr.
	 *
	 * @throws IllegalArgumentException
	 * 	If k is greater than n (invalid input).
	 */
	public BigNumber combination(BigNumber k) {
		if (hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Combination requires integer values for both n and k.");
		}

		if (k.compareTo(this) > 0) {
			throw new IllegalArgumentException("Cannot calculate combinations: k cannot be greater than n.");
		}

		if (k.equals(BigNumbers.ZERO) || k.equals(this)) {
			return BigNumbers.ONE;
		}

		k = k.min(subtract(k));
		BigNumber c = BigNumbers.ONE;
		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(k); i = i.add(BigNumbers.ONE)) {
			c = c.multiply(subtract(i)).divide(i.add(BigNumbers.ONE));
		}

		return c;
	}

	/**
	 * Calculates the number of permutations (k-permutations) of n (the current object) items taken k at a time.
	 * <p>
	 * A permutation is an arrangement of objects in a specific order. The number of k-permutations of n items,
	 * denoted as nPk or P(n, k), is the number of ways to select and order k items from a set of n items.
	 * <p>
	 * Mathematically, nPk is calculated as:
	 * nPk = n! / (n-k)!
	 *
	 * @param k
	 * 	The number of items to choose and order (0 <= k <= n).
	 *
	 * @return The number of k-permutations of n items.
	 *
	 * @throws IllegalArgumentException
	 * 	If k is greater than n.
	 */
	public BigNumber permutation(BigNumber k) {
		if (hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Permutations requires integer values for both n and k.");
		}

		if (k.compareTo(this) > 0) {
			throw new IllegalArgumentException("Cannot calculate permutations: k cannot be greater than n.");
		}

		BigNumber nFactorial = factorial();
		BigNumber nMinusKFactorial = subtract(k).factorial();
		return nFactorial.divide(nMinusKFactorial);
	}

	/**
	 * Converts polar coordinates to Cartesian coordinates.
	 * Assumes this BigNumber is the radius (r) and theta is the angle.
	 * The angle theta is interpreted according to its trigonometric mode (DEG or RAD).
	 *
	 * @param theta
	 * 	the angle in degrees or radians as a BigNumber
	 *
	 * @return a BigNumberCoordinate representing the Cartesian coordinates (x, y)
	 */
	public BigNumberCoordinate polarToCartesianCoordinates(BigNumber theta) {
		BigNumber x = multiply(theta.cos());
		BigNumber y = multiply(theta.sin());
		return new BigNumberCoordinate(x, y);
	}

	/**
	 * Converts Cartesian coordinates (this, y) to polar coordinates.
	 * Assumes this BigNumber is the x-coordinate and y is the y-coordinate.
	 * The returned BigNumberCoordinate contains:
	 * - r: the distance from the origin to the point (x, y)
	 * - theta: the angle in degrees between the positive x-axis and the point (x, y)
	 *
	 * @param y
	 * 	the y-coordinate as a BigNumber
	 *
	 * @return a BigNumberCoordinate representing the polar coordinates (r, theta in degrees)
	 */
	public BigNumberCoordinate cartesianToPolarCoordinates(BigNumber y) {
		BigNumber r = pow(BigNumbers.TWO).add(y.pow(BigNumbers.TWO)).squareRoot();
		BigNumber theta = y.atan2(this);
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
	public boolean equals(BigNumber other) {
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

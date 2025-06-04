package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.api.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.ArithmeticOperator.*;

/**
 * Immutable representation of a numeric value with optional decimal part and sign.
 * <p>
 * Use the constructors which internally rely on {@link BigNumberParser} to parse input strings.
 */
@Getter
public class BigNumber {

	/**
	 * Shared instance of the parser used to convert input strings into BigNumber objects.
	 * This static parser ensures consistent parsing logic across all BigNumber instances.
	 */
	private static final BigNumberParser bigNumberParser = new BigNumberParser();
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
	 * Shared instance of the CalculatorEngine used for performing arithmetic operations.
	 * This static engine allows all BigNumber instances to use the same precision settings.
	 */
	private CalculatorEngine calculatorEngine;
	/**
	 * The trigonometric mode (e.g., DEG, RAD, GRAD) used for trigonometric calculations.
	 * Defaults to DEG (degrees).
	 */
	private TrigonometricMode trigonometricMode = TrigonometricMode.DEG;

	/**
	 * Private builder constructor used internally.
	 */
	@Builder(access = AccessLevel.PUBLIC)
	private BigNumber(@NonNull Locale currentLocale, @NonNull String valueBeforeDecimal, @NonNull String valueAfterDecimal,
	                  boolean isNegative) {
		this.locale = currentLocale;
		this.valueBeforeDecimal = valueBeforeDecimal;
		this.valueAfterDecimal = valueAfterDecimal;
		this.isNegative = isNegative;
		this.calculatorEngine = new CalculatorEngine(trigonometricMode);
	}

	/**
	 * Constructs a BigNumber by auto-detecting the locale of the input string.
	 */
	public BigNumber(@NonNull String number) {
		this(bigNumberParser.parseAutoDetect(number));
	}

	/**
	 * Constructs a BigNumber by parsing the given number string using the specified currentLocale.
	 */
	public BigNumber(@NonNull String number, @NonNull Locale currentLocale) {
		this(bigNumberParser.parse(number, currentLocale));
	}

	/**
	 * Constructs a BigNumber by converting an existing BigNumber to a different locale.
	 */
	public BigNumber(@NonNull BigNumber number, @NonNull Locale targetLocale) {
		this(bigNumberParser.parse(number.toString(), targetLocale));
	}

	/**
	 * Constructs a BigNumber by parsing the input string using a source locale
	 * and converting it to the target locale.
	 */
	public BigNumber(@NonNull String input, @NonNull Locale fromLocale, @NonNull Locale targetLocale) {
		this(bigNumberParser.parse(bigNumberParser.parse(input, fromLocale).toString(), targetLocale));
	}

	/**
	 * Copy constructor.
	 */
	public BigNumber(@NonNull BigNumber other) {
		this.locale = other.locale;
		this.valueBeforeDecimal = other.valueBeforeDecimal;
		this.valueAfterDecimal = other.valueAfterDecimal;
		this.isNegative = other.isNegative;
		this.calculatorEngine = other.getCalculatorEngine();
	}

	/**
	 * Adds this BigNumber to another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to add
	 *
	 * @return the sum as a new BigNumber
	 */
	public BigNumber add(BigNumber other) {
		String expression = this + ADD.getOperator() + other.toString();
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result);
	}

	/**
	 * Subtracts another BigNumber from this BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to subtract
	 *
	 * @return the difference as a new BigNumber
	 */
	public BigNumber subtract(BigNumber other) {
		String expression = this + SUBTRACT.getOperator() + other.toString();
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result);
	}

	/**
	 * Multiplies this BigNumber with another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to multiply with
	 *
	 * @return the product as a new BigNumber
	 */
	public BigNumber multiply(BigNumber other) {
		String expression = this + MULTIPLY.getOperator() + other.toString();
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result);
	}

	/**
	 * Divides this BigNumber by another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to divide by
	 *
	 * @return the quotient as a new BigNumber
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(BigNumber other) {
		String expression = this + DIVIDE.getOperator() + other.toString();
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result);
	}

	/**
	 * Raises a BigNumber to the power of another BigNumber.
	 *
	 * @param base
	 * 	the base BigNumber
	 * @param exponent
	 * 	the exponent BigNumber
	 *
	 * @return result of base ^ exponent
	 */
	public BigNumber pow(BigNumber base, BigNumber exponent) {
		BigNumber result = calculatorEngine.evaluate(base.toString() + POWER.getOperator() + exponent.toString());
		return new BigNumber(result, base.locale);
	}

	/**
	 * Computes the square root of a BigNumber.
	 *
	 * @return square root of number
	 */
	public BigNumber root() {
		BigNumber result = calculatorEngine.evaluate(ROOT.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the cube root of a BigNumber.
	 *
	 * @return cube root of number
	 */
	public BigNumber thirdRoot() {
		BigNumber result = calculatorEngine.evaluate(CUBIC_ROOT.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the factorial of a BigNumber.
	 *
	 * @return factorial of number
	 */
	public BigNumber factorial() {
		BigNumber result = calculatorEngine.evaluate(this + FACTORIAL.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the sine of the given BigNumber.
	 *
	 * @return sine of number
	 */
	public BigNumber sin() {
		BigNumber result = calculatorEngine.evaluate(SIN.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the cosine of the given BigNumber.
	 *
	 * @return cosine of number
	 */
	public BigNumber cos() {
		BigNumber result = calculatorEngine.evaluate(COS.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the tangent of the given BigNumber.
	 *
	 * @return tangent of number
	 */
	public BigNumber tan() {
		BigNumber result = calculatorEngine.evaluate(TAN.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the hyperbolic sine of the given BigNumber.
	 *
	 * @return sinh(number)
	 */
	public BigNumber sinh() {
		BigNumber result = calculatorEngine.evaluate(SINH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the hyperbolic cosine of the given BigNumber.
	 *
	 * @return cosh(number)
	 */
	public BigNumber cosh() {
		BigNumber result = calculatorEngine.evaluate(COSH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the hyperbolic tangent of the given BigNumber.
	 *
	 * @return tanh(number)
	 */
	public BigNumber tanh() {
		BigNumber result = calculatorEngine.evaluate(TANH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse sine (arcsin) of the given BigNumber.
	 *
	 * @return asin(number)
	 */
	public BigNumber asin() {
		BigNumber result = calculatorEngine.evaluate(ASIN.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse cosine (arccos) of the given BigNumber.
	 *
	 * @return acos(number)
	 */
	public BigNumber acos() {
		BigNumber result = calculatorEngine.evaluate(ACOS.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse tangent (arctan) of the given BigNumber.
	 *
	 * @return atan(number)
	 */
	public BigNumber atan() {
		BigNumber result = calculatorEngine.evaluate(ATAN.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse hyperbolic sine of the given BigNumber.
	 *
	 * @return asinh(number)
	 */
	public BigNumber asinh() {
		BigNumber result = calculatorEngine.evaluate(ASINH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse hyperbolic cosine of the given BigNumber.
	 *
	 * @return acosh(number)
	 */
	public BigNumber acosh() {
		BigNumber result = calculatorEngine.evaluate(ACOSH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the inverse hyperbolic tangent of the given BigNumber.
	 *
	 * @return atanh(number)
	 */
	public BigNumber atanh() {
		BigNumber result = calculatorEngine.evaluate(ATANH.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the base-10 logarithm of the given BigNumber.
	 *
	 * @return log10(number)
	 */
	public BigNumber log10() {
		BigNumber result = calculatorEngine.evaluate(LOG10.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the natural logarithm (base e) of the given BigNumber.
	 *
	 * @return ln(number)
	 */
	public BigNumber ln() {
		BigNumber result = calculatorEngine.evaluate(LN.getOperator() + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
	}

	/**
	 * Computes the logarithm of the given number to a specified base.
	 *
	 * @param base
	 * 	the base of the logarithm
	 *
	 * @return log_base(number)
	 */
	public BigNumber logBase(int base) {
		BigNumber result = calculatorEngine.evaluate(LOG_BASE.getOperator() + base + LEFT_PARENTHESIS.getOperator() + this + RIGHT_PARENTHESIS.getOperator());
		return new BigNumber(result, locale);
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
			       .currentLocale(this.locale)
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
	 * 	the trigonometric mode to set (e.g., DEG, RAD, GRAD)
	 */
	public void setTrigonometricMode(@NonNull TrigonometricMode trigonometricMode) {
		this.trigonometricMode = trigonometricMode;
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
		char decimalSeparator = symbols.getDecimalSeparator();

		String localized = valueBeforeDecimal + decimalSeparator + valueAfterDecimal;
		return isNegative ? "-" + localized : localized;
	}

}

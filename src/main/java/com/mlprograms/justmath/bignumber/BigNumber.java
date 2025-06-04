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
	 * @return base ^ exponent
	 */
	public BigNumber pow(BigNumber base, BigNumber exponent) {
		String expression = base + POWER.getOperator() + exponent;
		return evaluate(expression, base.locale);
	}

	/**
	 * Computes the square root of this BigNumber.
	 *
	 * @return √number
	 */
	public BigNumber root() {
		return evaluateUnary(ROOT_T.getOperator());
	}

	/**
	 * Computes the cube root of this BigNumber.
	 *
	 * @return ∛number
	 */
	public BigNumber thirdRoot() {
		return evaluateUnary(CUBIC_ROOT_T.getOperator());
	}

	/**
	 * Computes the factorial of this BigNumber.
	 *
	 * @return number!
	 */
	public BigNumber factorial() {
		return evaluate(this + FACTORIAL.getOperator(), locale);
	}

	/**
	 * Computes the base-10 logarithm.
	 */
	public BigNumber log10() {
		return evaluateUnary(LOG10.getOperator());
	}

	/**
	 * Computes the natural logarithm (base e).
	 */
	public BigNumber ln() {
		return evaluateUnary(LN.getOperator());
	}

	/**
	 * Computes log base-N of this BigNumber.
	 *
	 * @param base
	 * 	logarithmic base
	 *
	 * @return log_base(number)
	 */
	public BigNumber logBase(int base) {
		String expression = LOG_BASE.getOperator() + base + wrapInParentheses(this.toString());
		return evaluate(expression, locale);
	}

	/**
	 * Computes the sine of this BigNumber.
	 *
	 * @return sin(number)
	 */
	public BigNumber sin() {
		return evaluateUnary(SIN.getOperator());
	}

	/**
	 * Computes the cosine of this BigNumber.
	 *
	 * @return cos(number)
	 */
	public BigNumber cos() {
		return evaluateUnary(COS.getOperator());
	}

	/**
	 * Computes the tangent of this BigNumber.
	 *
	 * @return tan(number)
	 */
	public BigNumber tan() {
		return evaluateUnary(TAN.getOperator());
	}

	/**
	 * Computes the hyperbolic sine of this BigNumber.
	 *
	 * @return sinh(number)
	 */
	public BigNumber sinh() {
		return evaluateUnary(SINH.getOperator());
	}

	/**
	 * Computes the hyperbolic cosine of this BigNumber.
	 *
	 * @return cosh(number)
	 */
	public BigNumber cosh() {
		return evaluateUnary(COSH.getOperator());
	}

	/**
	 * Computes the hyperbolic tangent of this BigNumber.
	 *
	 * @return tanh(number)
	 */
	public BigNumber tanh() {
		return evaluateUnary(TANH.getOperator());
	}

	/**
	 * Computes the arcsine (inverse sine) of this BigNumber.
	 *
	 * @return asin(number)
	 */
	public BigNumber asin() {
		return evaluateUnary(ASIN.getOperator());
	}

	/**
	 * Computes the arccosine (inverse cosine) of this BigNumber.
	 *
	 * @return acos(number)
	 */
	public BigNumber acos() {
		return evaluateUnary(ACOS.getOperator());
	}

	/**
	 * Computes the arctangent (inverse tangent) of this BigNumber.
	 *
	 * @return atan(number)
	 */
	public BigNumber atan() {
		return evaluateUnary(ATAN.getOperator());
	}

	/**
	 * Computes the inverse hyperbolic sine of this BigNumber.
	 *
	 * @return asinh(number)
	 */
	public BigNumber asinh() {
		return evaluateUnary(ASINH.getOperator());
	}

	/**
	 * Computes the inverse hyperbolic cosine of this BigNumber.
	 *
	 * @return acosh(number)
	 */
	public BigNumber acosh() {
		return evaluateUnary(ACOSH.getOperator());
	}

	/**
	 * Computes the inverse hyperbolic tangent of this BigNumber.
	 *
	 * @return atanh(number)
	 */
	public BigNumber atanh() {
		return evaluateUnary(ATANH.getOperator());
	}

	/**
	 * Evaluates the given mathematical expression using the current CalculatorEngine,
	 * and returns a new BigNumber in the specified locale.
	 *
	 * @param expression
	 * 	the mathematical expression to evaluate
	 * @param locale
	 * 	the locale to use for the resulting BigNumber
	 *
	 * @return a new BigNumber representing the result in the given locale
	 */
	private BigNumber evaluate(String expression, Locale locale) {
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result, locale);
	}

	/**
	 * Evaluates a unary operation (such as sin, cos, log) on this BigNumber.
	 *
	 * @param operator
	 * 	the unary operator to apply
	 *
	 * @return a new BigNumber representing the result
	 */
	private BigNumber evaluateUnary(String operator) {
		String expression = operator + wrapInParentheses(toString());
		return evaluate(expression, locale);
	}

	/**
	 * Evaluates a binary operation (such as addition, subtraction, multiplication, or division)
	 * between two BigNumber instances using the specified operator.
	 *
	 * @param left
	 * 	the left operand BigNumber
	 * @param right
	 * 	the right operand BigNumber
	 * @param operator
	 * 	the binary operator to apply (e.g., "+", "-", "*", "/")
	 *
	 * @return a new BigNumber representing the result of the operation
	 */
	private BigNumber evaluateBinary(BigNumber left, BigNumber right, String operator) {
		String expression = left + operator + right.toString();
		BigNumber result = calculatorEngine.evaluate(expression);
		return new BigNumber(result);
	}

	/**
	 * Wraps the given string in parentheses using the operator constants.
	 *
	 * @param s
	 * 	the string to wrap
	 *
	 * @return the string wrapped in parentheses
	 */
	private String wrapInParentheses(String s) {
		return LEFT_PARENTHESIS.getOperator() + s + RIGHT_PARENTHESIS.getOperator();
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

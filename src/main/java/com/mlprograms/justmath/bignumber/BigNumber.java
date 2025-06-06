package com.mlprograms.justmath.bignumber;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.api.CalculatorEngine;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Immutable representation of a numeric value with optional decimal part and sign.
 * <p>
 * Use the constructors which internally rely on {@link BigNumberParser} to parse input strings.
 */
@Getter
public class BigNumber implements Comparable<BigNumber> {

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
	// TODO: write javadoc
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

	public BigNumber add(BigNumber other) {
		return new BigNumber(toBigDecimal().add(other.toBigDecimal()).toPlainString());
	}

	public BigNumber subtract(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().subtract(bigDecimal).toPlainString());
	}

	public BigNumber multiply(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().multiply(bigDecimal).toPlainString());
	}

	public BigNumber divide(BigNumber other) {
		BigDecimal bigDecimal = new BigDecimal(other.toString());
		return new BigNumber(toBigDecimal().divide(bigDecimal, getMathContext()).toPlainString());
	}

	public BigNumber pow(BigNumber exponent) {
		return new BigNumber(BigDecimalMath.pow(toBigDecimal(), new BigDecimal(exponent.toString()), getMathContext()).toPlainString(), locale);
	}

	public BigNumber squareRoot() {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), BigDecimal.TWO, mathContext).toPlainString(), locale);
	}

	public BigNumber cubicRoot() {
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), new BigDecimal("3"), mathContext).toPlainString(), locale);
	}

	public BigNumber nthRoot(BigNumber n) {
		if (n.isNegative()) {
			throw new IllegalArgumentException("Cannot calculate nth root with negative n.");
		}
		return new BigNumber(BigDecimalMath.root(toBigDecimal(), n.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber factorial() {
		return new BigNumber(BigDecimalMath.factorial(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber log2() {
		return new BigNumber(BigDecimalMath.log2(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber log10() {
		return new BigNumber(BigDecimalMath.log10(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber ln() {
		return new BigNumber(BigDecimalMath.log(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber logBase(BigNumber base) {
		if (base.isNegative() || base.equals(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("Base must be positive and non-zero.");
		}

		BigDecimal logBase = BigDecimalMath.log(toBigDecimal(), mathContext);
		BigDecimal logOfBase = BigDecimalMath.log(base.toBigDecimal(), mathContext);
		return new BigNumber(logBase.divide(logOfBase, mathContext).toPlainString(), locale);
	}

	public BigNumber sin() {
		return new BigNumber(BigDecimalMath.sin(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber cos() {
		return new BigNumber(BigDecimalMath.cos(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber tan() {
		return new BigNumber(BigDecimalMath.tan(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber sinh() {
		return new BigNumber(BigDecimalMath.sinh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber cosh() {
		return new BigNumber(BigDecimalMath.cosh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber tanh() {
		return new BigNumber(BigDecimalMath.tanh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber asin() {
		return new BigNumber(BigDecimalMath.asin(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber acos() {
		return new BigNumber(BigDecimalMath.acos(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber atan() {
		return new BigNumber(BigDecimalMath.atan(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber asinh() {
		return new BigNumber(BigDecimalMath.asinh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber acosh() {
		return new BigNumber(BigDecimalMath.acosh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber atanh() {
		return new BigNumber(BigDecimalMath.atanh(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber atan2(BigNumber other) {
		return new BigNumber(BigDecimalMath.atan2(toBigDecimal(), other.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber cot() {
		return new BigNumber(BigDecimalMath.cot(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber acot() {
		return new BigNumber(BigDecimalMath.acot(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber acoth() {
		return new BigNumber(BigDecimalMath.acoth(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public BigNumber coth() {
		return new BigNumber(BigDecimalMath.coth(toBigDecimal(), mathContext).toPlainString(), locale);
	}

	private BigNumber evaluate(String expression, Locale locale) {
		BigNumber result = calculatorEngine.evaluate(expression);
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
	 * 	the trigonometric mode to set (e.g., DEG, RAD, GRAD)
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

package com.mlprograms.justmath.api;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.Evaluator;
import com.mlprograms.justmath.calculator.internal.Parser;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.Token;
import com.mlprograms.justmath.calculator.internal.token.Tokenizer;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * ExactCalculatorEngine.java
 * <p>
 * Main entry point for evaluating mathematical expressions as strings with exact precision.
 * Converts the input to tokens, parses them to postfix (RPN), and evaluates the result.
 */
@Getter
public class CalculatorEngine {

	private static final int DEFAULT_DIVISION_PRECISION = 1000;
	/**
	 * Tokenizer instance used to convert input expressions into tokens.
	 */
	private final Tokenizer tokenizer;
	/**
	 * Evaluator instance used to compute the result from postfix token lists.
	 */
	private final Evaluator evaluator;
	/**
	 * Parser instance used to convert infix token lists to postfix notation.
	 */
	private final Parser parser;

	public CalculatorEngine() {
		this(new MathContext(DEFAULT_DIVISION_PRECISION, RoundingMode.HALF_UP), TrigonometricMode.DEG);
	}

	/**
	 * Constructs a CalculatorEngine with the specified division precision and default trigonometric mode (DEG).
	 *
	 * @param divisionPrecision
	 * 	the precision for division operations
	 */
	public CalculatorEngine(int divisionPrecision) {
		this(new MathContext(divisionPrecision, RoundingMode.HALF_UP), TrigonometricMode.DEG);
	}

	/**
	 * Constructs a CalculatorEngine with the specified division precision and trigonometric mode.
	 *
	 * @param divisionPrecision
	 * 	the precision for division operations
	 * @param trigonometricMode
	 * 	the trigonometric mode (DEG or RAD)
	 */
	public CalculatorEngine(int divisionPrecision, @NonNull TrigonometricMode trigonometricMode) {
		this(new MathContext(divisionPrecision, RoundingMode.HALF_UP), trigonometricMode);
	}

	/**
	 * Constructs a CalculatorEngine with the specified MathContext and default trigonometric mode (DEG).
	 *
	 * @param mathContext
	 * 	the MathContext specifying precision and rounding mode
	 */
	public CalculatorEngine(@NonNull MathContext mathContext) {
		this(mathContext, TrigonometricMode.DEG);
	}

	/**
	 * Constructs a CalculatorEngine with the specified trigonometric mode and default MathContext (precision 1000).
	 *
	 * @param trigonometricMode
	 * 	the trigonometric mode (DEG or RAD)
	 */
	public CalculatorEngine(@NonNull TrigonometricMode trigonometricMode) {
		this(new MathContext(DEFAULT_DIVISION_PRECISION, RoundingMode.HALF_UP), trigonometricMode);
	}

	/**
	 * Constructs a CalculatorEngine with the specified MathContext and trigonometric mode.
	 * Initializes the tokenizer, evaluator, and parser components.
	 *
	 * @param mathContext
	 * 	the MathContext specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the trigonometric mode (DEG or RAD)
	 */
	public CalculatorEngine(@NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
		this.tokenizer = new Tokenizer();
		this.evaluator = new Evaluator(mathContext, trigonometricMode);
		this.parser = new Parser();
	}

	/**
	 * Evaluates a given mathematical expression with full BigDecimal precision.
	 *
	 * @param expression
	 * 	input string expression (e.g. "3.5 + sqrt(2)")
	 *
	 * @return result as string, rounded if necessary
	 */
	public BigNumber evaluate(@NonNull String expression) {
		try {
			// Tokenize the input string
			List<Token> tokens = tokenizer.tokenize(expression);

			// Parse to postfix notation using shunting yard algorithm
			List<Token> postfix = parser.toPostfix(tokens);

			// Evaluate the postfix expression to a BigDecimal result
			return new BigNumber(evaluator.evaluate(postfix).toString());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}

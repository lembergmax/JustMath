package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * ExactCalculatorEngine.java
 * <p>
 * Main entry point for evaluating mathematical expressions as strings with exact precision.
 * Converts the input to tokens, parses them to postfix (RPN), and evaluates the result.
 */
public class CalculatorEngine {

	private static final Tokenizer tokenizer = new Tokenizer();
	private static final Evaluator evaluator = new Evaluator();
	private static final Parser parser = new Parser();

	public CalculatorEngine() {
		this(1000, TrigonometricMode.DEG);
	}

	public CalculatorEngine(int divisionPrecision) {
		this(divisionPrecision, TrigonometricMode.DEG);
	}

	public CalculatorEngine(TrigonometricMode mode) {
		this(1000, mode);
	}

	public CalculatorEngine(int divisionPrecision, TrigonometricMode mode) {
		Values.MATH_CONTEXT = new MathContext(divisionPrecision, RoundingMode.HALF_UP);
		Values.MODE = mode;
	}

	/**
	 * Evaluates a given mathematical expression with full BigDecimal precision.
	 *
	 * @param expression
	 * 	input string expression (e.g. "3.5 + sqrt(2)")
	 *
	 * @return result as string, rounded if necessary
	 */
	public static BigNumber evaluate(String expression) {
		try {
			// Tokenize the input string
			List<Token> tokens = tokenizer.tokenize(expression);

			// Parse to postfix notation using shunting yard algorithm
			List<Token> postfix = parser.toPostfix(tokens);

			// Evaluate the postfix expression to a BigDecimal result
			return new BigNumber(evaluator.evaluate(postfix).toString());
		} catch (Exception e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

}

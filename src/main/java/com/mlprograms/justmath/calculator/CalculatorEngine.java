package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.number.BigNumber;

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

	public CalculatorEngine() {
		this(1000, TrigonometricMode.DEG);
	}

	public CalculatorEngine(int precision) {
		this(precision, TrigonometricMode.DEG);
	}

	public CalculatorEngine(TrigonometricMode mode) {
		this(1000, mode);
	}

	public CalculatorEngine(int precision, TrigonometricMode mode) {
		Values.MATH_CONTEXT = new MathContext(precision, RoundingMode.HALF_UP);
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
	public BigNumber evaluate(String expression) {
		try {
			// Tokenize the input string
			List<Token> tokens = Tokenizer.tokenize(expression);

			// Parse to postfix notation using shunting yard algorithm
			List<Token> postfix = Parser.toPostfix(tokens);

			// Evaluate the postfix expression to a BigDecimal result
			return new BigNumber(Evaluator.evaluate(postfix).toString());
		} catch (Exception e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

}

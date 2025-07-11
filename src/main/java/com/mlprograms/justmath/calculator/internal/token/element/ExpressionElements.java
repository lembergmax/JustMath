package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpressionElements {

	private static final Map<String, ExpressionElement> registry = new HashMap<>();

	static {
		List<ExpressionElement> expressionElementList = List.of(
			new Parenthesis(Parenthesis.Type.LEFT),
			new Parenthesis(Parenthesis.Type.RIGHT),
			new Separator(Separator.Type.SEMICOLON),
			//
			new SimpleBinaryOperator("+", 2, BigNumber::add),
			new SimpleBinaryOperator("-", 2, BigNumber::subtract),
			new SimpleBinaryOperator("*", 3, BigNumber::multiply),
			new BinaryOperator("/", 3, BigNumber::divide),
			new SimpleBinaryOperator("%", 3, BigNumber::modulo),
			new BinaryOperator("^", 4, BigNumber::power),
			new BinaryOperator("nPr", 6, BigNumber::permutation),
			new BinaryOperator("nCr", 6, BigNumber::combination),
			//
			new OneArgumentFunction("!", 5, BigNumber::factorial),
			new OneArgumentFunction("sqrt", 4, BigNumber::squareRoot),
			new OneArgumentFunction("√", 4, BigNumber::squareRoot),
			new OneArgumentFunction("cbrt", 4, BigNumber::cubicRoot),
			new OneArgumentFunction("³√", 4, BigNumber::cubicRoot),
			new TwoArgumentFunction("rootn", 4, BigNumber::nthRoot),
			//
			new OneArgumentTrigonometricFunction("sin", 6, BigNumber::sin),
			new OneArgumentTrigonometricFunction("cos", 6, BigNumber::cos),
			new OneArgumentTrigonometricFunction("tan", 6, BigNumber::tan),
			new OneArgumentTrigonometricFunction("cot", 6, BigNumber::cot),
			//
			new OneArgumentFunction("sinh", 6, BigNumber::sinh),
			new OneArgumentFunction("cosh", 6, BigNumber::cosh),
			new OneArgumentFunction("tanh", 6, BigNumber::tanh),
			new OneArgumentFunction("coth", 6, BigNumber::coth),
			//
			new OneArgumentTrigonometricFunction("asin", 6, BigNumber::asin),
			new OneArgumentTrigonometricFunction("acos", 6, BigNumber::acos),
			new OneArgumentTrigonometricFunction("atan", 6, BigNumber::atan),
			new OneArgumentTrigonometricFunction("acot", 6, BigNumber::acot),
			new OneArgumentTrigonometricFunction("sin⁻¹", 6, BigNumber::asin),
			new OneArgumentTrigonometricFunction("cos⁻¹", 6, BigNumber::acos),
			new OneArgumentTrigonometricFunction("tan⁻¹", 6, BigNumber::atan),
			new OneArgumentTrigonometricFunction("cot⁻¹", 6, BigNumber::acot),
			//
			new OneArgumentFunction("asinh", 6, BigNumber::asinh),
			new OneArgumentFunction("acosh", 6, BigNumber::acosh),
			new OneArgumentFunction("atanh", 6, BigNumber::atanh),
			new OneArgumentFunction("acoth", 6, BigNumber::acoth),
			new OneArgumentFunction("sinh⁻¹", 6, BigNumber::asinh),
			new OneArgumentFunction("cosh⁻¹", 6, BigNumber::acosh),
			new OneArgumentFunction("tanh⁻¹", 6, BigNumber::atanh),
			new OneArgumentFunction("coth⁻¹", 6, BigNumber::acoth),
			//
			new OneArgumentFunction("log2", 6, BigNumber::log2),
			new OneArgumentFunction("log10", 6, BigNumber::log10),
			new OneArgumentFunction("ln", 6, BigNumber::ln),
			new TwoArgumentFunction("logbase", 6, BigNumber::logBase),
			//
			new TwoArgumentFunction("atan2", 6, BigNumber::atan2),
			new TwoArgumentFunction("perm", 6, BigNumber::permutation),
			new TwoArgumentFunction("comb", 6, BigNumber::combination),
			//
			new TwoArgumentFunction("LCM", 6, BigNumber::lcm),
			new SimpleTwoArgumentFunction("GCD", 6, BigNumber::gcd),
			//
			new CoordinateFunction("Rec", 6, BigNumber::polarToCartesianCoordinates),
			new SimpleCoordinateFunction("Pol", 6, BigNumber::cartesianToPolarCoordinates),
			//
			new SimpleTwoArgumentFunction("RandInt", 6, BigNumber::randomIntegerForRange)
		);

		for (ExpressionElement expressionElement : expressionElementList) {
			register(expressionElement);
		}
	}

	public static boolean existsExpressionElement(ExpressionElement element) {
		return registry.containsKey(element.getSymbol());
	}

	public static Optional<ExpressionElement> findBySymbol(String symbol) {
		return Optional.ofNullable(registry.get(symbol));
	}

	public static void register(ExpressionElement element) {
		registry.put(element.getSymbol(), element);
	}

}

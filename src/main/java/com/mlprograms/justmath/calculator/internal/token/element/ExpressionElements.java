package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExpressionElements {

	private static final Map<String, ExpressionElement> registry = new HashMap<>();

	static {
		register(new Parenthesis(Parenthesis.Type.LEFT));
		register(new Parenthesis(Parenthesis.Type.RIGHT));
		register(new Separator(Separator.Type.SEMICOLON));
		//
		register(new BinaryOperator("+", 2, (a, b, context, locale) -> a.add(b, locale)));
		register(new BinaryOperator("-", 2, (a, b, context, locale) -> a.subtract(b, locale)));
		register(new BinaryOperator("*", 3, (a, b, context, locale) -> a.multiply(b, locale)));
		register(new BinaryOperator("/", 3, (a, b, context, locale) -> a.divide(b, context, locale)));
		register(new BinaryOperator("%", 3, (a, b, context, locale) -> a.modulo(b, locale)));
		register(new BinaryOperator("^", 4, (a, b, context, locale) -> a.power(b, context, locale)));
		register(new BinaryOperator("nPr", 6, (a, b, context, locale) -> a.permutation(b, context, locale)));
		register(new BinaryOperator("nCr", 6, (a, b, context, locale) -> a.combination(b, context, locale)));
		//
		register(new Function("!", 5, 1));
		register(new OneArgumentRadicalFunction("sqrt", BigNumber::squareRoot));
		register(new OneArgumentRadicalFunction("√", BigNumber::squareRoot));
		register(new OneArgumentRadicalFunction("cbrt", BigNumber::cubicRoot));
		register(new OneArgumentRadicalFunction("³√", BigNumber::cubicRoot));
		register(new TwoArgumentRadicalFunction("rootn", BigNumber::nthRoot));
		//
		register(new TrigonometricFunction("sin", BigNumber::sin));
		register(new TrigonometricFunction("cos", BigNumber::cos));
		register(new TrigonometricFunction("tan", BigNumber::tan));
		register(new TrigonometricFunction("cot", BigNumber::cot));
		//
		register(new HyperbolicTrigonometricFunction("sinh", BigNumber::sinh));
		register(new HyperbolicTrigonometricFunction("cosh", BigNumber::cosh));
		register(new HyperbolicTrigonometricFunction("tanh", BigNumber::tanh));
		register(new HyperbolicTrigonometricFunction("coth", BigNumber::coth));
		//
		register(new InverseTrigonometricFunction("asin", BigNumber::asin));
		register(new InverseTrigonometricFunction("acos", BigNumber::acos));
		register(new InverseTrigonometricFunction("atan", BigNumber::atan));
		register(new InverseTrigonometricFunction("acot", BigNumber::acot));
		register(new InverseTrigonometricFunction("sin⁻¹", BigNumber::asin));
		register(new InverseTrigonometricFunction("cos⁻¹", BigNumber::acos));
		register(new InverseTrigonometricFunction("tan⁻¹", BigNumber::atan));
		register(new InverseTrigonometricFunction("cot⁻¹", BigNumber::acot));
		//
		register(new InverseHyperbolicTrigonometricFunction("asinh", BigNumber::asinh));
		register(new InverseHyperbolicTrigonometricFunction("acosh", BigNumber::acosh));
		register(new InverseHyperbolicTrigonometricFunction("atanh", BigNumber::atanh));
		register(new InverseHyperbolicTrigonometricFunction("acoth", BigNumber::acoth));
		register(new InverseHyperbolicTrigonometricFunction("sinh⁻¹", BigNumber::asinh));
		register(new InverseHyperbolicTrigonometricFunction("cosh⁻¹", BigNumber::acosh));
		register(new InverseHyperbolicTrigonometricFunction("tanh⁻¹", BigNumber::atanh));
		register(new InverseHyperbolicTrigonometricFunction("coth⁻¹", BigNumber::acoth));
		//
		register(new OneArgumentFunction("log2", BigNumber::log2));
		register(new OneArgumentFunction("log10", BigNumber::log10));
		register(new OneArgumentFunction("ln", BigNumber::ln));
		register(new TwoArgumentFunction("logbase", BigNumber::logBase));
		//
		register(new TwoArgumentFunction("atan2", BigNumber::atan2));
		register(new TwoArgumentFunction("perm", BigNumber::permutation));
		register(new TwoArgumentFunction("comb", BigNumber::combination));
		register(new CoordinateFunction("Pol", BigNumber::cartesianToPolarCoordinates));
		register(new AdvancedCoordinateFunction("Rec", BigNumber::polarToCartesianCoordinates));
		register(new SimpleTwoArgumentFunction("GCD", BigNumber::gcd));
		register(new TwoArgumentFunction("LCM", BigNumber::lcm));
		register(new SimpleTwoArgumentFunction("RandInt", BigNumber::randomIntegerForRange));

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

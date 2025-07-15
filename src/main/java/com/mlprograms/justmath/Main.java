package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.Tokenizer;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		// TODO: only for developing purpose

		CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.DEG);
		// System.out.println(calculator.evaluate("e*pi"));
		// System.out.println(calculator.evaluate("∑(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		//System.out.println(calculator.evaluate("sum(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		// System.out.println(calculator.evaluate("summ(0;4;2^k)"));

		// funktioniert hier nicht
		System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("∑(0;4;2^(k!-k))"));
		System.out.println(calculator.evaluate("∑(0;4;2^(k!-k))", Map.of("a", new BigNumber("0.5"))));

		// aber hier schon
		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("piea"));
		// System.out.println(calculator.evaluate("piea", Map.of("a", new BigNumber("0.5"))));
		// System.out.println(calculator.evaluate("pie*api", Map.of("api", new BigNumber("0.5"))));
		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("pie*a"));
		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("pieapi"));

		// testCalculator();


	}

	private static void testCalculator() {
		System.out.println("Welcome to   JustMath Calculator!");
		System.out.println("Type your mathematical expression below:");
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print(">> ");
			String input = scanner.nextLine().trim();
			if (input.equalsIgnoreCase("exit")) {
				System.out.println("Exiting JustMath Calculator. Goodbye!");
				break;
			}

			try {
				CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.DEG);
				BigNumber result = calculator.evaluate(input);
				System.out.println("Result: " + result);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.util.Locale;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		// TODO: only for developing purpose

		CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.DEG);
		// System.out.println(calculator.evaluate("e*pi"));
		System.out.println(calculator.evaluate("∑(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		System.out.println(calculator.evaluate("sum(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		// System.out.println(calculator.evaluate("summ(0;4;2^k)"));

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
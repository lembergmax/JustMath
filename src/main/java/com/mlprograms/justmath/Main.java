package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.bignumber.math.CoordinateConversionMath;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		testCalculator();

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
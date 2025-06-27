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

		BigNumber bigNumber1 = new BigNumber("12.3", Locale.US);
		BigNumber bigNumber2 = new BigNumber("45.7");


		BigNumberCoordinate coordinate = CoordinateConversionMath.polarToCartesianCoordinates(bigNumber1, bigNumber2, new MathContext(10, RoundingMode.HALF_UP), TrigonometricMode.DEG, Locale.US);
		System.out.println(coordinate.toString(Locale.US));

		// testStation();

	}

	private static void testStation() {
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
				CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.RAD);
				BigNumber result = calculator.evaluate(input);
				System.out.println("Result: " + result);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
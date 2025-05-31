package com.mlprograms.justmath;

import com.mlprograms.justmath.calculator.CalculatorEngine;

public class Main {

	public static void main(String[] args) {

		CalculatorEngine calculatorEngine = new CalculatorEngine(1);
		System.out.println(calculatorEngine.evaluate("((2.5 + sqrt(16) * (3 - 1)^2) / (4.2 - 1.1))^2 + (5 - 6 / 2 + 3^2 - (1.5 + 2.5 / 5)) * (7 + sqrt(49)) / 2^3 - sqrt(81) + 0.5^(2 + 1)"));

	}

}
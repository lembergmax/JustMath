/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		// TODO: only for developing purpose

		// System.out.println(calculator.evaluate("∑(1;4;k^2)"));
		// System.out.println(calculator.evaluate("∏(1;4;k^2)"));

		// System.out.println(calculator.evaluate("e*pi"));
		// System.out.println(calculator.evaluate("∑(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		// System.out.println(calculator.evaluate("sum(0;8;2^(k*k))-∑(0;4;2^(k*k))").formatToLocale(Locale.GERMAN).toStringWithGrouping());
		// System.out.println(calculator.evaluate("summ(0;4;2^k)"));

		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("∑(0;4;2^(k!-k))"));
		// System.out.println(calculator.evaluate("a^2-∑(0;4;2^(k!-k*a+ka))", Map.of("a", new BigNumber("0.5"), "ka", new BigNumber("2"))));

		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("a^2-∑(0;4;2^(k!-k*a+k^a))"));

		// BigNumber result = calculator.evaluate("sum(0;4;2^(a*k))", Map.of("a", new BigNumber("1.872<")));

		// 11.242640687119285146405066172629094235709015626130844219530039213972197435386321116551162602982924718
		// System.out.println(result);

		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("piea"));
		// System.out.println(calculator.evaluate("piea", Map.of("a", new BigNumber("0.5"))));
		// System.out.println(calculator.evaluate("pie*api", Map.of("api", new BigNumber("0.5"))));
		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("pie*a"));
		// System.out.println(new Tokenizer(new MathContext(10, RoundingMode.HALF_UP)).tokenize("pieapi"));

		testCalculator();
	}

	private static void testCalculator() {
		System.out.println("Welcome to JustMath Calculator!");
		System.out.println("Type your mathematical expression below:");
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print(">> ");
			String input = scanner.nextLine().trim();

			if (input.equalsIgnoreCase("exit")) {
				System.out.println("Exiting JustMath Calculator. Goodbye!");
				break;
			}

			CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.DEG);
			BigNumber result = calculator.evaluate(input);
			System.out.println("Result: " + result);
		}
	}

}

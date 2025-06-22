package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        testStation();

    }

    private static void testStation() {
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
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
import com.mlprograms.justmath.bignumber.BigNumberMatrix;
import com.mlprograms.justmath.calculator.CalculatorEngine;

import java.util.Locale;

public class Main {

    public static void main(String[] args) {

        final CalculatorEngine calculatorEngine = new CalculatorEngine();
        System.out.println(calculatorEngine.evaluate("asin(0.25)"));


        BigNumber bigNumber = new BigNumber("3242341242345", Locale.GERMAN);
        System.out.println(bigNumber.toPrettyString());

        BigNumberMatrix matrix1 = new BigNumberMatrix("1,2,3;4,5,6;7,8,9", Locale.GERMAN);
        BigNumberMatrix matrix2 = new BigNumberMatrix("9,8,7;6,5,4;3,2,1", Locale.GERMAN);

        System.out.println(matrix1.add(matrix2).toPlainDataString());


    }

}

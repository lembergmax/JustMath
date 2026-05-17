/*
 * Copyright (c) 2025-2026 Max Lemberg
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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.errors.ErrorMode;

import java.util.Locale;

public class Main {

    public static void main(final String[] args) {

//        UnitConverter unitConverter = new UnitConverter();
//
//        BigNumber convertedToBigNumber = unitConverter.convertToBigNumber("1", Unit.Area.ACRE, Unit.Area.SQUARE_KILOMETER);
//        UnitValue convertedToUnitValue = unitConverter.convert("1", Unit.Area.ACRE, Unit.Area.SQUARE_KILOMETER);
//
//        System.out.println(convertedToBigNumber);
//        System.out.println(convertedToUnitValue.getValue());

        final CalculatorEngine calculatorEngine = new CalculatorEngine();
        calculatorEngine.setLocale(Locale.GERMAN);
        calculatorEngine.setErrorMode(ErrorMode.USER_FRIENDLY);
        System.out.println(calculatorEngine.evaluateToString("5000ß!sqrt()"));
        System.out.println(calculatorEngine.evaluateToString("5000!sqrt()"));
        System.out.println(calculatorEngine.evaluateToString("5000!*"));


        System.out.println(calculatorEngine.getTokenizer().tokenize("5000!sqrt()"));
        System.out.println((calculatorEngine.getPostfixParser().toPostfix(calculatorEngine.getTokenizer().tokenize("5000!sqrt()"))));


//        System.out.println(calculatorEngine.evaluateToString("Pol(1;2)"));
//        System.out.println(calculatorEngine.evaluateToString("Pol(1;2)+3"));
//        System.out.println(calculatorEngine.evaluateToString("Pol(1;2)+Rec(2;1)"));
//        System.out.println(calculatorEngine.evaluateToString("Rec(2;1)"));

//        final UnitValue unitValue = new UnitValue("1y");
//        System.out.println(new UnitConverter().convertToBigNumber(unitValue, Unit.Time.SECOND));

//        System.out.println(
//                UnitElements.getRegistry().entrySet().stream().filter(unit -> unit.getValue().toString().equals("0"))
//        );


//        UnitConverter unitConverter = new UnitConverter();
//
//        System.out.println(unitConverter.convert("15", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND));
//
//
//        System.out.println(unitConverter.convert(
//                "8935410796587016507816508716578061357893541079658701650781650871657806135789354107965870165078165087165780613578935410796587016507816508716578061357",
//                Unit.Length.SUN_RADIUS,
//                Unit.Length.METER
//        ));
//
//        System.out.println(unitConverter.convert(
//                "893541079658701650781650871657806135789354107965870165078165087165780613578935410796587016507816508716578061357893541079658701650781650871657806135789354107965870165078165087165780613578935410796587016507816508716578061357",
//                Unit.Length.SUN_RADIUS,
//                Unit.Length.METER
//        ));

    }

}

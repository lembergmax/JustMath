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

import com.mlprograms.justmath.converter.Unit;
import com.mlprograms.justmath.converter.UnitConverter;
import com.mlprograms.justmath.converter.UnitValue;
import com.mlprograms.justmath.graphfx.planar.view.GraphFxViewer;

public class Main {

    public static void main(final String[] args) {

//        UnitConverter unitConverter = new UnitConverter();
//
//        BigNumber convertedToBigNumber = unitConverter.convertToBigNumber("1", Unit.Area.ACRE, Unit.Area.SQUARE_KILOMETER);
//        UnitValue convertedToUnitValue = unitConverter.convert("1", Unit.Area.ACRE, Unit.Area.SQUARE_KILOMETER);
//
//        System.out.println(convertedToBigNumber);
//        System.out.println(convertedToUnitValue.getValue());

        final UnitValue unitValue = new UnitValue("12L");
        System.out.println(new UnitConverter().convertToBigNumber(unitValue, Unit.Volume.DROP));

        GraphFxViewer viewer = new GraphFxViewer("Test");
        viewer.plotExpression("x^2");
        viewer.show();


    }

}

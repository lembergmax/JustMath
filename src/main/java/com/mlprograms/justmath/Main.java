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
import com.mlprograms.justmath.graphfx.planar.calculator.GraphFxCalculatorEngine;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.GraphFxViewer;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;

public final class Main {

    public static void main(final String[] args) {

//        final GraphFxCalculatorEngine graphFxCalculatorEngine = new GraphFxCalculatorEngine();
//        final ViewportSnapshot viewportSnapshot = new ViewportSnapshot(
//                new BigNumber("-5"),
//                new BigNumber("5"),
//                new BigNumber("-5"),
//                new BigNumber("5"),
//                new BigNumber("0.05")
//        );
//
//        PlotResult result = graphFxCalculatorEngine.evaluate("sin(3*x)*cos(4*y) + sin(5*x + 2*y) - 0.15", viewportSnapshot);
//        System.out.println("lines = " + result.plotLines().size());
//
//        result = graphFxCalculatorEngine.evaluate("sin(x^2 + y^2) + 0.35*sin(3*x) - 0.35*cos(3*y)", viewportSnapshot);
//        System.out.println("lines = " + result.plotLines().size());
//
//        result = graphFxCalculatorEngine.evaluate("sin(6*sqrt(x^2+y^2)) + 0.4*sin(4*x)*sin(4*y) - 0.2", viewportSnapshot);
//        System.out.println("lines = " + result.plotLines().size());
//
//        result = graphFxCalculatorEngine.evaluate("(x^2 - y^2)^2 - (x^2 + y^2) + 0.2*sin(8*x)*cos(8*y)", viewportSnapshot);
//        System.out.println("lines = " + result.plotLines().size());
//
//        result = graphFxCalculatorEngine.evaluate("sin(5*x) + sin(5*y) + 0.5*sin(3*x + 4*y) + 0.25*sin(7*x - 2*y) - 0.1", viewportSnapshot);
//        System.out.println("lines = " + result.plotLines().size());

        final GraphFxViewer graphFxViewer = new GraphFxViewer();
        graphFxViewer.show();
        graphFxViewer.plot("sin(x)-y");

    }

}

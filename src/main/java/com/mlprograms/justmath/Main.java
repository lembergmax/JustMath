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
import com.mlprograms.justmath.graphfx.api.DisplayTheme;
import com.mlprograms.justmath.graphfx.api.GraphFxPlotViewer;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(final String[] args) {
        GraphFxPlotViewer viewer = new GraphFxPlotViewer(DisplayTheme.DARK);
        viewer.plotExpression("sin(10x)/x+0.2x^3-2x", "#ff5500");
        viewer.plotExpression("sin(10x)", "#0055ff");
        viewer.plotExpression("sin(10x)/x", "#ff0055");
        viewer.plotExpression("(10x)/x", "#5500ff");
        viewer.show();

        GraphFxPlotViewer viewer2 = new GraphFxPlotViewer(DisplayTheme.DARK);

        Map<String, BigNumber> variables = new HashMap<>();
        variables.put("a", new BigNumber("2.5"));
        variables.put("b", new BigNumber("1.2"));

        viewer2.plotExpression("a*sin(x) + b", variables, "#00B7FF");
        viewer2.show();

        GraphFxPlotViewer viewer3 = new GraphFxPlotViewer(DisplayTheme.DARK);
        viewer3.plotExpression("1/x", "#5500ff");
        viewer3.show();


    }

}

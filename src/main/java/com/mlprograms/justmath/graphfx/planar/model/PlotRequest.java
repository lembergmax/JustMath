/*
 * Copyright (c) 2026 Max Lemberg
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

package com.mlprograms.justmath.graphfx.planar.model;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PlotRequest {

    @NonNull
    private String expression;

    @NonNull
    private Map<String, String> variables;

    @NonNull
    private BigNumber minX;

    @NonNull
    private BigNumber minY;

    @NonNull
    private BigNumber maxX;

    @NonNull
    private BigNumber maxY;

    @NonNull
    private BigNumber cellSize;

    public PlotRequest(@NonNull final String expression, final Map<String, String> variables, @NonNull final BigNumber minX, @NonNull final BigNumber minY, @NonNull final BigNumber maxX, @NonNull final BigNumber maxY, @NonNull final BigNumber cellSize) {
        this.expression = expression;
        this.variables = variables == null ? new HashMap<>() : variables;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.cellSize = cellSize;
    }

}

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

package com.mlprograms.justmath.graph.fx.model;

import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.util.UUID;

public record GraphFxPointObject(
        UUID id,
        String name,
        boolean visible,
        GraphFxStyle style,
        BigDecimal x,
        BigDecimal y,
        UUID functionId
) implements GraphFxObject {

    public static GraphFxPointObject of(final String name, final BigDecimal x, final BigDecimal y, final UUID functionId) {
        return new GraphFxPointObject(
                UUID.randomUUID(),
                name,
                true,
                new GraphFxStyle(Color.BLACK, 2.0, 1.0),
                x, y,
                functionId
        );
    }

    @Override
    public UUID referencesFunctionId() {
        return functionId;
    }
}

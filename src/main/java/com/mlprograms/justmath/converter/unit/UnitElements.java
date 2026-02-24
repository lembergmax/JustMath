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

package com.mlprograms.justmath.converter.unit;

import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitElements {

    @Getter
    private static final Map<String, ExpressionElement> registry = new HashMap<>();

    static {
        List<Unit> units = List.of(
        );

        for (Unit unit : units) {
            register(unit);
        }
    }


    /**
     * Registers an {@link Unit} in the registry.
     * The unit is mapped by its symbol for a later lookup.
     *
     * @param unit the {@code ExpressionElement} to register
     */
    public static void register(Unit unit) {
        registry.put(unit.getSymbol(), unit);
    }

}

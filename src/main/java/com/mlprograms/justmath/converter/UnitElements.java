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

package com.mlprograms.justmath.converter;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Central unit registry for normalized token lookup.
 */
@UtilityClass
public class UnitElements {

    @Getter
    private static final Map<String, UnitDefinition> registry = new HashMap<>();

    static {
        List<List<UnitDefinition>> unitDefinitionList = List.of(
                Unit.lengthDefinitions()
        );

        for (List<UnitDefinition> unitDefinitions : unitDefinitionList) {
            for (UnitDefinition unitDefinition : unitDefinitions) {
                register(unitDefinition);
            }
        }
    }

    private static void register(final UnitDefinition unitDefinition) {
        for (String token : unitDefinition.tokens()) {
            put(token, unitDefinition);
        }
    }

    private static void put(final String token, final UnitDefinition unitDefinition) {
        String normalized = token.toLowerCase(Locale.ROOT);
        registry.put(normalized, unitDefinition);
    }

}

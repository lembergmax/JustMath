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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for token-to-unit mapping (data storage only).
 */
public final class UnitElements {

    public static final Map<String, Unit.UnitType> registry = new HashMap<>();

    @Getter
    private static int maxTokenLength = -1;

    static {
        for (Unit.Type.Length lengthUnit : Unit.Type.Length.values()) {
            register(lengthUnit);
        }
    }

    private UnitElements() {
    }

    public static Optional<Unit.UnitType> find(final String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(registry.get(token.toLowerCase(Locale.ROOT)));
    }

    private static void register(final Unit.UnitType unitType) {
        put(unitType.symbol(), unitType);
        put(unitType.toString(), unitType);

        for (String alias : unitType.aliases()) {
            put(alias, unitType);
        }
    }

    private static void put(final String token, final Unit.UnitType unitType) {
        String normalized = token.toLowerCase(Locale.ROOT);
        registry.put(normalized, unitType);
        maxTokenLength = Math.max(maxTokenLength, normalized.length());
    }

}

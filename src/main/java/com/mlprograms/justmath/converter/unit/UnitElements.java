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

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@UtilityClass
public class UnitElements {

    @Getter
    private static final Map<String, Unit> registry = new ConcurrentHashMap<>();

    static {
        UnitDefinitions.defaults().forEach(UnitElements::register);
    }

    public static Optional<Unit> findBySymbol(@NonNull final String symbol) {
        return Optional.ofNullable(registry.get(symbol));
    }

    public static Unit requireBySymbol(@NonNull final String symbol) {
        return findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit symbol: " + symbol));
    }

    public static List<Unit> unitsByCategory(@NonNull final UnitCategory category) {
        return registry.values().stream()
                .filter(unit -> unit.getCategory() == category)
                .toList();
    }

    public static Map<UnitCategory, List<Unit>> allByCategory() {
        final Map<UnitCategory, List<Unit>> grouped = registry.values().stream()
                .collect(Collectors.groupingBy(Unit::getCategory, () -> new EnumMap<>(UnitCategory.class), Collectors.toList()));

        return Map.copyOf(grouped);
    }

    public static void register(@NonNull final Unit unit) {
        final Unit existing = registry.putIfAbsent(unit.getSymbol(), unit);

        if (existing != null && !existing.equals(unit)) {
            throw new IllegalArgumentException("Unit symbol already registered: " + unit.getSymbol());
        }
    }

}

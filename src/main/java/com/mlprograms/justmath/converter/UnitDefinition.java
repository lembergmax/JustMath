/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 */

package com.mlprograms.justmath.converter;

import java.util.List;

/**
 * @deprecated Replaced by enum-based unit definitions in {@link Unit.Type.Length}.
 */
@Deprecated(since = "1.0.5")
public record UnitDefinition(
        String key,
        String displaySymbol,
        List<String> aliases,
        UnitCategory category,
        String factorToCategoryBase
) {
}

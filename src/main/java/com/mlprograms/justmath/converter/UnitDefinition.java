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

import java.util.List;

/**
 * Immutable storage object for one unit and its conversion metadata.
 *
 * @param key                    unique internal id (e.g. "KILOMETER")
 * @param displaySymbol          canonical user-facing symbol (e.g. "km")
 * @param aliases                accepted aliases (e.g. "kilometer", "kilometre")
 * @param category               conversion category this unit belongs to
 * @param factorToCategoryBase   multiplicative factor to the category base unit, as decimal string
 * @param source                 reference used for this factor
 */
public record UnitDefinition(
        String key,
        String displaySymbol,
        List<String> aliases,
        UnitCategory category,
        String factorToCategoryBase,
        String source
) {
}

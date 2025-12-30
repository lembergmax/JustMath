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

package com.mlprograms.justmath.graphfx.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WindowConfigTest {

    @Test
    void scaleConstraintsMakeSense() {
        assertTrue(WindowConfig.MIN_SCALE_PX_PER_UNIT > 0);
        assertTrue(WindowConfig.MAX_SCALE_PX_PER_UNIT > WindowConfig.MIN_SCALE_PX_PER_UNIT);
        assertTrue(WindowConfig.INITIAL_SCALE_PX_PER_UNIT >= WindowConfig.MIN_SCALE_PX_PER_UNIT);
        assertTrue(WindowConfig.INITIAL_SCALE_PX_PER_UNIT <= WindowConfig.MAX_SCALE_PX_PER_UNIT);
    }

    @Test
    void palettesAreNotNull() {
        assertNotNull(WindowConfig.LIGHT_THEME);
        assertNotNull(WindowConfig.DARK_THEME);

        assertNotNull(WindowConfig.LIGHT_THEME.background());
        assertNotNull(WindowConfig.LIGHT_THEME.axis());
        assertNotNull(WindowConfig.DARK_THEME.background());
        assertNotNull(WindowConfig.DARK_THEME.axis());
    }

}

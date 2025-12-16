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

package com.mlprograms.justmath.graphfx.model;

import javafx.scene.paint.Color;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable graph object representing an infinite straight line.
 * <p>
 * The line is defined in point-slope form:
 * </p>
 * <pre>
 * y = slope * (x - x0) + y0
 * </pre>
 * <p>
 * where {@code (x0, y0)} is a point on the line and {@code slope} is the line's slope.
 * </p>
 *
 * <p>
 * {@link #style()} controls rendering (color, stroke width, alpha), while {@link #visible()} toggles whether
 * the view should draw the line.
 * </p>
 *
 * <p>
 * This object is currently independent of functions; therefore {@link #referencesFunctionId()} returns {@code null}.
 * If you want lines to be removed automatically when a function is removed, extend this record with a function id.
 * </p>
 *
 * @param id      unique identifier of the object
 * @param name    short label (e.g., {@code "t"} for tangent or {@code "n"} for normal)
 * @param visible whether the line should be rendered
 * @param style   rendering style (color, stroke width, opacity)
 * @param x0      x-coordinate of a point on the line
 * @param y0      y-coordinate of a point on the line
 * @param slope   slope of the line
 */
public record GraphFxLineObject(@NonNull UUID id, @NonNull String name, boolean visible, @NonNull GraphFxStyle style,
                                @NonNull BigDecimal x0, @NonNull BigDecimal y0,
                                @NonNull BigDecimal slope) implements GraphFxObject {

    /**
     * Creates a visible line object with a default visual style.
     * <p>
     * The returned line:
     * </p>
     * <ul>
     *     <li>gets a random {@link UUID} as identifier</li>
     *     <li>is marked as {@code visible}</li>
     *     <li>uses a default dark gray {@link GraphFxStyle} with stroke width {@code 2.0} and alpha {@code 1.0}</li>
     * </ul>
     *
     * @param name  the label of the line (must not be {@code null})
     * @param x0    x-coordinate of a point on the line (must not be {@code null})
     * @param y0    y-coordinate of a point on the line (must not be {@code null})
     * @param slope slope of the line (must not be {@code null})
     * @return a new {@link GraphFxLineObject} instance
     */
    public static GraphFxLineObject of(@NonNull final String name, @NonNull final BigDecimal x0, @NonNull final BigDecimal y0, @NonNull final BigDecimal slope) {
        return new GraphFxLineObject(UUID.randomUUID(), name, true, new GraphFxStyle(Color.rgb(40, 40, 40), 2.0, 1.0), x0, y0, slope);
    }

    /**
     * Returns the identifier of a referenced function, if the object depends on one.
     * <p>
     * A line created by the current tooling is treated as independent; therefore this method returns {@code null}.
     * </p>
     *
     * @return {@code null}, indicating that this line is not associated with a specific function
     */
    @Override
    public UUID referencesFunctionId() {
        return null;
    }

}

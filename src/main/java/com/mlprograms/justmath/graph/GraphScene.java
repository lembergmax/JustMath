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

package com.mlprograms.justmath.graph;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Model holding functions, variables, objects and view settings for graph rendering.
 */
@Getter
public class GraphScene {

    private final List<FunctionEntry> functions = new ArrayList<>();
    private final List<SceneObject> objects = new ArrayList<>();
    private final Map<String, VariableEntry> variables = new LinkedHashMap<>();
    private final Settings settings = new Settings();
    private final AtomicLong revision = new AtomicLong(0);
    private final List<SceneListener> listeners = new CopyOnWriteArrayList<>();

    public GraphScene() {
        variables.put("a", VariableEntry.withValue("a", new BigDecimal("1")));
        variables.put("b", VariableEntry.withValue("b", new BigDecimal("1")));
        variables.put("c", VariableEntry.withValue("c", new BigDecimal("0")));
    }

    /**
     * Adds a new function to the scene.
     *
     * @param name       function name (for display)
     * @param expression expression string (e.g. "sin(x)+x^2")
     * @return created function entry
     */
    @NonNull
    public synchronized FunctionEntry addFunction(@NonNull final String name, @NonNull final String expression) {
        final FunctionEntry entry = FunctionEntry.builder()
                .id(UUID.randomUUID())
                .name(name)
                .expression(expression)
                .visible(true)
                .style(Style.defaultFunctionStyle())
                .build();
        functions.add(entry);
        bump(SceneChangeType.DATA);
        return entry;
    }

    /**
     * Removes a function by id.
     *
     * @param functionId id of function
     */
    public synchronized void removeFunction(@NonNull final UUID functionId) {
        functions.removeIf(f -> f.getId().equals(functionId));
        objects.removeIf(o -> o.referencesFunction(functionId));
        bump(SceneChangeType.DATA);
    }

    /**
     * Updates a function entry.
     *
     * @param updated updated instance (same id)
     */
    public synchronized void updateFunction(@NonNull final FunctionEntry updated) {
        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).getId().equals(updated.getId())) {
                functions.set(i, updated);
                bump(SceneChangeType.DATA);
                return;
            }
        }
    }

    /**
     * Adds an object to the scene.
     *
     * @param object scene object
     */
    public synchronized void addObject(@NonNull final SceneObject object) {
        objects.add(object);
        bump(SceneChangeType.DATA);
    }

    /**
     * Removes an object by id.
     *
     * @param objectId object id
     */
    public synchronized void removeObject(@NonNull final UUID objectId) {
        objects.removeIf(o -> o.getId().equals(objectId));
        bump(SceneChangeType.DATA);
    }

    /**
     * Updates a variable value.
     *
     * @param name  variable name
     * @param value value
     */
    public synchronized void setVariableValue(@NonNull final String name, @NonNull final BigDecimal value) {
        final VariableEntry existing = variables.get(name);
        if (existing == null) {
            variables.put(name, VariableEntry.withValue(name, value));
        } else {
            variables.put(name, existing.toBuilder().value(value).build());
        }
        bump(SceneChangeType.VARIABLES);
    }

    /**
     * Updates a variable entry.
     *
     * @param entry variable entry
     */
    public synchronized void updateVariable(@NonNull final VariableEntry entry) {
        variables.put(entry.getName(), entry);
        bump(SceneChangeType.VARIABLES);
    }

    /**
     * Adds a listener that is notified on scene changes.
     *
     * @param listener listener
     */
    public void addListener(@NonNull final SceneListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener listener
     */
    public void removeListener(@NonNull final SceneListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns a monotonically increasing revision counter, incremented on any change.
     *
     * @return revision
     */
    public long getRevision() {
        return revision.get();
    }

    private void bump(@NonNull final SceneChangeType changeType) {
        final long rev = revision.incrementAndGet();
        final SceneChange change = new SceneChange(changeType, rev);
        for (final SceneListener l : listeners) {
            l.onSceneChanged(change);
        }
    }

    /**
     * Scene listener for change notifications.
     */
    public interface SceneListener {
        void onSceneChanged(@NonNull SceneChange change);
    }

    /**
     * Scene change payload.
     */
    @Value
    public static class SceneChange {
        @NonNull
        SceneChangeType type;
        long revision;
    }

    /**
     * Change types.
     */
    public enum SceneChangeType {
        DATA,
        VARIABLES
    }

    /**
     * Rendering and interaction settings.
     */
    @Getter
    @Setter
    public static class Settings {
        private boolean showGrid = true;
        private boolean showAxes = true;
        private boolean snapToGrid = false;
        private int targetGridLines = 10;
    }

    /**
     * Drawable objects besides functions.
     */
    public interface SceneObject {

        @NonNull
        UUID getId();

        @NonNull
        String getName();

        @NonNull
        Style getStyle();

        boolean isVisible();

        boolean referencesFunction(@NonNull UUID functionId);
    }

    /**
     * Style container.
     */
    @Value
    @Builder(toBuilder = true)
    public static class Style {
        @NonNull
        Color color;
        float strokeWidth;
        boolean dashed;
        int alpha;

        public static Style defaultFunctionStyle() {
            return Style.builder()
                    .color(new Color(0, 102, 204))
                    .strokeWidth(2f)
                    .dashed(false)
                    .alpha(255)
                    .build();
        }

        public static Style defaultObjectStyle() {
            return Style.builder()
                    .color(new Color(30, 30, 30))
                    .strokeWidth(2f)
                    .dashed(false)
                    .alpha(255)
                    .build();
        }

        public Color toAwtColor() {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
        }
    }

    /**
     * Function definition.
     */
    @Value
    @Builder(toBuilder = true)
    public static class FunctionEntry {
        @NonNull
        UUID id;
        @NonNull
        String name;
        @NonNull
        String expression;
        boolean visible;
        @NonNull
        Style style;
    }

    /**
     * Variable definition with optional slider config.
     */
    @Value
    @Builder(toBuilder = true)
    public static class VariableEntry {
        @NonNull
        String name;
        @NonNull
        BigDecimal value;
        boolean sliderEnabled;
        @NonNull
        BigDecimal sliderMin;
        @NonNull
        BigDecimal sliderMax;
        @NonNull
        BigDecimal sliderStep;

        public static VariableEntry withValue(@NonNull final String name, @NonNull final BigDecimal value) {
            return VariableEntry.builder()
                    .name(name)
                    .value(value)
                    .sliderEnabled(false)
                    .sliderMin(new BigDecimal("-10"))
                    .sliderMax(new BigDecimal("10"))
                    .sliderStep(new BigDecimal("0.1"))
                    .build();
        }
    }

    /**
     * Point object (free or bound to function).
     */
    @Value
    @Builder(toBuilder = true)
    public static class PointObject implements SceneObject {
        @NonNull
        UUID id;
        @NonNull
        String name;
        boolean visible;
        @NonNull
        Style style;

        @NonNull
        BigDecimal x;
        @NonNull
        BigDecimal y;

        UUID functionId;
        boolean traceEnabled;
        @NonNull
        List<com.mlprograms.justmath.graph.GraphPoint> trace;

        public static PointObject free(@NonNull final String name, @NonNull final BigDecimal x, @NonNull final BigDecimal y) {
            return PointObject.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .visible(true)
                    .style(Style.defaultObjectStyle())
                    .x(x)
                    .y(y)
                    .functionId(null)
                    .traceEnabled(false)
                    .trace(new ArrayList<>())
                    .build();
        }

        @Override
        public boolean referencesFunction(@NonNull final UUID functionId) {
            return this.functionId != null && this.functionId.equals(functionId);
        }
    }

    /**
     * Line object in point-slope form.
     */
    @Value
    @Builder(toBuilder = true)
    public static class LineObject implements SceneObject {
        @NonNull
        UUID id;
        @NonNull
        String name;
        boolean visible;
        @NonNull
        Style style;

        @NonNull
        BigDecimal x0;
        @NonNull
        BigDecimal y0;
        @NonNull
        BigDecimal slope;

        public static LineObject of(@NonNull final String name, @NonNull final BigDecimal x0, @NonNull final BigDecimal y0, @NonNull final BigDecimal slope) {
            return LineObject.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .visible(true)
                    .style(Style.defaultObjectStyle())
                    .x0(x0)
                    .y0(y0)
                    .slope(slope)
                    .build();
        }

        @Override
        public boolean referencesFunction(@NonNull final UUID functionId) {
            return false;
        }
    }

    /**
     * Integral area object for a function on [a,b].
     */
    @Value
    @Builder(toBuilder = true)
    public static class IntegralObject implements SceneObject {
        @NonNull
        UUID id;
        @NonNull
        String name;
        boolean visible;
        @NonNull
        Style style;

        @NonNull
        UUID functionId;
        @NonNull
        BigDecimal a;
        @NonNull
        BigDecimal b;
        @NonNull
        BigDecimal value;

        public static IntegralObject of(@NonNull final String name, @NonNull final UUID functionId, @NonNull final BigDecimal a, @NonNull final BigDecimal b, @NonNull final BigDecimal value) {
            return IntegralObject.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .visible(true)
                    .style(Style.builder()
                            .color(new Color(0, 102, 204))
                            .strokeWidth(1.5f)
                            .dashed(false)
                            .alpha(60)
                            .build())
                    .functionId(functionId)
                    .a(a)
                    .b(b)
                    .value(value)
                    .build();
        }

        @Override
        public boolean referencesFunction(@NonNull final UUID functionId) {
            return this.functionId.equals(functionId);
        }
    }

}

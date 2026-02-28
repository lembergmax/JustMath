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

package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.sampling.SamplingStrategies;
import com.mlprograms.justmath.graphing.engine.sampling.SamplingStrategy;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable plot request configuration.
 * <p>
 * A request contains:
 * <ul>
 *     <li>the expression string</li>
 *     <li>the domain for x-values</li>
 *     <li>the sampling resolution</li>
 *     <li>the sampling strategy</li>
 *     <li>optional constant variables (excluding reserved x)</li>
 * </ul>
 * </p>
 * <p>
 * Instances are created via {@link Builder}.
 * </p>
 */
@ToString
@EqualsAndHashCode
public final class PlotRequest {

    /**
     * Reserved variable name used by the plot engine to represent the sampled x-value.
     */
    public static final String RESERVED_X_VARIABLE = "x";

    /**
     * The expression to be plotted.
     */
    private final String expression;

    /**
     * The inclusive sampling domain for x-values.
     */
    private final Domain domain;

    /**
     * The requested sampling resolution.
     */
    private final Resolution resolution;

    /**
     * The sampling strategy used to generate sample points.
     */
    private final SamplingStrategy samplingStrategy;

    /**
     * Constant variables used during evaluation (must not contain {@link #RESERVED_X_VARIABLE}).
     * <p>
     * The map is stored as an unmodifiable defensive copy.
     * </p>
     */
    private final Map<String, Double> variables;

    /**
     * Creates an immutable request from a builder.
     *
     * @param builder builder containing validated configuration
     */
    private PlotRequest(final Builder builder) {
        this.expression = builder.expression;
        this.domain = builder.domain;
        this.resolution = builder.resolution;
        this.samplingStrategy = builder.samplingStrategy;
        this.variables = Collections.unmodifiableMap(new HashMap<>(builder.variables));
    }

    /**
     * Convenience factory for creating a new {@link Builder}.
     *
     * @param expression expression string (must not be {@code null} or blank)
     * @return builder instance
     */
    public static Builder builder(@NonNull final String expression) {
        return new Builder(expression);
    }

    /**
     * Returns the expression string.
     *
     * @return expression (never blank)
     */
    public String expression() {
        return expression;
    }

    /**
     * Returns the x-domain.
     *
     * @return domain (never {@code null})
     */
    public Domain domain() {
        return domain;
    }

    /**
     * Returns the sampling resolution.
     *
     * @return resolution (never {@code null})
     */
    public Resolution resolution() {
        return resolution;
    }

    /**
     * Returns the sampling strategy.
     *
     * @return sampling strategy (never {@code null})
     */
    public SamplingStrategy samplingStrategy() {
        return samplingStrategy;
    }

    /**
     * Returns constant variables used during evaluation.
     *
     * @return unmodifiable variable map (never {@code null})
     */
    public Map<String, Double> variables() {
        return variables;
    }

    /**
     * Builder for {@link PlotRequest}.
     * <p>
     * Defaults:
     * <ul>
     *     <li>domain: {@code [-10, 10]}</li>
     *     <li>resolution: {@code 500} samples</li>
     *     <li>strategy: uniform</li>
     *     <li>variables: empty</li>
     * </ul>
     * </p>
     */
    public static final class Builder {

        /**
         * Required expression string.
         */
        private final String expression;

        /**
         * Domain (optional).
         */
        private Domain domain = new Domain(-10.0d, 10.0d);

        /**
         * Resolution (optional).
         */
        private Resolution resolution = Resolution.fixed(500);

        /**
         * Sampling strategy (optional).
         */
        private SamplingStrategy samplingStrategy = SamplingStrategies.uniform();

        /**
         * Variables (optional).
         */
        private Map<String, Double> variables = Map.of();

        /**
         * Creates a builder for a required expression string.
         *
         * @param expression expression string (must not be {@code null} or blank)
         * @throws IllegalArgumentException if expression is blank
         */
        public Builder(@NonNull final String expression) {
            if (expression.isBlank()) {
                throw new IllegalArgumentException("expression must not be blank");
            }
            this.expression = expression;
        }

        /**
         * Sets the domain.
         *
         * @param domain domain (must not be {@code null})
         * @return this builder
         */
        public Builder domain(@NonNull final Domain domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Sets the resolution.
         *
         * @param resolution resolution (must not be {@code null})
         * @return this builder
         */
        public Builder resolution(@NonNull final Resolution resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Sets the sampling strategy.
         *
         * @param samplingStrategy sampling strategy (must not be {@code null})
         * @return this builder
         */
        public Builder samplingStrategy(@NonNull final SamplingStrategy samplingStrategy) {
            this.samplingStrategy = samplingStrategy;
            return this;
        }

        /**
         * Sets constant variables for evaluation.
         * <p>
         * Variable names must not be null/blank and must not include {@code "x"}.
         * </p>
         *
         * @param variables variables map (must not be {@code null})
         * @return this builder
         */
        public Builder variables(@NonNull final Map<String, Double> variables) {
            this.variables = variables;
            return this;
        }

        /**
         * Builds a validated {@link PlotRequest}.
         *
         * @return immutable plot request
         * @throws IllegalStateException    if any required component is null
         * @throws IllegalArgumentException if variable constraints are violated
         */
        public PlotRequest build() {
            if (domain == null || resolution == null || samplingStrategy == null || variables == null) {
                throw new IllegalStateException("domain, resolution, samplingStrategy and variables must not be null");
            }
            validateVariables(variables);
            return new PlotRequest(this);
        }

        /**
         * Validates variables for illegal names/values.
         *
         * @param variables variables to validate
         */
        private void validateVariables(final Map<String, Double> variables) {
            for (final Map.Entry<String, Double> entry : variables.entrySet()) {
                final String key = entry.getKey();

                if (key == null || key.isBlank()) {
                    throw new IllegalArgumentException("variable names must not be null/blank");
                }
                if (RESERVED_X_VARIABLE.equals(key)) {
                    throw new IllegalArgumentException("variables must not contain reserved name '" + RESERVED_X_VARIABLE + "'");
                }
                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("variable value for '" + key + "' must not be null");
                }
            }
        }
    }
}

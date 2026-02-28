package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.SamplingStrategy;
import com.mlprograms.justmath.graphing.engine.UniformSampler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PlotRequest {

    private final String expression;
    private final Domain domain;
    private final Resolution resolution;
    private final SamplingStrategy samplingStrategy;
    private final Map<String, Double> variables;

    private PlotRequest(final Builder builder) {
        this.expression = builder.expression;
        this.domain = builder.domain;
        this.resolution = builder.resolution;
        this.samplingStrategy = builder.samplingStrategy;
        this.variables = Map.copyOf(builder.variables);
    }

    public String expression() {
        return expression;
    }

    public Domain domain() {
        return domain;
    }

    public Resolution resolution() {
        return resolution;
    }

    public SamplingStrategy samplingStrategy() {
        return samplingStrategy;
    }

    public Map<String, Double> variables() {
        return variables;
    }

    public static final class Builder {
        private final String expression;
        private Domain domain = new Domain(-10.0, 10.0);
        private Resolution resolution = Resolution.fixed(1000);
        private SamplingStrategy samplingStrategy = new UniformSampler();
        private Map<String, Double> variables = Map.of();

        public Builder(final String expression) {
            this.expression = Objects.requireNonNull(expression, "expression must not be null");
            if (expression.isBlank()) {
                throw new IllegalArgumentException("expression must not be blank");
            }
        }

        public Builder domain(final Domain domain) {
            this.domain = Objects.requireNonNull(domain, "domain must not be null");
            return this;
        }

        public Builder resolution(final Resolution resolution) {
            this.resolution = Objects.requireNonNull(resolution, "resolution must not be null");
            return this;
        }

        public Builder samplingStrategy(final SamplingStrategy samplingStrategy) {
            this.samplingStrategy = Objects.requireNonNull(samplingStrategy, "samplingStrategy must not be null");
            return this;
        }

        public Builder variables(final Map<String, Double> variables) {
            Objects.requireNonNull(variables, "variables must not be null");
            this.variables = new HashMap<>(variables);
            return this;
        }

        public PlotRequest build() {
            return new PlotRequest(this);
        }
    }
}

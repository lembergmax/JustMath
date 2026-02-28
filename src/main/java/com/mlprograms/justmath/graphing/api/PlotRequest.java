package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.sampling.SamplingStrategies;
import com.mlprograms.justmath.graphing.engine.sampling.SamplingStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Plot request configuration object.
 */
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
        this.variables = Collections.unmodifiableMap(new HashMap<>(builder.variables));
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
        private Domain domain = new Domain(-10.0d, 10.0d);
        private Resolution resolution = Resolution.fixed(500);
        private SamplingStrategy samplingStrategy = SamplingStrategies.uniform();
        private Map<String, Double> variables = Map.of();

        public Builder(final String expression) {
            if (expression == null || expression.isBlank()) {
                throw new IllegalArgumentException("expression must not be blank");
            }
            this.expression = expression;
        }

        public Builder domain(final Domain domain) {
            this.domain = domain;
            return this;
        }

        public Builder resolution(final Resolution resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder samplingStrategy(final SamplingStrategy samplingStrategy) {
            this.samplingStrategy = samplingStrategy;
            return this;
        }

        public Builder variables(final Map<String, Double> variables) {
            this.variables = variables;
            return this;
        }

        public PlotRequest build() {
            if (domain == null || resolution == null || samplingStrategy == null || variables == null) {
                throw new IllegalStateException("domain, resolution, samplingStrategy and variables must not be null");
            }
            return new PlotRequest(this);
        }
    }
}

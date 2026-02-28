package com.mlprograms.justmath.graphing.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Config object for one plot operation.
 */
public final class PlotRequest {

    private final String expression;
    private final Domain domain;
    private final Resolution resolution;
    private final SamplingMode samplingMode;
    private final Map<String, String> variables;
    private final boolean parallelEnabled;

    private PlotRequest(Builder builder) {
        this.expression = builder.expression;
        this.domain = builder.domain;
        this.resolution = builder.resolution;
        this.samplingMode = builder.samplingMode;
        this.variables = Collections.unmodifiableMap(new HashMap<>(builder.variables));
        this.parallelEnabled = builder.parallelEnabled;
    }

    public String expression() { return expression; }
    public Domain domain() { return domain; }
    public Resolution resolution() { return resolution; }
    public SamplingMode samplingMode() { return samplingMode; }
    public Map<String, String> variables() { return variables; }
    public boolean parallelEnabled() { return parallelEnabled; }

    public static final class Builder {
        private final String expression;
        private Domain domain = new Domain(-10, 10);
        private Resolution resolution = Resolution.fixed(1000);
        private SamplingMode samplingMode = SamplingMode.UNIFORM;
        private Map<String, String> variables = Map.of();
        private boolean parallelEnabled;

        public Builder(String expression) {
            if (expression == null || expression.isBlank()) {
                throw new IllegalArgumentException("expression must not be blank");
            }
            this.expression = expression;
        }

        public Builder domain(Domain domain) { this.domain = domain; return this; }
        public Builder resolution(Resolution resolution) { this.resolution = resolution; return this; }
        public Builder samplingMode(SamplingMode samplingMode) { this.samplingMode = samplingMode; return this; }
        public Builder variables(Map<String, String> variables) { this.variables = variables == null ? Map.of() : variables; return this; }
        public Builder parallelEnabled(boolean parallelEnabled) { this.parallelEnabled = parallelEnabled; return this; }

        public PlotRequest build() {
            if (domain == null || resolution == null || samplingMode == null) {
                throw new IllegalArgumentException("domain, resolution and samplingMode are required");
            }
            return new PlotRequest(this);
        }
    }
}

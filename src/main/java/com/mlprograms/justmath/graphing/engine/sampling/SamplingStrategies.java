package com.mlprograms.justmath.graphing.engine.sampling;

public final class SamplingStrategies {

    private static final SamplingStrategy UNIFORM = new UniformSampler();

    private SamplingStrategies() {
    }

    public static SamplingStrategy uniform() {
        return UNIFORM;
    }
}

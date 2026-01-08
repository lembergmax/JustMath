package com.mlprograms.justmath.graphfx;

import java.util.Objects;

public record WindowConfig(String title, int width, int height, boolean exitApplicationOnLastViewerClose) {

    public static final int DEFAULT_WIDTH = 1200;
    public static final int DEFAULT_HEIGHT = 800;

    public WindowConfig {
        Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
    }

    public static WindowConfig defaultConfig() {
        return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }
    
}

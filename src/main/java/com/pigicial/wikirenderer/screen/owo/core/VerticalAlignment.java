package com.pigicial.wikirenderer.screen.owo.core;

public enum VerticalAlignment {
    TOP, CENTER, BOTTOM;

    public int align(int componentWidth, int span) {
        return switch (this) {
            case TOP -> 0;
            case CENTER -> span / 2 - componentWidth / 2;
            case BOTTOM -> span - componentWidth;
        };
    }
}

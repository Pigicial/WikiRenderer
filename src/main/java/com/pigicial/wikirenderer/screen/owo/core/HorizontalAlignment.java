package com.pigicial.wikirenderer.screen.owo.core;

public enum HorizontalAlignment {
    LEFT, CENTER, RIGHT;

    public int align(int componentWidth, int span) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> span / 2 - componentWidth / 2;
            case RIGHT -> span - componentWidth;
        };
    }
}

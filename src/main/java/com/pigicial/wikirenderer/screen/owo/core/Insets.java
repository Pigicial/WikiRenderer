package com.pigicial.wikirenderer.screen.owo.core;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

public record Insets(int top, int bottom, int left, int right) implements Animatable<Insets> {

    private static final Insets NONE = new Insets(0, 0, 0, 0);

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Insets {}

    public Insets inverted() {
        return new Insets(-this.top, -this.bottom, -this.left, -this.right);
    }

    public Insets add(int top, int bottom, int left, int right) {
        return new Insets(this.top + top, this.bottom + bottom, this.left + left, this.right + right);
    }

    public Insets withTop(int top) {
        return new Insets(top, this.bottom, this.left, this.right);
    }

    public Insets withBottom(int bottom) {
        return new Insets(this.top, bottom, this.left, this.right);
    }

    public Insets withLeft(int left) {
        return new Insets(this.top, this.bottom, left, this.right);
    }

    public Insets withRight(int right) {
        return new Insets(this.top, this.bottom, this.left, right);
    }

    public int horizontal() {
        return this.left + this.right;
    }

    public int vertical() {
        return this.top + this.bottom;
    }

    @Override
    public Insets interpolate(Insets next, float delta) {
        return new Insets(
                Mth.lerpInt(delta, this.top, next.top),
                Mth.lerpInt(delta, this.bottom, next.bottom),
                Mth.lerpInt(delta, this.left, next.left),
                Mth.lerpInt(delta, this.right, next.right)
        );
    }

    public static Insets both(int horizontal, int vertical) {
        return new Insets(vertical, vertical, horizontal, horizontal);
    }

    public static Insets top(int top) {
        return new Insets(top, 0, 0, 0);
    }

    public static Insets bottom(int bottom) {
        return new Insets(0, bottom, 0, 0);
    }

    public static Insets left(int left) {
        return new Insets(0, 0, left, 0);
    }

    public static Insets right(int right) {
        return new Insets(0, 0, 0, right);
    }

    public static Insets of(int top, int bottom, int left, int right) {
        return new Insets(top, bottom, left, right);
    }

    public static Insets of(int inset) {
        return new Insets(inset, inset, inset, inset);
    }

    public static Insets vertical(int inset) {
        return new Insets(inset, inset, 0, 0);
    }

    public static Insets horizontal(int inset) {
        return new Insets(0, 0, inset, inset);
    }

    public static Insets none() {
        return NONE;
    }

}

package com.pigicial.wikirenderer.screen.owo.core;

import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.util.Mth;

import java.util.Objects;

public class Positioning implements Animatable<Positioning> {

    private static final Positioning LAYOUT_POSITIONING = new Positioning(0, 0, Type.LAYOUT);

    public final Type type;
    public final int x, y;

    private Positioning(int x, int y, Type type) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public Positioning withX(int x) {
        return new Positioning(x, this.y, this.type);
    }

    public Positioning withY(int y) {
        return new Positioning(this.x, y, this.type);
    }

    public boolean isRelative() {
        return this.type == Type.RELATIVE || this.type == Type.ACROSS;
    }

    @Override
    public Positioning interpolate(Positioning next, float delta) {
        if (next.type != this.type) {
            WikiRenderer.LOGGER.warn("Cannot interpolate between positioning of type " + this.type + " and " + next.type);
            return this;
        }

        return new Positioning(
                Mth.lerpInt(delta, this.x, next.x),
                Mth.lerpInt(delta, this.y, next.y),
                this.type
        );
    }

    /**
     * Position the component at an absolute offset
     * from the root of parent
     *
     * @param xPixels The offset on the x-axis
     * @param yPixels The offset on the y-axis
     */
    public static Positioning absolute(int xPixels, int yPixels) {
        return new Positioning(xPixels, yPixels, Type.ABSOLUTE);
    }

    /**
     * Position the component at a relative offset
     * inside the parent. This respect the size of
     * the component itself. As such:
     * <ul>
     *     <li>50,50 centers the component inside the parent</li>
     *     <li>100,50 centers to component vertically and pushes it all the way to the right</li>
     *     <li>100,100 pushes the component all the way into the bottom right corner of the parent</li>
     * </ul>
     *
     * @param xPercent The offset on the x-axis
     * @param yPercent The offset on the y-axis
     */
    public static Positioning relative(int xPercent, int yPercent) {
        return new Positioning(xPercent, yPercent, Type.RELATIVE);
    }

    /**
     * Position the component the specified percentage
     * across the parent, <i>not including the component's own size</i>
     *
     * @param xPercent The offset on the x-axis
     * @param yPercent The offset on the y-axis
     */
    public static Positioning across(int xPercent, int yPercent) {
        return new Positioning(xPercent, yPercent, Type.ACROSS);
    }

    /**
     * Position the component using whatever layout
     * method the parent component wants to apply
     */
    public static Positioning layout() {
        return LAYOUT_POSITIONING;
    }

    public enum Type {
        RELATIVE, ACROSS, ABSOLUTE, LAYOUT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Positioning that = (Positioning) o;
        return x == that.x && y == that.y && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y);
    }
}

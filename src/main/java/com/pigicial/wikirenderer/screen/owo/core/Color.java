package com.pigicial.wikirenderer.screen.owo.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public record Color(float red, float green, float blue, float alpha) implements Animatable<Color> {

    public static final Color BLACK = Color.ofRgb(0);
    public static final Color WHITE = Color.ofRgb(0xFFFFFF);

    public Color(float red, float green, float blue) {
        this(red, green, blue, 1f);
    }

    public static Color ofArgb(int argb) {
        return new Color(
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f,
                (argb >>> 24) / 255f
        );
    }

    public static Color ofRgb(int rgb) {
        return new Color(
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >> 8) & 0xFF) / 255f,
                (rgb & 0xFF) / 255f,
                1f
        );
    }

    public static Color ofFormatting(@NotNull ChatFormatting formatting) {
        var color = TextColor.fromLegacyFormat(formatting);
        return ofRgb(color == null ? 0 : color.getValue());
    }

    public int argb() {
        return (int) (this.alpha * 255) << 24
                | (int) (this.red * 255) << 16
                | (int) (this.green * 255) << 8
                | (int) (this.blue * 255);
    }

    @Override
    public Color interpolate(Color next, float delta) {
        return new Color(
                Mth.lerp(delta, this.red, next.red),
                Mth.lerp(delta, this.green, next.green),
                Mth.lerp(delta, this.blue, next.blue),
                Mth.lerp(delta, this.alpha, next.alpha)
        );
    }
}

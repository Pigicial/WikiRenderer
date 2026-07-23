package com.pigicial.wikirenderer.render.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

public class EntityVertexPositionTracker implements VertexConsumer {
    public static Matrix4f MODEL_VIEW_PROJECTION;
    public static Integer SCREEN_WIDTH, SCREEN_HEIGHT;

    public static EntityVertexBounds BOUNDS = null;
    public static boolean renderingText = false;

    @Override
    public @NonNull VertexConsumer addVertex(float x, float y, float z) {
        if (MODEL_VIEW_PROJECTION != null) {
            Vector4f v = new Vector4f(x, y, z, 1.0f);
            MODEL_VIEW_PROJECTION.transform(v);
            if (Math.abs(v.w) > 1e-6f) {
                float displayedX = (v.x / v.w + 1.0f) / 2.0f * SCREEN_WIDTH;
                float displayedY = (1.0f - v.y / v.w) / 2.0f * SCREEN_HEIGHT;
                if (BOUNDS == null) {
                    BOUNDS = new EntityVertexBounds(displayedX, displayedY, 0, false);
                } else {
                    BOUNDS.addPoint(displayedX, displayedY, 0, false);
                }
            }
            return this;
        }

        if (BOUNDS == null) {
            BOUNDS = new EntityVertexBounds(x, y, z, renderingText);
        } else {
            BOUNDS.addPoint(x, y, z, renderingText);
        }
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int i, int j, int k, int l) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int i) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv(float f, float g) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv1(int i, int j) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv2(int i, int j) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv3(float u, float v) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setNormal(float f, float g, float h) {
        return this;
    }

    @Override
    public @NonNull VertexConsumer setLineWidth(float f) {
        return this;
    }
}

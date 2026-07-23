package com.pigicial.wikirenderer.render.area;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class FluidVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final Matrix4f transform;
    private final Matrix3f normalTransform;

    public FluidVertexConsumer(VertexConsumer delegate, Matrix4f transform, Matrix3f normalTransform) {
        this.delegate = delegate;
        this.transform = transform;
        this.normalTransform = normalTransform;
    }

    @Override
    public @NonNull VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(this.transform, x, y, z);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int argb) {
        this.delegate.setColor(argb);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        this.delegate.setUv3(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setNormal(float x, float y, float z) {
        Vector3f transformed = new Vector3f(x, y, z).mul(this.normalTransform);
        this.delegate.setNormal(transformed.x(), transformed.y(), transformed.z());
        return this;
    }

    @Override
    public @NonNull VertexConsumer setLineWidth(float width) {
        this.delegate.setLineWidth(width);
        return this;
    }
}

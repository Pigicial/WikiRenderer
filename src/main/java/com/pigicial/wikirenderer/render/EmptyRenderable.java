package com.pigicial.wikirenderer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.pigicial.wikirenderer.property.PropertyBundle;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

public class EmptyRenderable implements Renderable<PropertyBundle> {

    private static final PropertyBundle EMPTY_BUNDLE = new PropertyBundle() {
        @Override
        public void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {}

        @Override
        public void applyToViewMatrix(Renderable<?> renderable, Matrix4fStack modelViewStack) {}

        @Override
        public int getExportResolution(Renderable<?> renderable) {
            return 1000;
        }

        @Override
        public void setExportResolution(Renderable<?> renderable, int resolution) {}
    };

    @Override
    public void emitVerticesThenDraw(RenderScreen renderScreen, Matrix4fStack matrix4fStack, PoseStack matrices, float tickDelta, long timeSinceCreationMs) {}

    @Override
    public void setupLighting() {}

    @Override
    public void drawSubmittedRenderFeatures(@Nullable RenderPass pass, @Nullable FeatureRenderDispatcher.PreparedFrame frame) {}

    @Override
    public PropertyBundle getProperties() {
        return EMPTY_BUNDLE;
    }

    @Override
    public ExportPathSpec getExportPath() {
        return ExportPathSpec.of("", "empty");
    }

    @Override
    public @Nullable String getCustomFileName() {
        return "empty";
    }

    @Override
    public void setCustomFileName(@Nullable String fileName) {

    }
}

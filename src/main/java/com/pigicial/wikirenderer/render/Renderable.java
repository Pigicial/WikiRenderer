package com.pigicial.wikirenderer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.pigicial.wikirenderer.property.CroppablePropertyBundle;
import com.pigicial.wikirenderer.property.PropertyBundle;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.particle.ParticleDisplayCondition;
import com.pigicial.wikirenderer.screen.RenderScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

import java.util.Map;

public interface Renderable<P extends PropertyBundle> {

    Renderable<PropertyBundle> EMPTY = new EmptyRenderable();

    void setupLighting();

    void emitVerticesThenDraw(RenderScreen renderScreen, Matrix4fStack modelViewStack, PoseStack poseStack, float tickDelta, long timeSinceCreationMs);

    default void drawSubmittedRenderFeatures() {
        drawSubmittedRenderFeatures(null, null);
    }

    void drawSubmittedRenderFeatures(@Nullable RenderPass pass, @Nullable FeatureRenderDispatcher.PreparedFrame frame);

    P getProperties();

    ExportPathSpec getExportPath();

    @Nullable
    String getCustomFileName();

    void setCustomFileName(@Nullable String fileName);

    default boolean renderPreviewToEntireScreenWidth() {
        return true;
    }

    default void prepare() {}

    default void onScreenHandle(RenderScreen screen, GuiGraphicsExtractor graphics, float tickDelta) {}

    default boolean onScreenViewportClick(MouseButtonEvent click, boolean doubled) {
        return false;
    }

    default ParticleDisplayCondition getParticleDisplayCondition() {
        return ParticleDisplayCondition.HIDE_ALL;
    }

    default void cleanUp() {}

    default void dispose() {}

    default int getExportResolution() {
        return getProperties().getExportResolution(this);
    }

    default boolean shouldCrop() {
        if (getProperties() instanceof CroppablePropertyBundle croppablePropertyBundle) {
            return croppablePropertyBundle.getCropProperty().get();
        } else {
            return false;
        }
    }

    default boolean shouldCropForFFmpeg() {
        if (getProperties() instanceof CroppablePropertyBundle croppablePropertyBundle) {
            return croppablePropertyBundle.getFFmpegCropProperty().get();
        } else {
            return false;
        }
    }

    default int optionallyOverrideExportWidth(int width) {
        return width;
    }

    default int optionallyOverrideExportHeight(int height) {
        return height;
    }

    default Map<String, String> getPngTextMetadata() {
        return Map.of();
    }

    default void onAnimationStart() {}
}

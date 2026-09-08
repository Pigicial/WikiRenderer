package com.pigicial.wikirenderer.render.export.animation;

import com.mojang.blaze3d.Blaze3D;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.CropData;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.export.FileIO;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class AnimationHandler implements AutoCloseable {
    protected final List<CropData> collectedCropData = Collections.synchronizedList(new ArrayList<>());

    protected final RenderScreen screen;
    protected final Renderable<?> renderable;

    protected final String framesFolderName;
    protected final Path framesFolder;

    private final int animationFrames;
    protected int remainingAnimationFrames;
    protected boolean closed = false;
    protected boolean finished = false;

    private String currentFrame = null;
    private String currentFFmpegFps = null;

    protected AnimationHandler(RenderScreen screen, Renderable<?> renderable, int framesToRender) {
        this.screen = screen;
        this.renderable = renderable;

        ExportPathSpec defaultExportPath = this.renderable.getExportPath();
        ExportPathSpec exportPath = defaultExportPath.differentFileName(renderable.getCustomFileName());
        String fileName = exportPath.filename();

        this.framesFolderName = "sequence_frames/" + fileName + "-" + UUID.randomUUID();
        this.framesFolder = ExportPathSpec.exportRoot().resolve(this.framesFolderName + "/");
        this.animationFrames = framesToRender;
        this.remainingAnimationFrames = framesToRender;
    }

    public abstract void renderAndSaveFrame(float effectiveTickDelta);

    protected void finishAndCleanup(@Nullable File animationFile, @Nullable Throwable error, @Nullable Path framesFolderToLinkTo) {
        this.screen.exportAnimationButton.active = true;
        this.screen.exportAnimationButton.setMessage(Translate.gui("export_animation"));
        if (this.screen.refreshCustomFFmpegPathButton != null) {
            this.screen.refreshCustomFFmpegPathButton.active = true;
            this.screen.refreshCustomFFmpegPathButton.setMessage(Translate.gui("check_ffmpeg_path"));
        }

        this.screen.currentAnimationExportData = null;
        this.closed = true;
        this.collectedCropData.clear();
        WikiRenderer.currentAnimationHandler = null;

        if (animationFile == null || error != null) {
            WikiRenderer.LOGGER.error("Failed to render animation", error);
            Minecraft.getInstance().execute(() -> screen.notify(
                    Translate.gui("animation_export_failed").withStyle(ChatFormatting.RED),
                    Component.literal(String.valueOf(error == null ? "No Error" : error.getMessage())).withStyle(ChatFormatting.GRAY)
            ));
            return;
        }

        Minecraft.getInstance().execute(() -> screen.notify(
                () -> Blaze3D.openPath(animationFile.toPath()),
                Translate.gui("animation_saved"),
                Component.literal(ExportPathSpec.exportRoot().relativize(animationFile.toPath()).toString())
        ));

        if (framesFolderToLinkTo != null) {
            Minecraft.getInstance().execute(() -> screen.notify(
                    () -> Blaze3D.openPath(framesFolderToLinkTo),
                    Translate.gui("animation_frames_saved"),
                    Component.literal(ExportPathSpec.exportRoot().relativize(framesFolderToLinkTo).toString())
            ));
        }
    }

    public boolean isFinished() {
        return this.finished || this.closed;
    }

    @Override
    public void close() {
        this.closed = true;
        this.collectedCropData.clear();
        if (remainingAnimationFrames > 0) {
            FileIO.deleteSequenceFilesFromPath(this.framesFolder);
        }
    }

    public int getAnimationFrames() {
        return animationFrames;
    }

    public int getRemainingFrames() {
        return this.remainingAnimationFrames;
    }

    public void setProgressData(String frame, String fps) {
        this.currentFrame = frame;
        this.currentFFmpegFps = fps;
    }

    public String getCurrentFrame() {
        return currentFrame;
    }

    public String getCurrentFFmpegFps() {
        return currentFFmpegFps;
    }
}

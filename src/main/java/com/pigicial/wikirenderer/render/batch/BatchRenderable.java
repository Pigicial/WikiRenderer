package com.pigicial.wikirenderer.render.batch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.property.CroppablePropertyBundle;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.export.FileIO;
import com.pigicial.wikirenderer.render.item.AnimationTimingsProvider;
import com.pigicial.wikirenderer.render.particle.ParticleDisplayCondition;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.textures.TextureData;
import com.pigicial.wikirenderer.textures.TextureDataProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

import java.util.List;
import java.util.Map;

public class BatchRenderable<R extends Renderable<?>> implements Renderable<BatchPropertyBundle>, TextureDataProvider, AnimationTimingsProvider {

    private final BatchPropertyBundle properties;
    protected final List<R> delegates;
    private final String contentType;

    protected R currentDelegate;
    protected int currentIndex;

    private long renderDelay;
    private long lastRenderTime;

    private boolean batchActive;
    private boolean firstRenderStarted;

    private BatchRenderable(String source, List<R> delegates) {
        this.delegates = delegates;
        this.reset(null);
        this.properties = new BatchPropertyBundle(this, this.currentDelegate.getProperties()); // properties is the same regardless of the index

        this.contentType = ExportPathSpec.exportRoot().resolve("batches/")
                .relativize(FileIO.next(ExportPathSpec.exportRoot().resolve("batches/" + source + "/"))).toString();
        this.renderDelay = Math.max((int) Math.pow(getProperties().getExportResolution(this.currentDelegate) / 1024f, 2) * 100L, 75);

    }

    public static <R extends Renderable<?>> BatchRenderable<?> of(String source, List<R> delegates) {
        if (delegates.isEmpty()) {
            return new BatchRenderable<>(source, List.of(Renderable.EMPTY));
        } else {
            return new BatchRenderable<>(source, delegates);
        }
    }

    @Override
    public void prepare() {
        this.currentDelegate.prepare();
    }

    @Override
    public void setupLighting() {
        this.currentDelegate.setupLighting();
    }

    @Override
    public boolean onScreenViewportClick(MouseButtonEvent click, boolean doubled) {
        return currentDelegate.onScreenViewportClick(click, doubled);
    }

    @Override
    public void onScreenHandle(RenderScreen renderScreen, GuiGraphicsExtractor graphics, float tickDelta) {
        this.currentDelegate.onScreenHandle(renderScreen, graphics, tickDelta);
        WikiRenderer.inBatchRender = this.batchActive;
        if (!batchActive || currentIndex >= this.delegates.size() || System.currentTimeMillis() - lastRenderTime < renderDelay || FileIO.taskCount() > 5) {
            return;

        }

        if (BatchPropertyBundle.EXPORT_AS_ANIMATIONS.get()) {
            if (renderScreen.currentAnimationExportData == null) {
                if (!firstRenderStarted) {
                    firstRenderStarted = true;
                } else {
                    this.next(renderScreen);
                }
                renderScreen.queueAnimationExport();
            }
        } else {
            if (!renderScreen.capturing) {
                if (!firstRenderStarted) {
                    firstRenderStarted = true;
                } else {
                    this.next(renderScreen);
                }
                renderScreen.exportImage(false, tickDelta);
            }
        }
    }

    private void next(RenderScreen screen) {
        this.currentDelegate.dispose();
        this.currentIndex++;
        this.currentDelegate = this.currentIndex < this.delegates.size() ? this.delegates.get(this.currentIndex) : this.currentDelegate;
        this.lastRenderTime = System.currentTimeMillis();
        if (currentIndex == this.delegates.size() - 1) {
            WikiRenderer.inBatchRender = false;
            batchActive = false;
        }

        if (this.currentDelegate instanceof TextureDataProvider) {
            screen.guiRebuildScheduled = true;
        }
    }

    @Override
    public void emitVerticesThenDraw(RenderScreen renderScreen, Matrix4fStack matrix4fStack, PoseStack matrices, float tickDelta, long timeSinceCreationMs) {
        this.currentDelegate.emitVerticesThenDraw(renderScreen, matrix4fStack, matrices, tickDelta, timeSinceCreationMs);
    }

    @Override
    public void drawSubmittedRenderFeatures() {
        this.currentDelegate.drawSubmittedRenderFeatures();
    }

    @Override
    public void drawSubmittedRenderFeatures(@Nullable RenderPass pass, @Nullable FeatureRenderDispatcher.PreparedFrame frame) {
        this.currentDelegate.drawSubmittedRenderFeatures(pass, frame);
    }

    @Override
    public void cleanUp() {
        this.currentDelegate.cleanUp();
    }

    @Override
    public void dispose() {
        this.delegates.forEach(Renderable::dispose);
    }

    @Override
    public ParticleDisplayCondition getParticleDisplayCondition() {
        return this.currentDelegate.getParticleDisplayCondition();
    }

    protected void start() {
        WikiRenderer.inBatchRender = true;
        this.batchActive = true;
        this.currentIndex = 0;
        this.lastRenderTime = System.currentTimeMillis();
        this.renderDelay = Math.max((int) Math.pow(this.getExportResolution() / 1024f, 2) * 100L, 75);
    }

    protected void reset(@Nullable RenderScreen screen) {
        WikiRenderer.inBatchRender = false;
        this.batchActive = false;
        this.lastRenderTime = -1;
        this.currentIndex = 0;
        this.currentDelegate = this.delegates.getFirst();
        this.firstRenderStarted = false;

        if (screen != null && screen.currentAnimationExportData != null) {
            screen.currentAnimationExportData.close();
            screen.currentAnimationExportData = null;
        }
    }

    protected void decreaseIndex() {
        if (this.currentIndex <= 0) {
            this.currentIndex = this.delegates.size() - 1;
        } else {
            this.currentIndex--;
        }
        this.currentDelegate = this.currentIndex < this.delegates.size() ? this.delegates.get(this.currentIndex) : this.currentDelegate;
    }


    protected void increaseIndex() {
        if (this.currentIndex >= this.delegates.size() - 1) {
            this.currentIndex = 0;
        } else {
            this.currentIndex++;
        }
        this.currentDelegate = this.currentIndex < this.delegates.size() ? this.delegates.get(this.currentIndex) : this.currentDelegate;
    }

    @Override
    public BatchPropertyBundle getProperties() {
        return this.properties;
    }

    @Override
    public ExportPathSpec getExportPath() {
        return this.currentDelegate.getExportPath().relocate("batches/" + this.contentType);
    }

    @Override
    public @Nullable String getCustomFileName() {
        if (this.currentDelegate instanceof DynamicBatchLabelProvider provider && BatchPropertyBundle.fileNameFormatter != null) {
            return provider.buildFileName(BatchPropertyBundle.fileNameFormatter);
        } else {
            return this.currentDelegate.getCustomFileName();
        }
    }

    @Override
    public void setCustomFileName(@Nullable String fileName) {
        if (this.currentDelegate instanceof DynamicBatchLabelProvider) {
            BatchPropertyBundle.fileNameFormatter = fileName;
        } else {
            this.currentDelegate.setCustomFileName(fileName);
        }
    }

    @Override
    public @NotNull Map<String, TextureData> getTextureData(Runnable rebuildCallback) {
        if (this.currentDelegate instanceof TextureDataProvider provider) {
            return provider.getTextureData(rebuildCallback);
        } else {
            return Map.of();
        }
    }

    @Override
    public void cacheTextureData(Runnable rebuildCallback) {
        if (this.currentDelegate instanceof TextureDataProvider provider) {
            provider.cacheTextureData(rebuildCallback);
        }
    }

    @Override
    public List<List<Integer>> getTicksToFullyAnimate() {
        if (this.currentDelegate instanceof AnimationTimingsProvider provider) {
            return provider.getTicksToFullyAnimate();
        } else {
            return List.of();
        }
    }

    @Override
    public boolean shouldCrop() {
        if (this.currentDelegate.getProperties() instanceof CroppablePropertyBundle) {
            return this.currentDelegate.shouldCrop();
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldCropForFFmpeg() {
        if (this.currentDelegate.getProperties() instanceof CroppablePropertyBundle) {
            return this.currentDelegate.shouldCropForFFmpeg();
        } else {
            return false;
        }
    }

    @Override
    public Map<String, String> getPngTextMetadata() {
        return this.currentDelegate.getPngTextMetadata();
    }
}

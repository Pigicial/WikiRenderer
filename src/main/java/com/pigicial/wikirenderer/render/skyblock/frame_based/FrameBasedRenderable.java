package com.pigicial.wikirenderer.render.skyblock.frame_based;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.property.PropertyBundle;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.export.FileIO;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.component.ItemComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

import java.util.*;
import java.util.function.Supplier;

public abstract class FrameBasedRenderable<S, R extends Renderable<P>, P extends PropertyBundle> implements Renderable<FrameBasedPropertyBundle<S, R, P>> {

    protected String customFileName = null;
    private final UUID entityID;
    private final Supplier<TreeMap<Integer, S>> dataSourceSupplier;
    private final FrameBasedPropertyBundle<S, R, P> propertyBundle;

    protected int lastFetchRawFrameCount;
    protected List<FrameData<S>> currentDataSet;
    private InterpolatedTimings timingData;

    protected S lastUpdatedSourceData;
    protected R renderable;
    protected FrameData<S> currentFrame;
    protected int currentIndex;

    private boolean renderActive;
    private int currentFrameTotalExportsSoFar;
    private int currentFrameTotalDuration;

    public FrameBasedRenderable(UUID entityID, Supplier<TreeMap<Integer, S>> dataSourceSupplier, P propertyBundle) {
        this.entityID = entityID;
        this.dataSourceSupplier = dataSourceSupplier;
        this.propertyBundle = new FrameBasedPropertyBundle<>(this, propertyBundle);
        this.fetch();
    }

    public void fetch() {
        currentDataSet = new ArrayList<>();

        TreeMap<Integer, S> data = this.dataSourceSupplier.get();
        for (Map.Entry<Integer, S> entry : data.entrySet()) {
            currentDataSet.add(new FrameData<>(entry.getKey(), entry.getValue()));
        }
        lastFetchRawFrameCount = data.size();

        FrameData<S> firstMarkedFrame = currentDataSet.getFirst();
        S matchingFirstMarkedData = this.getMatchingFirstMarkedData();

        if (matchingFirstMarkedData != null) {
            for (int frameIndex = 0, currentDataSetSize = currentDataSet.size(); frameIndex < currentDataSetSize; frameIndex++) {
                FrameData<S> frameData = currentDataSet.get(frameIndex);
                if (this.sourceDataMatches(frameData.sourceData(), matchingFirstMarkedData)) {
                    // WikiRenderer.LOGGER.info("Matching first frame is {}", frameIndex);
                    firstMarkedFrame = frameData;

                    if (frameIndex > 0) {
                        currentDataSet.subList(0, frameIndex).clear();
                        // WikiRenderer.LOGGER.info("Removing 0 to {} due to it being cut off (before the first marked frame)", frameIndex);
                    }
                    break;
                }
            }
        }

        // auto-refresh the first frame incase it changes (i.e. from not seeing a matching frame as to what you selected before when you opened it to then seeing it)
        this.currentFrame = currentDataSet.getFirst();
        this.currentIndex = 0;
        this.getOrUpdateRenderable();

        // get animation ranges
        List<int[]> subAnimationsRanges = new ArrayList<>();
        int lastMatchingIndex = 0;
        for (int i = 0, currentDataSetSize = currentDataSet.size(); i < currentDataSetSize; i++) {
            FrameData<S> frameData = currentDataSet.get(i);
            if (sourceDataMatches(frameData.sourceData(), firstMarkedFrame.sourceData())) {
                if (i != 0) {
                    subAnimationsRanges.add(new int[]{lastMatchingIndex, i});
                    // WikiRenderer.LOGGER.info("Range {} to {} is duration {}", lastMatchingIndex, i, i - lastMatchingIndex);
                }
                lastMatchingIndex = i;
            }
        }

        if (subAnimationsRanges.isEmpty()) {
            // WikiRenderer.LOGGER.warn("No complete animation loops found");
            return;
        }

        // remove cut off loop at the end
        if (lastMatchingIndex < currentDataSet.size()) {
            currentDataSet.subList(lastMatchingIndex, currentDataSet.size()).clear();
            // WikiRenderer.LOGGER.info("Removing {} to {} due to it being cut off", lastMatchingIndex, currentDataSet.size());
        }

        // remove shorter loops (missing textures due to server lag or whatever)
        int longestLoopFrameCount = subAnimationsRanges.stream().mapToInt(range -> range[1] - range[0]).max().orElseThrow();
        for (int i = subAnimationsRanges.size() - 1; i >= 0; i--) {
            int[] range = subAnimationsRanges.get(i);
            int frameCount = range[1] - range[0];
            if (frameCount != longestLoopFrameCount) {
                // WikiRenderer.LOGGER.info("Removing {} to {} due to {} not matching {}", range[0], range[1], frameCount, longestLoopFrameCount);
                currentDataSet.subList(range[0], range[1]).clear();
                subAnimationsRanges.remove(i);
            }
        }

        // re-derive ranges from clean dataset
        int loopCount = subAnimationsRanges.size();
        // WikiRenderer.LOGGER.info("Clean dataset: {} loops of duration {}", loopCount, longestLoopFrameCount);
        this.timingData = this.getTimings(currentDataSet, longestLoopFrameCount);
        this.timingData.resetForEntity(entityID);

        for (int loop = 0; loop < loopCount; loop++) {
            int from = loop * longestLoopFrameCount;
            int to = from + longestLoopFrameCount;

            for (int i = from; i < to; i++) {
                FrameData<S> frameData = currentDataSet.get(i);
                int animationIndex = i % longestLoopFrameCount;

                int frameDuration;
                if (i + 1 < currentDataSet.size()) {
                    // time til next frame (if it exists)
                    frameDuration = currentDataSet.get(i + 1).recordedTimingMsOffset()
                                    - frameData.recordedTimingMsOffset();
                } else {
                    if (i == 0) {
                        frameDuration = 50;
                    } else {
                        // use second-to-last frame's duration as a guess
                        frameDuration = currentDataSet.get(i).recordedTimingMsOffset()
                                        - currentDataSet.get(i - 1).recordedTimingMsOffset();
                    }
                }

                timingData.submit(entityID, animationIndex, frameDuration);
            }
        }

        if (loopCount > 1) {
            currentDataSet.subList(longestLoopFrameCount, currentDataSet.size()).clear();
        }
    }

    public abstract ItemComponent createItemComponentForPreview(FrameData<S> frameData);

    protected abstract boolean sourceDataMatches(S data1, S data2);

    @NotNull
    protected abstract InterpolatedTimings getTimings(List<FrameData<S>> currentDataSet, int framesCount);

    @Nullable
    protected abstract S getMatchingFirstMarkedData();

    protected abstract List<String> generateWikiTextFile(List<FrameData<S>> currentDataSet);

    protected abstract R createBlankRenderable();

    protected abstract void updateRenderable(R renderable, S sourceData);

    protected R getOrUpdateRenderable() {
        if (this.lastUpdatedSourceData == null || !this.sourceDataMatches(lastUpdatedSourceData, currentFrame.sourceData())) {
            if (renderable == null) {
                renderable = this.createBlankRenderable();
            }
            this.updateRenderable(renderable, currentFrame.sourceData());
            this.lastUpdatedSourceData = currentFrame.sourceData();
        }

        return this.renderable;
    }

    @Override
    public void onScreenHandle(RenderScreen renderScreen, GuiGraphicsExtractor graphics, float tickDelta) {
        this.getOrUpdateRenderable().onScreenHandle(renderScreen, graphics, tickDelta);
        if (timingData == null && renderScreen.exportAnimationButton != null) {
            renderScreen.exportAnimationButton.active = false;
        }

        if (renderScreen.currentAnimationExportData == null && this.dataSourceSupplier.get().size() > lastFetchRawFrameCount) {
            boolean hadTimingDataBefore = timingData != null;

            fetch();

            boolean hasTimingDataNow = timingData != null;
            if (hadTimingDataBefore != hasTimingDataNow) {
                renderScreen.guiRebuildScheduled = true;
            }
        }

        if (!renderActive || currentIndex >= currentDataSet.size() || FileIO.taskCount() > 5) {
            return;
        }

        if (renderScreen.currentAnimationExportData != null) {
            if (currentFrameTotalExportsSoFar == currentFrameTotalDuration) {
                this.next(renderScreen);
            }
            currentFrameTotalExportsSoFar++;
        }
    }

    @Override
    public void onAnimationStart() {
        this.renderActive = true;
        this.currentIndex = 0;
        this.currentFrame = currentDataSet.getFirst();
        this.currentFrameTotalExportsSoFar = 0;
        this.currentFrameTotalDuration = timingData.getTickTimingMinimized(currentIndex);

        GlobalProperties.get().exportFramerate.set(timingData.getFPS());
        GlobalProperties.get().exportFrames.set(timingData.getTotalTickDuration());
    }

    private void next(RenderScreen screen) {
        this.getOrUpdateRenderable().dispose();

        if (currentIndex + 1 >= this.currentDataSet.size()) {
            renderActive = false;
            currentIndex = 0;
            currentFrame = this.currentDataSet.getFirst();

            // for the start button
            screen.guiRebuildScheduled = true;

            this.saveFileData(screen);
        } else {
            this.currentIndex++;
            this.currentFrame = this.currentDataSet.get(this.currentIndex);
            this.currentFrameTotalExportsSoFar = 0;
            this.currentFrameTotalDuration = timingData.getTickTimingMinimized(currentIndex);
        }
    }

    protected void saveFileData(RenderScreen screen) {
        String fileText = String.join("\n", this.generateWikiTextFile(currentDataSet));

        ExportPathSpec defaultExportPath = this.getExportPath();
        ExportPathSpec exportPath = defaultExportPath.differentFileName(this.getCustomFileName());

        FileIO.saveTextAndNotify(fileText, exportPath, screen, "exported_texture_data_as");
    }

    public InterpolatedTimings getTimingData() {
        return timingData;
    }

    public List<FrameData<S>> getCurrentDataSet() {
        return currentDataSet;
    }

    @Override
    public void setupLighting() {
        this.getOrUpdateRenderable().setupLighting();
    }

    @Override
    public void prepare() {
        this.getOrUpdateRenderable().prepare();
    }

    @Override
    public void cleanUp() {
        this.getOrUpdateRenderable().cleanUp();
    }

    @Override
    public void emitVerticesThenDraw(RenderScreen renderScreen, Matrix4fStack modelViewStack, PoseStack poseStack, float tickDelta, long timeSinceCreationMs) {
        this.getOrUpdateRenderable().emitVerticesThenDraw(renderScreen, modelViewStack, poseStack, tickDelta, timeSinceCreationMs);
    }

    @Override
    public void drawSubmittedRenderFeatures() {
        this.getOrUpdateRenderable().drawSubmittedRenderFeatures();
    }

    @Override
    public void drawSubmittedRenderFeatures(@Nullable RenderPass pass, @Nullable FeatureRenderDispatcher.PreparedFrame frame) {
        this.getOrUpdateRenderable().drawSubmittedRenderFeatures(pass, frame);
    }

    @Override
    public FrameBasedPropertyBundle<S, R, P> getProperties() {
        return propertyBundle;
    }

    @Override
    public ExportPathSpec getExportPath() {
        return this.getOrUpdateRenderable().getExportPath();
    }

    @Override
    public @Nullable String getCustomFileName() {
        return this.customFileName;
    }

    @Override
    public void setCustomFileName(@Nullable String fileName) {
        this.customFileName = fileName;
    }
}

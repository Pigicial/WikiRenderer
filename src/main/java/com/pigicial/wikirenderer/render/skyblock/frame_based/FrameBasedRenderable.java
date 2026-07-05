package com.pigicial.wikirenderer.render.skyblock.frame_based;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.property.PropertyBundle;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.export.FileIO;
import com.pigicial.wikirenderer.screen.RenderScreen;
import io.wispforest.owo.ui.component.ItemComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    // for head-based animations with the texture-marked frame appearing multiple times, being able to adjust the timings to ensure consistency is very useful
    private final List<Integer> validPhaseOffsets = new ArrayList<>();
    private int phaseOffsetIndex = 0;
    private long lastPhaseOffsetsHash = 0;

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

    public abstract ItemComponent createItemComponentForPreview(FrameData<S> frameData);

    protected abstract boolean sourceDataMatches(S data1, S data2);

    @NotNull
    protected abstract InterpolatedTimings getTimings(List<FrameData<S>> currentDataSet, int framesCount);

    @Nullable
    protected abstract S getMatchingFirstMarkedData();

    protected abstract List<String> generateWikiTextFile(List<FrameData<S>> currentDataSet);

    protected abstract R createBlankRenderable();

    protected abstract void updateRenderable(R renderable, S sourceData);

    public void fetch() {
        currentDataSet = new ArrayList<>();

        TreeMap<Integer, S> data = this.dataSourceSupplier.get();
        for (Map.Entry<Integer, S> entry : data.entrySet()) {
            currentDataSet.add(new FrameData<>(entry.getKey(), entry.getValue()));
        }
        lastFetchRawFrameCount = data.size();

        // trim frames before the user-selected first frame
        S matchingFirstMarkedData = this.getMatchingFirstMarkedData();

        if (matchingFirstMarkedData != null) {
            for (int frameIndex = 0; frameIndex < currentDataSet.size(); frameIndex++) {
                FrameData<S> frameData = currentDataSet.get(frameIndex);
                if (this.sourceDataMatches(frameData.sourceData(), matchingFirstMarkedData)) {
                    if (frameIndex > 0) {
                        currentDataSet.subList(0, frameIndex).clear();
                    }
                    break;
                }
            }
        }

        // auto-refresh the first frame incase it changes (i.e. from not seeing a matching frame as to what you selected before when you opened it to then seeing it)
        this.currentFrame = currentDataSet.getFirst();
        this.currentIndex = 0;
        this.getOrUpdateRenderable();

        if (currentDataSet.size() < 2) {
            return;
        }

        // find the loop length that tiles the dataset most consistently
        int bestLength = -1;
        int bestScore = -1;

        // todo make this check most frequent combination of hashes, not most frequent length
        for (int candidateLength = 1; candidateLength <= currentDataSet.size(); candidateLength++) {
            int matches = 0;

            for (int i = candidateLength; i < currentDataSet.size(); i++) {
                S reference = currentDataSet.get(i % candidateLength).sourceData();
                S actual = currentDataSet.get(i).sourceData();
                if (sourceDataMatches(reference, actual)) {
                    matches++;
                }
            }

            int requiredMinMatchAmount = bestScore == -1 ? 1 : 2;
            if (matches >= requiredMinMatchAmount && (matches > bestScore || (matches == bestScore && candidateLength > bestLength))) {
                bestScore = matches;
                bestLength = candidateLength;
            }
        }

        if (bestLength == -1) {
            // WikiRenderer.LOGGER.warn("No complete animation loops found");
            return;
        }

        int loopCount = currentDataSet.size() / bestLength;

        // remove cut off loop at the end
        currentDataSet.subList(loopCount * bestLength, currentDataSet.size()).clear();

        // WikiRenderer.LOGGER.info("Clean dataset: {} loops of duration {}", loopCount, bestLength);
        this.timingData = this.getTimings(currentDataSet, bestLength);
        this.timingData.resetForEntity(entityID);

        for (int loop = 0; loop < loopCount; loop++) {
            int from = loop * bestLength;
            int to = from + bestLength;

            for (int i = from; i < to; i++) {
                FrameData<S> frameData = currentDataSet.get(i);
                int animationIndex = i % bestLength;

                int frameDuration;
                if (i + 1 < currentDataSet.size()) {
                    frameDuration = currentDataSet.get(i + 1).recordedTimingMsOffset()
                                    - frameData.recordedTimingMsOffset();
                } else {
                    if (i == 0) {
                        frameDuration = 50;
                    } else {
                        frameDuration = currentDataSet.get(i).recordedTimingMsOffset()
                                        - currentDataSet.get(i - 1).recordedTimingMsOffset();
                    }
                }

                timingData.submit(entityID, animationIndex, frameDuration);
            }
        }

        if (loopCount > 1) {
            currentDataSet.subList(bestLength, currentDataSet.size()).clear();
        }

        List<Integer> newOffsets = new ArrayList<>();
        S firstFrameData = currentDataSet.getFirst().sourceData();
        for (int i = 0; i < currentDataSet.size(); i++) {
            if (sourceDataMatches(currentDataSet.get(i).sourceData(), firstFrameData)) {
                newOffsets.add(i);
            }
        }

        long newHash = computePhaseOffsetsHash(newOffsets);
        if (newHash != lastPhaseOffsetsHash) {
            validPhaseOffsets.clear();
            validPhaseOffsets.addAll(newOffsets);
            lastPhaseOffsetsHash = newHash;
            phaseOffsetIndex = 0;
        }
    }

    private long computePhaseOffsetsHash(List<Integer> offsets) {
        long hash = 1;
        for (int offset : offsets) {
            hash = hash * 31 + offset;
        }
        return hash;
    }

    public FrameData<S> getFrame(int index) {
        if (validPhaseOffsets.isEmpty()) {
            return currentDataSet.get(index);
        }
        int offset = validPhaseOffsets.get(phaseOffsetIndex);
        return currentDataSet.get((index + offset) % currentDataSet.size());
    }

    public void cyclePhaseOffset(int delta) {
        if (validPhaseOffsets.size() <= 1) {
            return;
        }

        this.phaseOffsetIndex = (phaseOffsetIndex + delta + validPhaseOffsets.size()) % validPhaseOffsets.size();
        this.currentIndex = 0;
        this.currentFrame = getFrame(0);
        this.lastUpdatedSourceData = null;
    }

    public List<Integer> getValidPhaseOffsets() {
        return validPhaseOffsets;
    }

    public int getPhaseOffsetIndex() {
        return phaseOffsetIndex;
    }

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
            boolean hadRepeatingFirstTexturesBefore = validPhaseOffsets.size() >= 2;

            fetch();

            boolean hasTimingDataNow = timingData != null;
            boolean hasRepeatingFirstTexturesNow = validPhaseOffsets.size() >= 2;
            if (hadTimingDataBefore != hasTimingDataNow || hadRepeatingFirstTexturesBefore != hasRepeatingFirstTexturesNow) {
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
        this.currentFrameTotalDuration = timingData.getTickTimingMinimized(currentIndex, this);

        GlobalProperties.get().exportFramerate.set(timingData.getFPS());
        GlobalProperties.get().exportFrames.set(timingData.getTotalTickDuration());
    }

    private void next(RenderScreen screen) {
        this.getOrUpdateRenderable().dispose();

        if (currentIndex + 1 >= this.currentDataSet.size()) {
            renderActive = false;
            currentIndex = 0;
            currentFrame = this.getFrame(0);

            // for the start button
            screen.guiRebuildScheduled = true;

            this.saveFileData(screen);
        } else {
            this.currentIndex++;
            this.currentFrame = this.getFrame(this.currentIndex);
            this.currentFrameTotalExportsSoFar = 0;
            this.currentFrameTotalDuration = timingData.getTickTimingMinimized(currentIndex, this);
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

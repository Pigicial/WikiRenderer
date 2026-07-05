package com.pigicial.wikirenderer.render.skyblock.frame_based;

import com.pigicial.wikirenderer.property.IntProperty;
import com.pigicial.wikirenderer.property.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InterpolatedTimings {
    private final Property<Boolean> useCustomFrameTime = Property.of(false);
    @Nullable
    private IntProperty customFrameTime = null;

    private final List<FrameTime> frameTimes;
    private final int frameCount;

    public InterpolatedTimings(int frameCount) {
        this.frameTimes = new ArrayList<>();
        this.frameCount = frameCount;

        for (int i = 0; i < frameCount; i++) {
            frameTimes.add(new FrameTime());
        }
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void submit(UUID entityID, int index, int millisecondDuration) {
        this.frameTimes.get(index).addMillisecondTiming(entityID, millisecondDuration);
    }

    public int getTickTimingMinimized(int index, FrameBasedRenderable<?, ?, ?> renderable) {
        if (useCustomFrameTime.get() && customFrameTime != null) {
            return customFrameTime.get();
        } else {
            return this.getFrame(index, renderable).getAverageTickTime();
        }
    }

    public int getRawTotalTickDuration() {
        int ticks = 0;
        for (FrameTime frameTime : frameTimes) {
            ticks += frameTime.getAverageTickTime();
        }
        return ticks;
    }

    public int getTotalTickDuration() {
        int ticks = 0;
        for (FrameTime frameTime : frameTimes) {
            if (useCustomFrameTime.get() && customFrameTime != null) {
                ticks += customFrameTime.get();
            } else {
                ticks += frameTime.getAverageTickTime();
            }
        }
        return ticks;
    }

    public int getFPS() {
        return 20;
    }

    public String getTickValuesWithPossibleOffsetsApplied(FrameBasedRenderable<?, ?, ?> renderable) {
        List<String> values = new ArrayList<>();
        for (int i = 0, frameTimesSize = frameTimes.size(); i < frameTimesSize; i++) {
            FrameTime frameTime = this.getFrame(i, renderable);
            values.add(String.valueOf(frameTime.getAverageTickTime()));
        }
        return String.join(", ", values);
    }

    private FrameTime getFrame(int index, FrameBasedRenderable<?, ?, ?> renderable) {
        if (renderable.getValidPhaseOffsets().isEmpty()) {
            return frameTimes.get(index);
        }
        int offset = renderable.getValidPhaseOffsets().get(renderable.getPhaseOffsetIndex());
        return frameTimes.get((index + offset) % frameTimes.size());
    }

    public void resetForEntity(UUID entityID) {
        for (FrameTime frameTime : frameTimes) {
            frameTime.clear(entityID);
        }
    }

    public Property<Boolean> getUseCustomFrameTimeProperty() {
        return useCustomFrameTime;
    }

    public IntProperty getOrSetupCustomFrameTimeProperty() {
        if (customFrameTime == null) {
            Map<Integer, Integer> frameTimeFrequencies = new HashMap<>();
            for (FrameTime frameTime : frameTimes) {
                int averageTickTime = frameTime.getAverageTickTime();
                frameTimeFrequencies.put(averageTickTime, frameTimeFrequencies.getOrDefault(averageTickTime, 0) + 1);
            }

            int mostRecurringEntry = 0;
            int mostRecurringEntryAmounts = 0;
            for (Map.Entry<Integer, Integer> entry : frameTimeFrequencies.entrySet()) {
                if (entry.getValue() > mostRecurringEntryAmounts) {
                    mostRecurringEntryAmounts = entry.getValue();
                    mostRecurringEntry = entry.getKey();
                }
            }
            if (mostRecurringEntry == 0) mostRecurringEntry = 1;

            customFrameTime = IntProperty.of(mostRecurringEntry, 1, 1000);
        }
        return customFrameTime;
    }

    public int getAmountOfLoops() {
        return frameTimes.stream().mapToInt(FrameTime::getAmountOfTimings).max().orElse(0);
    }
}

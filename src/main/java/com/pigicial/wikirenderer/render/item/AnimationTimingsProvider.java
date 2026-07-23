package com.pigicial.wikirenderer.render.item;

import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.util.AnimationTimingUtil;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.List;

public interface AnimationTimingsProvider {
    List<List<Integer>> getTicksToFullyAnimate();

    default void buildTimingsSection(FlowLayout layout) {
        try (WikiRendererUI.RowBuilder builder = WikiRendererUI.autoNewLineRow(layout)) {
            WikiRendererUI.dynamicText(builder.row, () -> {
                List<List<Integer>> textureTimings = this.getTicksToFullyAnimate();
                if (textureTimings == null || textureTimings.isEmpty() || textureTimings.getFirst().isEmpty()) return Component.empty();

                DecimalFormat df = new DecimalFormat("###.##");
                String timingsKey = textureTimings.stream().mapToInt(List::size).sum() == 1 ? "texture_timings_data_single" : "texture_timings_data_multiple";

                long seamlessLoopTicks = AnimationTimingUtil.getSeamlessLoopDuration(textureTimings);
                Component seamlessLoopTicksText = Component.literal(df.format(seamlessLoopTicks)).withStyle(ChatFormatting.GREEN);
                Component seamlessLoopSecondsText = Component.literal(df.format(seamlessLoopTicks / 20D) + "s").withStyle(ChatFormatting.AQUA);
                return Translate.gui(timingsKey, seamlessLoopSecondsText, seamlessLoopTicksText);
            });
        }
    }
}

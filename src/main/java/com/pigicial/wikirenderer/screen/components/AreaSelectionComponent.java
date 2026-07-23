package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.WikiRendererKeybinds;
import com.pigicial.wikirenderer.render.area.AreaSelectionHelper;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Color;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.Surface;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class AreaSelectionComponent extends FlowLayout {

    public AreaSelectionComponent() {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);

        this.surface(Surface.flat(0x77000000).and(Surface.outline(0x77000000)));
        this.padding(Insets.of(5));

        this.child(WikiRendererUI.label(Translate.PREFIX).shadow(true).margins(Insets.bottom(10)));

        this.child(WikiRendererUI.label(Translate.gui("hud.area_selection")).shadow(true));
        this.child(new DynamicLabelComponent(positionText(() -> AreaSelectionHelper.pos1, "from")).shadow(true).color(Color.ofFormatting(ChatFormatting.GRAY)));
        this.child(new DynamicLabelComponent(positionText(() -> AreaSelectionHelper.pos2, "to")).shadow(true).color(Color.ofFormatting(ChatFormatting.GRAY)).margins(Insets.bottom(10)));

        Component firstKeybind = Component.keybind(WikiRendererKeybinds.KEYBIND_SELECT_AREA.getName()).withStyle(ChatFormatting.YELLOW);
        Component secondKeybind = Component.keybind(WikiRendererKeybinds.KEYBIND_SELECT_AREA_EXPAND.getName()).withStyle(ChatFormatting.YELLOW);
        this.child(WikiRendererUI.label(Translate.gui("hud.area_selection.clear_hint", firstKeybind, secondKeybind)).shadow(true));
    }

    private Supplier<Component> positionText(Supplier<BlockPos> positionSupplier, String key) {
        return () -> {
            BlockPos position = positionSupplier.get();
            Component positionText = Component.literal(position == null ? "---" : position.getX() + " " + position.getY() + " " + position.getZ());
            return Translate.gui("hud.area_selection." + key, positionText);
        };
    }

}

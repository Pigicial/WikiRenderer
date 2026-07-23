package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.util.UISounds;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.NonNull;

public class MiniEditBoxComponent extends TextBoxComponent {
    public MiniEditBoxComponent(Sizing horizontalSizing, String text) {
        super(horizontalSizing);
        this.setMaxLength(Integer.MAX_VALUE);
        this.text(text);
        this.verticalSizing(Sizing.fixed(14));
        this.horizontalSizing(horizontalSizing);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (super.mouseClicked(mouseButtonEvent, bl)) {
            UISounds.playInteractionSound();
            return true;
        }
        return false;
    }
}

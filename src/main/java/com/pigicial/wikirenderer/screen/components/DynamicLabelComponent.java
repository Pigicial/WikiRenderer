package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.core.Color;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class DynamicLabelComponent extends AutoResizingLabelComponent {

    private final Font textRenderer = Minecraft.getInstance().font;

    private final Supplier<Component> content;

    public DynamicLabelComponent(Supplier<Component> content) {
        super(content.get());
        super.color(Color.WHITE);
        super.shadow(true);
        this.content = content;
    }

    public DynamicLabelComponent shadow(boolean shadow) {
        super.shadow(shadow);
        return this;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return Math.max(super.determineVerticalContentSize(sizing), this.textRenderer.lineHeight);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        Component newText = this.content.get();
        if (newText.getString().isEmpty()) {
            return;
        }
        this.text(newText);
        this.applySizing();
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
    }
}

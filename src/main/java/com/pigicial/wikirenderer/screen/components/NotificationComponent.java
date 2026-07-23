package com.pigicial.wikirenderer.screen.components;

import com.mojang.blaze3d.platform.InputConstants;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.component.LabelComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.*;
import com.pigicial.wikirenderer.screen.owo.util.UISounds;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class NotificationComponent extends FlowLayout {

    private float age = 0;

    public NotificationComponent(RenderScreen screen, @Nullable Runnable onClick, Component... messages) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);

        this.margins(Insets.top(5));
        this.padding(Insets.of(10));
        this.surface(Surface.flat(0x77000000).and(Surface.outline(0x77000000)));

        if (onClick != null) {
            this.cursorStyle(CursorStyle.HAND);
            this.mouseDown().subscribe((click, doubled) -> {
                if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

                screen.openingFile = true;
                onClick.run();
                UISounds.playInteractionSound();

                return true;
            });
        }

        int maxWidth = screen.viewportEndX - screen.viewportBeginX - 40;
        for (Component message : messages) {
            LabelComponent label = WikiRendererUI.label(message).maxWidth(maxWidth);
            if (onClick != null) {
                label.tooltip(Translate.gui("click_to_open").withStyle(ChatFormatting.GRAY)).cursorStyle(this.cursorStyle);
            }

            this.child(label);
        }
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        super.parentUpdate(delta, mouseX, mouseY);
        this.age += delta * 50;

        if (this.age > 6000 && this.horizontalSizing.get().method == Sizing.Method.CONTENT) {
            this.verticalSizing(Sizing.fixed(this.height));
            this.horizontalSizing(Sizing.fixed(this.width));

            this.margins.animate(2000, Easing.CUBIC, Insets.none()).forwards();
            this.verticalSizing.animate(2000, Easing.CUBIC, Sizing.fixed(0)).forwards();
            this.horizontalSizing.animate(2000, Easing.CUBIC, Sizing.fixed(0)).forwards();
        }

        if (this.age > 8000) {
            this.queue(() -> {
                if (this.parent != null) {
                    this.parent.removeChild(this);
                }
            });
        }
    }
}

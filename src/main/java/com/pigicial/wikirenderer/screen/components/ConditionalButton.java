package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.component.ButtonComponent;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConditionalButton extends ButtonComponent {
    private final Supplier<Boolean> enabled;

    public ConditionalButton(Component message, Consumer<ButtonComponent> onPress, Supplier<Boolean> enabled) {
        super(message, onPress);
        this.enabled = enabled;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        super.active = enabled.get();
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
    }
}

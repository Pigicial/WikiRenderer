package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BooleanOverride<S extends EntityRenderState> extends OptionalOverride<S, Boolean> {

    private boolean simpleToggle;

    public BooleanOverride(String key, Function<S, Boolean> getter, BiConsumer<S, Boolean> setter) {
        super(key, getter, setter);
    }

    public BooleanOverride(String key) {
        super(key, s -> false, (s, b) -> {});
        this.simpleToggle = true;
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {
        if (!simpleToggle) {
            row.child(new SearchableEntityListComponent.DynamicTextButton(this::label, unused -> this.setValue(!getValue())).margins(Insets.right(3)));
        }
    }

    private Component label() {
        if (this.getValue()) {
            return Component.literal("True").withStyle(!this.enabled ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN);
        } else {
            return Component.literal("False").withStyle(!this.enabled ? ChatFormatting.DARK_GRAY : ChatFormatting.RED);
        }
    }

    @Override
    public Boolean getValue() {
        return simpleToggle ? enabled : super.getValue();
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }
}

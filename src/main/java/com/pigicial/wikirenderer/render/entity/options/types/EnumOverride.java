package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.FullWidthCollapsibleContainer;
import com.pigicial.wikirenderer.screen.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class EnumOverride<S extends EntityRenderState, E extends Enum<E>> extends OptionalOverride<S, E> {

    private final Class<E> enumClass;

    public EnumOverride(String key, Class<E> enumClass, Function<S, E> getter, BiConsumer<S, E> setter) {
        super(key, getter, setter, enumClass.getEnumConstants()[0]);
        this.enumClass = enumClass;
    }

    public UIComponent buildComponent() {
        SearchableEntityListComponent.LeftAlignedCheckbox checkbox = new SearchableEntityListComponent.LeftAlignedCheckbox(
                Component.literal(toDisplayName(key)),
                Sizing.fill(50),
                () -> this.enabled,
                pressed -> this.enabled = pressed
        );

        FullWidthCollapsibleContainer layout = new FullWidthCollapsibleContainer(checkbox, () -> {
            E value = getValue();
            return value == null ? Translate.gui("not_set") : Component.literal(toDisplayName(value.toString()));
        }, false);
        for (E enumOption : enumClass.getEnumConstants()) {
            layout.child(addOption(enumOption));
        }

        return layout;
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {

    }

    private SearchableEntityListComponent.DynamicTextButton addOption(@Nullable E enumOption) {
        return new SearchableEntityListComponent.DynamicTextButton(() -> {
            MutableComponent component = enumOption == null ? Translate.gui("not_set") : Component.literal(toDisplayName(enumOption.toString()));

            boolean isOption = getValue() == enumOption;
            ChatFormatting color = this.enabled ? (isOption ? ChatFormatting.GREEN : ChatFormatting.WHITE) : ChatFormatting.DARK_GRAY;

            return component.withStyle(color);
        }, ignored -> setValue(enumOption));
    }

    @Override
    public E getDefaultValue() {
        return enumClass.getEnumConstants()[0];
    }
}

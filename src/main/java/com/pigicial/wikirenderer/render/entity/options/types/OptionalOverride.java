package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.GridLayout;
import com.pigicial.wikirenderer.screen.owo.container.UIContainers;
import com.pigicial.wikirenderer.screen.owo.core.HorizontalAlignment;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;
import com.pigicial.wikirenderer.screen.owo.core.VerticalAlignment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class OptionalOverride<S extends EntityRenderState, T> {
    protected final String key;
    private final Function<S, T> getter;
    protected final BiConsumer<S, T> setter;

    protected T value;
    protected boolean enabled = false;

    public OptionalOverride(String key, Function<S, T> getter, BiConsumer<S, T> setter) {
        this.key = key;
        this.getter = getter;
        this.setter = setter;
        this.value = getDefaultValue();
    }

    public OptionalOverride(String key, Function<S, T> getter, BiConsumer<S, T> setter, T defaultValue) {
        this.key = key;
        this.getter = getter;
        this.setter = setter;
        this.value = defaultValue;
    }

    public void reset() {
        setValue(getDefaultValue());
        this.enabled = false;
    }

    public abstract T getDefaultValue();

    public void setValue(T value) {
        this.enabled = true;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void apply(S renderState) {
        if (enabled) {
            this.setter.accept(renderState, value);
        }
    }

    public void copyFromRenderState(S renderState) {
        this.value = this.getter.apply(renderState);
    }

    public UIComponent buildComponent() {
        FlowLayout controlLayout = UIContainers.horizontalFlow(Sizing.expand(50), Sizing.content());
        controlLayout.horizontalAlignment(HorizontalAlignment.RIGHT);
        this.addToComponentRow(controlLayout);

        GridLayout layout = UIContainers.grid(Sizing.expand(100), Sizing.content(), 1, 2);
        layout.verticalAlignment(VerticalAlignment.CENTER);
        layout.child(new SearchableEntityListComponent.LeftAlignedCheckbox(Component.literal(toDisplayName(key)), Sizing.fill(50), () -> this.enabled, pressed -> this.enabled = pressed), 0, 0);
        layout.child(controlLayout, 0, 1);
        return layout;
    }

    public static String toDisplayName(String input) {
        return Arrays.stream(input
                        .replaceAll("([a-z])([A-Z])", "$1_$2")       // camelCase -> snake
                        .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2") // CLAYFish -> CLAY_Fish
                        .toLowerCase()
                        .split("_")
                )
                .filter(w -> !w.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    protected abstract void addToComponentRow(FlowLayout row);
}

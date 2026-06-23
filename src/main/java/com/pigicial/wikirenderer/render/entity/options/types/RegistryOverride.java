package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.components.FullWidthCollapsibleContainer;
import com.pigicial.wikirenderer.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.util.Translate;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RegistryOverride<S extends EntityRenderState, R> extends OptionalOverride<S, R> {

    private static HolderLookup.Provider vanillaLookup;
    private final List<Holder.Reference<R>> options = new ArrayList<>();
    private final BiFunction<ResourceKey<R>, R, String> toString;
    private final boolean forcedFallback;
    private R defaultFallback = null;

    private Holder.Reference<R> valueAsReference = null;

    public RegistryOverride(String key,
                            ResourceKey<? extends Registry<? extends R>> registryKey,
                            Function<S, R> getter,
                            BiConsumer<S, R> setter,
                            BiFunction<ResourceKey<R>, R, String> toString,
                            ResourceKey<R> defaultFallback,
                            boolean forcedFallback) {
        super(key, getter, setter, null);

        vanillaLookup = vanillaLookup == null ? VanillaRegistries.createLookup() : vanillaLookup;
        HolderLookup.RegistryLookup<R> lookup = vanillaLookup.lookupOrThrow(registryKey);
        for (Holder.Reference<R> option : lookup.listElements().toList()) {
            if (option.isBound()) {
                this.options.add(option);
                ResourceKey<R> asKey = option.key();
                if (asKey == defaultFallback) {
                    this.defaultFallback = option.value();
                }
            }
        }

        this.toString = toString;
        this.value = getDefaultValue();
        if (this.defaultFallback == null && !forcedFallback) {
            this.defaultFallback = getDefaultValue();
        }
        this.forcedFallback = forcedFallback;
    }

    public static <R> Holder.Reference<R> getHolderValue(ResourceKey<? extends Registry<? extends R>> registryType, R value) {
        vanillaLookup = vanillaLookup == null ? VanillaRegistries.createLookup() : vanillaLookup;
        HolderLookup.RegistryLookup<R> lookup = vanillaLookup.lookupOrThrow(registryType);
        for (Holder.Reference<R> option : lookup.listElements().toList()) {
            if (option.isBound() && option.value() == value) {
                return option;
            }
        }

        throw new RuntimeException();
    }

    public static <R> Holder.Reference<R> getHolderValue(ResourceKey<? extends Registry<? extends R>> registryType, ResourceKey<R> value) {
        vanillaLookup = vanillaLookup == null ? VanillaRegistries.createLookup() : vanillaLookup;
        HolderLookup.RegistryLookup<R> lookup = vanillaLookup.lookupOrThrow(registryType);
        for (Holder.Reference<R> option : lookup.listElements().toList()) {
            if (option.isBound() && option.key() == value) {
                return option;
            }
        }

        throw new RuntimeException();
    }

    public static <R> List<Holder.Reference<R>> getHolderValues(ResourceKey<? extends Registry<? extends R>> registryType) {
        vanillaLookup = vanillaLookup == null ? VanillaRegistries.createLookup() : vanillaLookup;
        HolderLookup.RegistryLookup<R> lookup = vanillaLookup.lookupOrThrow(registryType);
        return lookup.listElements().toList();
    }

    @Override
    public UIComponent buildComponent() {
        SearchableEntityListComponent.LeftAlignedCheckbox checkbox = new SearchableEntityListComponent.LeftAlignedCheckbox(
                Component.literal(toDisplayName(key)),
                Sizing.fill(50),
                () -> this.enabled,
                pressed -> this.enabled = pressed
        );

        FullWidthCollapsibleContainer layout = new FullWidthCollapsibleContainer(
                checkbox,
                () -> valueAsReference == null ? Translate.gui("not_set") : Component.literal(this.toString.apply(valueAsReference.key(), valueAsReference.value())),
                false
        );

        for (Holder.Reference<R> enumOption : this.options) {
            layout.child(addOption(enumOption));
        }

        return layout;
    }

    @Override
    public void apply(S renderState) {
        if (this.enabled || forcedFallback) {
            this.setter.accept(renderState, enabled && value != null ? value : defaultFallback);
        }
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {

    }

    private void setValue(Holder.Reference<R> registryOption) {
        if (registryOption == null) {
            super.setValue(null);
            valueAsReference = null;
        } else {
            super.setValue(registryOption.value());
            valueAsReference = registryOption;
        }
    }

    private SearchableEntityListComponent.DynamicTextButton addOption(@Nullable Holder.Reference<R> registryOption) {
        return new SearchableEntityListComponent.DynamicTextButton(() -> {
            MutableComponent component = registryOption == null ? Translate.gui("not_set") : Component.literal(this.toString.apply(registryOption.key(), registryOption.value()));

            boolean isOption = getValue() == registryOption;
            ChatFormatting color = this.enabled ? (isOption ? ChatFormatting.GREEN : ChatFormatting.WHITE) : ChatFormatting.DARK_GRAY;

            return component.withStyle(color);
        }, ignored -> setValue(registryOption));
    }

    @Override
    public R getDefaultValue() {
        return options.isEmpty() ? null : options.getFirst().value();
    }
}

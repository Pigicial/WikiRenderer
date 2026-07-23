package com.pigicial.wikirenderer.render.entity.options.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.components.FullWidthCollapsibleContainer;
import com.pigicial.wikirenderer.screen.components.MiniEditBoxComponent;
import com.pigicial.wikirenderer.screen.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.UIContainers;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;
import com.pigicial.wikirenderer.screen.owo.core.VerticalAlignment;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BlockStateOverride<S extends EntityRenderState> extends OptionalOverride<S, BlockState> {

    private static HolderLookup.Provider vanillaLookup;

    private String blockName = "";
    private FlowLayout propertiesLayout;

    public BlockStateOverride(String key, Function<S, BlockState> getter, BiConsumer<S, BlockState> setter) {
        super(key, getter, setter);
    }

    @Override
    public UIComponent buildComponent() {
        SearchableEntityListComponent.LeftAlignedCheckbox checkbox = new SearchableEntityListComponent.LeftAlignedCheckbox(
                Component.literal(toDisplayName(key)),
                Sizing.fill(50),
                () -> this.enabled, pressed -> this.enabled = pressed
        );

        FullWidthCollapsibleContainer layout = new FullWidthCollapsibleContainer(checkbox, () -> {
            BlockState state = getValue();
            return state == null || state.isAir() ? Translate.gui("not_set") : state.getBlock().getName();
        }, false);
        layout.margins(Insets.of(0, 0, 0, 0));

        FlowLayout blockInputRow = UIContainers.horizontalFlow(Sizing.fixed(100), Sizing.content());
        blockInputRow.verticalAlignment(VerticalAlignment.CENTER);
        blockInputRow.margins(Insets.top(5));

        blockInputRow.child(WikiRendererUI.label(Translate.gui("block")).margins(Insets.right(5)));
        blockInputRow.child(this.buildBlockNameComponent());
        layout.child(blockInputRow);

        this.propertiesLayout = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        layout.child(this.propertiesLayout);

        this.rebuildPropertyWidgets();
        return layout;
    }

    private MiniEditBoxComponent buildBlockNameComponent() {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.expand(), blockName);
        editBox.onChanged().subscribe(text -> {
            this.blockName = text;
            try {
                vanillaLookup = vanillaLookup == null ? VanillaRegistries.createWorldLookup() : vanillaLookup;
                BlockStateParser.BlockResult result = BlockStateParser.parseForBlock(vanillaLookup.lookupOrThrow(Registries.BLOCK), new StringReader(text), false);
                this.setValue(result.blockState());
            } catch (CommandSyntaxException e) {
                this.setValue(null);
            }
        });
        return editBox;
    }

    private void rebuildPropertyWidgets() {
        if (propertiesLayout == null) return;
        this.propertiesLayout.clearChildren();

        BlockState state = getValue();
        if (state == null || state.isAir()) return;

        Collection<Property<?>> properties = state.getProperties();
        for (Property<?> property : properties) {
            if (property.getName().contains("waterlogged")) continue; // doesnt render
            this.propertiesLayout.child(buildPropertyContainer(property));
        }
    }

    private <T extends Comparable<T>> UIComponent buildPropertyContainer(Property<T> property) {
        UIComponent headerLabel = WikiRendererUI.label(Component.literal(toDisplayName(property.getName())));

        FullWidthCollapsibleContainer propContainer = new FullWidthCollapsibleContainer(headerLabel, () -> {
            BlockState value = getValue();
            if (value == null) return Component.empty();
            T val = value.getValue(property);
            return Component.literal(OptionalOverride.toDisplayName(property.getName(val)));
        }, false);

        for (T possibleValue : property.getPossibleValues()) {
            propContainer.child(new SearchableEntityListComponent.DynamicTextButton(() -> {
                Component text = Component.literal(OptionalOverride.toDisplayName(property.getName(possibleValue)));

                BlockState value = getValue();
                boolean isSelected = value != null && value.getValue(property).equals(possibleValue);
                ChatFormatting color = this.enabled ? (isSelected ? ChatFormatting.GREEN : ChatFormatting.WHITE) : ChatFormatting.DARK_GRAY;

                return text.copy().withStyle(color);
            }, button -> {
                BlockState value = getValue();
                this.setValue(value == null ? null : value.setValue(property, possibleValue));
            }));
        }

        return propContainer;
    }

    @Override
    public void setValue(@Nullable BlockState newState) {
        BlockState oldState = getValue();
        super.setValue(newState);

        if ((oldState != null && newState == null) || (newState != null && (oldState == null || newState.getBlock() != oldState.getBlock()))) {
            this.rebuildPropertyWidgets();
        }
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {}

    @Override
    public BlockState getDefaultValue() {
        return Blocks.AIR.defaultBlockState();
    }
}
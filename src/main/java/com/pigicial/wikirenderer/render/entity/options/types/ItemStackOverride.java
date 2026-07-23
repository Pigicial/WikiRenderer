package com.pigicial.wikirenderer.render.entity.options.types;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.components.FullWidthCollapsibleContainer;
import com.pigicial.wikirenderer.screen.components.MiniEditBoxComponent;
import com.pigicial.wikirenderer.screen.components.SearchableEntityListComponent;
import com.pigicial.wikirenderer.screen.owo.component.ItemComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.UIContainers;
import com.pigicial.wikirenderer.screen.owo.core.*;
import com.pigicial.wikirenderer.textures.PlayerTextureUtils;
import com.pigicial.wikirenderer.textures.TextureData;
import com.pigicial.wikirenderer.util.ItemComponentEncoder;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ItemStackOverride<S extends EntityRenderState> extends OptionalOverride<S, ItemStack> {

    private static final List<Item> DYEABLE_ITEMS = List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.WOLF_ARMOR);

    private FullWidthCollapsibleContainer layout;
    private FlowLayout itemOptionsLayout = null;
    private ItemComponent itemIconComponent = null;
    private String itemName = "";

    private FlowLayout playerHeadOptionsLayout;
    private String playerHeadTextureID = "";

    private FlowLayout dyeColorOptionsLayout;
    private String dyeColor = "";

    public ItemStackOverride(String key, Function<S, ItemStack> getter, BiConsumer<S, ItemStack> setter) {
        super(key, getter, setter);
    }

    @Override
    public UIComponent buildComponent() {
        SearchableEntityListComponent.LeftAlignedCheckbox checkbox = new SearchableEntityListComponent.LeftAlignedCheckbox(
                Component.literal(toDisplayName(key)),
                Sizing.fill(50),
                () -> this.enabled, pressed ->
                this.enabled = pressed
        );

        this.layout = new FullWidthCollapsibleContainer(checkbox, () -> {
            ItemStack item = getValue();
            return item == null || item.isEmpty() ? Translate.gui("not_set") : (item.getItem() == Items.PLAYER_HEAD ? Items.PLAYER_HEAD.getName(new ItemStack(Items.PLAYER_HEAD)).copy() : item.getItemName().copy());
        }, false);
        this.layout.margins(Insets.of(0, 0, 0, 0));

        this.itemOptionsLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        this.itemOptionsLayout.horizontalAlignment(HorizontalAlignment.LEFT);
        this.itemOptionsLayout.verticalAlignment(VerticalAlignment.CENTER);

        this.itemOptionsLayout.child(WikiRendererUI.label(Translate.gui("item")));
        this.itemOptionsLayout.child(this.buildItemNameComponent());

        ItemStack currentItem = getValue();
        if (currentItem != null && !currentItem.isEmpty()) {
            this.itemIconComponent = WikiRendererUI.item(currentItem);
            this.itemOptionsLayout.child(this.itemIconComponent);
        }

        this.layout.child(this.itemOptionsLayout);

        this.playerHeadOptionsLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        this.playerHeadOptionsLayout.horizontalAlignment(HorizontalAlignment.LEFT);
        this.playerHeadOptionsLayout.verticalAlignment(VerticalAlignment.CENTER);
        this.playerHeadOptionsLayout.child(WikiRendererUI.label(Translate.gui("head_texture_id")));
        this.playerHeadOptionsLayout.child(this.buildPlayerHeadTextureComponent());
        this.playerHeadOptionsLayout.id("player_head_layout");

        if (currentItem != null && currentItem.getItem() == Items.PLAYER_HEAD) {
            this.layout.child(this.playerHeadOptionsLayout);
        }

        this.dyeColorOptionsLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        this.dyeColorOptionsLayout.horizontalAlignment(HorizontalAlignment.LEFT);
        this.dyeColorOptionsLayout.verticalAlignment(VerticalAlignment.CENTER);
        this.dyeColorOptionsLayout.child(WikiRendererUI.label(Translate.gui("dye_color")));
        this.dyeColorOptionsLayout.child(this.buildArmorColorComponent());
        this.dyeColorOptionsLayout.id("dye_color_layout");

        if (currentItem != null && DYEABLE_ITEMS.contains(currentItem.getItem())) {
            this.layout.child(this.dyeColorOptionsLayout);
        }

        return layout;
    }

    @Override
    public void copyFromRenderState(S renderState) {
        super.copyFromRenderState(renderState);
        if (this.value == null) {
            this.value = ItemStack.EMPTY;
        }

        // todo: make player heads on the helmet slot copy over properly
        if (!this.value.isEmpty()) {
            if (this.dyeColor == null || this.dyeColor.isBlank()) {
                DyedItemColor color = this.value.get(DataComponents.DYED_COLOR);
                if (color != null) {
                    dyeColor = String.valueOf(color.rgb());
                }
            }

            if (playerHeadTextureID == null || playerHeadTextureID.isBlank()) {
                TextureData textureData = PlayerTextureUtils.getTextureDataFromPlayerHead(this.value);
                if (textureData != null) {
                    MinecraftProfileTexture skin = textureData.payload().textures().get(MinecraftProfileTexture.Type.SKIN);
                    if (skin != null) {
                        this.playerHeadTextureID = skin.getHash();
                    }
                }
            }

            this.itemName = ItemComponentEncoder.toGiveString(this.value);
        }
    }

    private MiniEditBoxComponent buildItemNameComponent() {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.expand(80), itemName);
        editBox.onChanged().subscribe(text -> {
            this.itemName = text;
            try {
                ItemInput result = new ItemParser(HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM))).parse(new StringReader(text));
                ItemStack item = new ItemStack(result.item());
                item.applyComponents(result.components());
                setValue(item);
            } catch (CommandSyntaxException e) {
                setValue(ItemStack.EMPTY);
            }
        });
        return editBox;
    }

    private MiniEditBoxComponent buildPlayerHeadTextureComponent() {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.expand(80), playerHeadTextureID);
        editBox.onChanged().subscribe(text -> {
            this.playerHeadTextureID = text.trim();
            setValue(new ItemStack(Items.PLAYER_HEAD));
        });

        return editBox;
    }

    private MiniEditBoxComponent buildArmorColorComponent() {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.expand(80), dyeColor);
        editBox.onChanged().subscribe(text -> {
            this.dyeColor = text.trim();
            setValue(getValue());
        });
        return editBox;
    }

    @Override
    public void setValue(@Nullable ItemStack newItem) {
        if (newItem != null && newItem.getItem() == Items.PLAYER_HEAD) {
            newItem = PlayerTextureUtils.createPlayerHead(PlayerTextureUtils.createTexturedGameProfileFromID(playerHeadTextureID));
        }

        if (newItem != null && dyeColor != null) {
            try {
                int color = Integer.parseInt(dyeColor);
                newItem.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
            } catch (NumberFormatException ignored) {}
        }

        ItemStack previousItem = getValue();
        super.setValue(newItem);
        if (!Objects.equals(newItem, previousItem)) {
            if (itemIconComponent != null) {
                this.itemOptionsLayout.removeChild(itemIconComponent);
                this.itemIconComponent = null;
            }
            if (newItem != null && !newItem.isEmpty()) {
                this.itemIconComponent = WikiRendererUI.item(newItem);
                this.itemOptionsLayout.child(this.itemIconComponent);
            }

            if (newItem != null && newItem.getItem() == Items.PLAYER_HEAD) {
                if (layout.childById(playerHeadOptionsLayout.getClass(), "player_head_layout") == null) {
                    this.layout.child(this.playerHeadOptionsLayout);
                }
            } else {
                this.layout.removeChild(this.playerHeadOptionsLayout);
            }

            if (newItem != null && DYEABLE_ITEMS.contains(newItem.getItem())) {
                if (layout.childById(dyeColorOptionsLayout.getClass(), "dye_color_layout") == null) {
                    this.layout.child(this.dyeColorOptionsLayout);
                }
            } else {
                this.layout.removeChild(this.dyeColorOptionsLayout);
            }
        }
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {

    }

    @Override
    public ItemStack getDefaultValue() {
        return ItemStack.EMPTY;
    }
}

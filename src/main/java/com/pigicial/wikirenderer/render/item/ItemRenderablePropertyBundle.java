package com.pigicial.wikirenderer.render.item;

import com.mojang.math.Axis;
import com.pigicial.wikirenderer.property.*;
import com.pigicial.wikirenderer.property.config.WikiRendererConfigs;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ImageRescaleMode;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.util.ItemBlockUtil;
import com.pigicial.wikirenderer.util.Translate;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.joml.Matrix4fStack;

import java.util.List;

public class ItemRenderablePropertyBundle extends DefaultCroppablePropertyBundle implements SerializablePropertyBundle {

    private static final List<Item> DYEABLE_ITEMS = List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.WOLF_ARMOR);

    public static final ItemRenderablePropertyBundle INSTANCE = WikiRendererConfigs.loadOrDefault(new ItemRenderablePropertyBundle());

    public final Property<Boolean> allowScalingWithMouse = Property.of(false);
    public final Property<Boolean> forceEnchantmentGlints = Property.of(false);
    public final Property<Boolean> overrideDyeColors = Property.of(false);
    public int dyeColorOverride = 0;

    public final Property<Boolean> useModelOverrides = Property.of(false);
    protected int blockItemsExportResolution = 300;

    @Override
    public String getConfigFileName() {
        return "item_render_settings";
    }

    @Override
    protected int getDefaultExportResolution() {
        return 160;
    }

    @Override
    public void setExportResolution(Renderable<?> renderable, int exportResolution) {
        if (ItemBlockUtil.doesItemUseBlockLight(((ItemRenderable) renderable).stack)) {
            blockItemsExportResolution = exportResolution;
        } else {
            super.setExportResolution(renderable, exportResolution);
        }
    }

    @Override
    public int getExportResolution(Renderable<?> renderable) {
        if (ItemBlockUtil.doesItemUseBlockLight(((ItemRenderable) renderable).stack)) {
            return blockItemsExportResolution;
        } else {
            return super.getExportResolution(renderable);
        }
    }

    @Override
    protected boolean shouldCropByDefault() {
        return false;
    }

    @Override
    protected int getDefaultRotation() {
        return 0;
    }

    @Override
    protected double getDefaultSlant() {
        return 0;
    }

    @Override
    public void modifyScale(double amount) {
        if (allowScalingWithMouse.get() || (crop.get() && rescaleMode.get() != ImageRescaleMode.DISABLED)) {
            super.modifyScale(amount);
        }
    }

    @Override
    public void applyToViewMatrix(Renderable<?> renderable, Matrix4fStack modelViewStack) {
        float scale = (this.scale.get() / 100f) * 2f;
        modelViewStack.scale(scale, scale, scale);

        modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / -26000f, 0);

        modelViewStack.rotate(Axis.XP.rotationDegrees(this.slant.get().floatValue()));
        modelViewStack.rotate(Axis.YP.rotationDegrees(this.rotation.get() + this.updateAndGetSpinningRotationOffset()));
    }

    @Override
    public void buildMainGUIControls(Renderable<?> r, RenderScreen screen, FlowLayout container) {
        ItemRenderable renderable = ((ItemRenderable) r);
        WikiRendererUI.text(container, "transform_options", false);
        WikiRendererUI.intControl(screen, container, scale, "scale");
        if (!crop.get() || rescaleMode.get() == ImageRescaleMode.DISABLED) {
            WikiRendererUI.booleanControl(container, this.allowScalingWithMouse, "allow_scaling_with_mouse");
        }

        WikiRendererUI.text(container, "item_scale_warning_1", 10);
        WikiRendererUI.text(container, "item_scale_warning_2", false);
        WikiRendererUI.intControl(screen, container, rotation, "rotation");
        WikiRendererUI.doubleControl(screen, container, slant, "slant");
        WikiRendererUI.intControl(screen, container, rotationSpeed, "rotation_speed");
        WikiRendererUI.conditionalBooleanControl(container, GlobalProperties.get().syncRotationToAnimation, "sync_rotation_to_animation", () -> !rotationSpeed.isDefault());
        WikiRendererUI.booleanControl(container, this.allowRotatingWithMouse, "allow_rotating_with_mouse");
        container.child(this.buildResetButton());

        // todo figure out a better way to check for glint support
        ItemStack stack = renderable.stack;
        if (!stack.is(Items.PLAYER_HEAD) || DYEABLE_ITEMS.contains(stack.getItem())) {
            WikiRendererUI.text(container, "item_options", true);
        }

        if (!stack.is(Items.PLAYER_HEAD)) {
            WikiRendererUI.booleanControl(container, forceEnchantmentGlints, "force_enchanted");
        }

        if (DYEABLE_ITEMS.contains(stack.getItem())) {
            WikiRendererUI.booleanControl(container, overrideDyeColors, "override_dye_color");
            overrideDyeColors.addRebuildListener(screen);
            if (overrideDyeColors.get()) {
                EditBox colorField = WikiRendererUI.labelledTextField(container, "#000000", "dye_color", Sizing.fixed(50));
                colorField.setFilter(s -> s.matches("^#([A-Fa-f\\d]{0,6})$"));
                colorField.setValue(String.format("#%06x", dyeColorOverride & 0xFFFFFF));
                colorField.moveCursorToStart(false);
                colorField.setResponder(s -> {
                    String text = s.startsWith("#") ? s.substring(1) : s;
                    if (text.length() < 6) {
                        return;
                    }

                    dyeColorOverride = Integer.parseInt(s.substring(1), 16) | 0xFF000000;
                });
            }

            DyedItemColor dyedItemColor = stack.get(DataComponents.DYED_COLOR);
            if (dyedItemColor != null) {
                int rgb = dyedItemColor.rgb();
                MutableComponent hexText = Component.literal(String.format("#%06x", rgb & 0xFFFFFF)).withStyle(ChatFormatting.GRAY);
                MutableComponent rgbText = Component.literal(String.valueOf(rgb)).withStyle(ChatFormatting.GRAY);

                WikiRendererUI.text(container, Translate.gui(overrideDyeColors.get() ? "original_dye_color" : "item_dye_color", hexText, rgbText), 10);
            }
        }

        IntProperty modelIndexOption = renderable.getCurrentModelIndex();
        if (modelIndexOption != null) {
            WikiRendererUI.text(container, "item_model_options", true);
            WikiRendererUI.text(container, Translate.gui("model_combinations_detected", modelIndexOption.max()), 5);
            WikiRendererUI.booleanControl(container, this.useModelOverrides, "use_model_overrides");
            WikiRendererUI.conditionalIntControl(screen, container, modelIndexOption, "use_model_overrides", useModelOverrides::get);
        }
    }
}

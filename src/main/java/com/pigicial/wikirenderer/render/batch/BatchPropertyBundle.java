package com.pigicial.wikirenderer.render.batch;

import com.pigicial.wikirenderer.property.*;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.item.ItemRenderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.textures.TextureDataProvider;
import com.pigicial.wikirenderer.util.ItemBlockUtil;
import com.pigicial.wikirenderer.util.Translate;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4fStack;

public class BatchPropertyBundle extends DefaultCroppablePropertyBundle {

    private static final IntProperty ITEM_RESOLUTION_PROPERTY = IntProperty.of(160, 1, Short.MAX_VALUE / 2);
    private static final IntProperty BLOCK_ITEM_RESOLUTION_PROPERTY = IntProperty.of(300, 1, Short.MAX_VALUE / 2);
    public static final Property<Boolean> EXPORT_AS_ANIMATIONS = Property.of(false);
    public static final Property<Boolean> ONLY_PLAYER_HEADS = Property.of(false);
    public static String fileNameFormatter = "%name%";

    private final BatchRenderable<?> batchRenderable;
    private final PropertyBundle actualProperties;

    public BatchPropertyBundle(BatchRenderable<?> batchRenderable, PropertyBundle actualProperties) {
        this.batchRenderable = batchRenderable;
        this.actualProperties = actualProperties;

        // sync up properties to make things way easier to work with
        if (this.actualProperties instanceof DefaultPropertyBundle clonedFrom) {
            this.scale = clonedFrom.scale;
            this.rotation = clonedFrom.rotation;
            this.slant = clonedFrom.slant;
            this.xOffset = clonedFrom.xOffset;
            this.yOffset = clonedFrom.yOffset;
            this.rotationSpeed = clonedFrom.rotationSpeed;
            this.allowRotatingWithMouse = clonedFrom.allowRotatingWithMouse;
        }

        if (this.actualProperties instanceof DefaultCroppablePropertyBundle clonedFrom) {
            this.crop = clonedFrom.getCropProperty();
            this.ffmpegCrop = clonedFrom.getFFmpegCropProperty();
            this.rescaleMode = clonedFrom.getRescaleMode();
        }
    }

    @Override
    public void modifySlant(double amount) {
        if (this.actualProperties instanceof DefaultPropertyBundle defaultPropertyBundle) {
            defaultPropertyBundle.modifySlant(amount);
        } else {
            super.modifySlant(amount);
        }
    }

    @Override
    public void modifyRotation(int amount) {
        if (this.actualProperties instanceof DefaultPropertyBundle delegate) {
            delegate.modifyRotation(amount);
        } else {
            super.modifySlant(amount);
        }
    }

    @Override
    public void modifyScale(double amount) {
        if (this.actualProperties instanceof DefaultPropertyBundle delegate) {
            delegate.modifyScale(amount);
        } else {
            super.modifyScale(amount);
        }
    }

    public PropertyBundle getActualProperties() {
        return actualProperties;
    }

    @Override
    public void onRenderStart() {
        this.actualProperties.onRenderStart();
    }

    @Override
    public void applyToViewMatrix(Renderable<?> ignored, Matrix4fStack modelViewStack) {
        this.actualProperties.applyToViewMatrix(this.batchRenderable.currentDelegate, modelViewStack);
    }

    @Override
    public void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        BatchRenderable<?> batchRenderable = (BatchRenderable<?>) renderable;

        WikiRendererUI.text(container, "batch.controls", false);
        WikiRendererUI.booleanControl(container, EXPORT_AS_ANIMATIONS, "batch.export_as_animations");

        try (WikiRendererUI.RowBuilder builder = WikiRendererUI.autoNewLineRow(container)) {
            ButtonComponent startButton = UIComponents.button(Translate.gui("batch.start"), button -> {
                batchRenderable.start();
                button.active = false;
            });
            builder.row.child(startButton.horizontalSizing(Sizing.content(20)));
            builder.row.child(UIComponents.button(Translate.gui("batch.reset"), _ -> {
                batchRenderable.reset(screen);
                startButton.active = true;
            }));
            builder.row.child(UIComponents.button(Translate.gui("batch.previous"), _ -> {
                batchRenderable.decreaseIndex();
                if (batchRenderable.currentDelegate instanceof TextureDataProvider) {
                    screen.guiRebuildScheduled = true;
                }
            }));
            builder.row.child(UIComponents.button(Translate.gui("batch.next"), _ -> {
                batchRenderable.increaseIndex();
                if (batchRenderable.currentDelegate instanceof TextureDataProvider) {
                    screen.guiRebuildScheduled = true;
                }
            }));
        }

        WikiRendererUI.dynamicText(container, () -> Translate.gui(
                "batch.amount",
                batchRenderable.currentIndex + 1,
                batchRenderable.delegates.size(),
                Math.max(0, batchRenderable.delegates.size() - batchRenderable.currentIndex - 1)
                )).margins(Insets.of(4, 13, 0, 0));

        this.actualProperties.buildMainGUIControls(batchRenderable.currentDelegate, screen, container);
    }

    @Override
    public void buildRenderOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        this.actualProperties.buildRenderOptionGUIControls(this.batchRenderable.currentDelegate, screen, container);
    }

    @Override
    public int getExportResolution(Renderable<?> ignored) {
        if (this.batchRenderable.currentDelegate instanceof ItemRenderable itemRenderable) {
            if (ItemBlockUtil.doesItemUseBlockLight(itemRenderable.stack)) {
                return BLOCK_ITEM_RESOLUTION_PROPERTY.get();
            } else {
                return ITEM_RESOLUTION_PROPERTY.get();
            }
        }

        return this.actualProperties.getExportResolution(this.batchRenderable.currentDelegate); // this.batchRenderable is not needed here really
    }

    @Override
    public void buildExportResolutionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        if (this.batchRenderable.currentDelegate instanceof ItemRenderable) {
            WikiRendererUI.labelledTextField(screen, container, ITEM_RESOLUTION_PROPERTY, "item_resolution", Sizing.fixed(50));
            WikiRendererUI.labelledTextField(screen, container, BLOCK_ITEM_RESOLUTION_PROPERTY, "block_item_resolution", Sizing.fixed(50));
        } else {
            this.actualProperties.buildExportResolutionGUIControls(this.batchRenderable.currentDelegate, screen, container);
        }
    }

    @Override
    public void buildFileNameGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        BatchRenderable<?> batchRenderable = (BatchRenderable<?>) renderable;
        if (!batchRenderable.delegates.isEmpty() && this.batchRenderable.currentDelegate instanceof DynamicBatchLabelProvider labelProvider) {
            screen.fileNameField = WikiRendererUI.labelledTextField(container, fileNameFormatter, "batch.file_name_preset", Sizing.fixed(120));
            screen.fileNameField.setFilter(s -> s.matches("^[^<>:\"|?*\\\\\\x00-\\x1F]*$")); // file name regex
            screen.fileNameField.onChanged().subscribe(s -> renderable.setCustomFileName(s.trim()));

            WikiRendererUI.text(container, "batch.label_presets", 10);
            for (String exampleKey : labelProvider.buildPresetExamples()) {
                WikiRendererUI.text(container, exampleKey, false);
            }

            WikiRendererUI.text(container, "batch.name_previews", 10);
            int delegatesAmount = batchRenderable.delegates.size();
            for (int i = 0; i < Math.min(delegatesAmount, 3); i++) {
                int index = i;
                WikiRendererUI.dynamicText(container, () -> {
                    int newIndex = (index + Math.max(batchRenderable.currentIndex, 0)) % delegatesAmount;
                    return Component.literal("- " + ((DynamicBatchLabelProvider) batchRenderable.delegates.get(newIndex)).buildFileName(fileNameFormatter));
                });
            }
        } else {
            this.actualProperties.buildFileNameGUIControls(this.batchRenderable.currentDelegate, screen, container);
        }
    }

    @Override
    public void buildExportOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        if (this.actualProperties instanceof CroppablePropertyBundle croppablePropertyBundle) {
            croppablePropertyBundle.buildExportOptionGUIControls(renderable, screen, container);
        } else {
            super.buildRegularExportOptions(renderable, screen, container);
        }
    }
}

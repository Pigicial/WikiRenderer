package com.pigicial.wikirenderer.property;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ImageRescaleMode;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.util.Translate;

public interface CroppablePropertyBundle extends PropertyBundle {

    Property<Boolean> getCropProperty();

    Property<Boolean> getFFmpegCropProperty();

    Property<ImageRescaleMode> getRescaleMode();

    default boolean allowForRescaling() {
        return true;
    }

    @Override
    default void buildExportOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        Property<Boolean> cropProperty = this.getCropProperty();
        Property<ImageRescaleMode> resizeModeProperty = this.getRescaleMode();

        boolean allowForRescaling = this.allowForRescaling();
        WikiRendererUI.booleanControl(container, cropProperty, allowForRescaling ? "crop_and_rescale_" + resizeModeProperty.get().name().toLowerCase() : "crop");
        cropProperty.addRebuildListener(screen);

        if (cropProperty.get() && allowForRescaling) {
            container.child(WikiRendererUI.dropdown(Sizing.content())
                    .button(Translate.gui("rescale_longer_side"), _ -> {
                        resizeModeProperty.set(ImageRescaleMode.LONGER_SIDE);
                        screen.guiRebuildScheduled = true;
                    })
                    .button(Translate.gui("rescale_shorter_side"), _ -> {
                        resizeModeProperty.set(ImageRescaleMode.SHORTER_SIDE);
                        screen.guiRebuildScheduled = true;
                    })
                    .button(Translate.gui("rescale_vertically"), _ -> {
                        resizeModeProperty.set(ImageRescaleMode.VERTICAL);
                        screen.guiRebuildScheduled = true;
                    })
                    .button(Translate.gui("rescale_horizontally"), _ -> {
                        resizeModeProperty.set(ImageRescaleMode.HORIZONTAL);
                        screen.guiRebuildScheduled = true;
                    })
                    .button(Translate.gui("dont_rescale"), _ -> {
                        resizeModeProperty.set(ImageRescaleMode.DISABLED);
                        screen.guiRebuildScheduled = true;
                    })
                    .closeWhenNotHovered(false)
                    .padding(Insets.of(5))
            );
        }

        PropertyBundle.super.buildExportOptionGUIControls(renderable, screen, container);
    }

    default void buildRegularExportOptions(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        PropertyBundle.super.buildExportOptionGUIControls(renderable, screen, container);
    }

    @Override
    default void buildExportResolutionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        String key = "renderer_resolution";
        if (allowForRescaling() && this.getCropProperty().get()) {
            key = "renderer_resolution_rescale_" + this.getRescaleMode().get().name().toLowerCase();
        }

        TextBoxComponent resolutionField = WikiRendererUI.labelledTextField(container, String.valueOf(renderable.getExportResolution()), key, Sizing.fixed(50));
        resolutionField.setFilter(s -> s.matches("\\d{0,5}"));
        resolutionField.onChanged().subscribe(s -> {
            if (s.isBlank()) return;
            int resolution = Integer.parseInt(s);

            if ((resolution < 16 || resolution >= RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSize()) && !GlobalProperties.get().unsafe.get()) {
                screen.exportButton.active = false;
            } else {
                renderable.getProperties().setExportResolution(renderable, resolution);
                screen.exportButton.active = true;
            }
        });
    }
}

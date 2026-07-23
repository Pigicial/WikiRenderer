package com.pigicial.wikirenderer.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pigicial.wikirenderer.property.*;
import com.pigicial.wikirenderer.property.config.WikiRendererConfigs;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import org.joml.Matrix4fStack;

public class TooltipPropertyBundle extends DefaultCroppablePropertyBundle implements SerializablePropertyBundle {
    public static final TooltipPropertyBundle INSTANCE = WikiRendererConfigs.loadOrDefault(new TooltipPropertyBundle());

    private final IntProperty fontScaling = IntProperty.of(4, 1, 128);
    public final Property<Boolean> hideBackground = IntProperty.of(false);

    @Override
    public boolean allowForRescaling() {
        return false;
    }

    @Override
    public String getConfigFileName() {
        return "tooltip_render_settings";
    }

    @Override
    public void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        WikiRendererUI.text(container, "tooltip_options", false);
        WikiRendererUI.booleanControl(container, hideBackground, "hide_tooltip_background");
    }

    @Override
    public void buildExportResolutionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        TextBoxComponent resolutionField = WikiRendererUI.labelledTextField(container, String.valueOf(this.fontScaling.get()), "font_resolution", Sizing.fixed(28));
        resolutionField.setFilter(s -> s.matches("\\d{0,3}"));
        resolutionField.onChanged().subscribe(s -> {
            if (s.isBlank()) return;
            int resolution = Integer.parseInt(s);

            int tooltipSize = ((TooltipRenderable) renderable).getTooltipSize();
            int bufferSizeWithThisResolution = tooltipSize * resolution;

            if ((resolution < 1 || bufferSizeWithThisResolution >= RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSize()) && !GlobalProperties.get().unsafe.get()) {
                screen.exportButton.active = false;
            } else {
                this.fontScaling.set(resolution);
                screen.exportButton.active = true;
            }
        });
    }

    @Override
    public void applyToViewMatrix(Renderable<?> r, Matrix4fStack modelViewStack) {
        TooltipRenderable renderable = (TooltipRenderable) r;

        // same logic as area overhead rendering
        double imagePixelsPerFontPixel = this.fontScaling.get();
        int tooltipSize = renderable.getTooltipSize();
        double bufferSize = tooltipSize * imagePixelsPerFontPixel;

        this.setExportResolution(renderable, (int) bufferSize);

        double pixelPerfectScale = 2.0 / (double) tooltipSize;
        modelViewStack.scale((float) pixelPerfectScale, (float) pixelPerfectScale, (float) pixelPerfectScale);

        if (tooltipSize % 2 != 0) {
            // without this the image is really blurry
            modelViewStack.translate(-0.5f, -0.5f, 0);
        }
    }
}

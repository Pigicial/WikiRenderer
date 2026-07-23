package com.pigicial.wikirenderer.render.item;

import com.mojang.math.Axis;
import com.pigicial.wikirenderer.property.DefaultCroppablePropertyBundle;
import com.pigicial.wikirenderer.property.DoubleProperty;
import com.pigicial.wikirenderer.property.IntProperty;
import com.pigicial.wikirenderer.property.SerializablePropertyBundle;
import com.pigicial.wikirenderer.property.config.WikiRendererConfigs;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import org.joml.Matrix4fStack;

public class ItemAtlasPropertyBundle extends DefaultCroppablePropertyBundle implements SerializablePropertyBundle {

    public static final ItemAtlasPropertyBundle INSTANCE = WikiRendererConfigs.loadOrDefault(new ItemAtlasPropertyBundle());

    protected final IntProperty columns = IntProperty.of(20, 1, 500);
    protected final DoubleProperty spacing = DoubleProperty.of(1,  0.0, 10.0);

    @Override
    public String getConfigFileName() {
        return "item_atlas_render_settings";
    }

    @Override
    public void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        WikiRendererUI.text(container, "transform_options", false);

        WikiRendererUI.intControl(screen, container, this.scale, "scale");
        WikiRendererUI.intControl(screen, container, this.columns, "columns");
        WikiRendererUI.doubleControl(screen, container, this.spacing, "spacing");
        container.child(this.buildResetButton(() -> {
            this.columns.setToDefault();
            this.spacing.setToDefault();
        }));
    }

    @Override
    protected double getDefaultSlant() {
        return 0;
    }

    @Override
    protected int getDefaultRotation() {
        return 0;
    }

    @Override
    public double getUsedSlant() {
        return 0;
    }

    @Override
    public float getUsedRotation() {
        return 0;
    }

    @Override
    public void applyToViewMatrix(Renderable<?> renderable, Matrix4fStack modelViewStack) {
        float scale = this.scale.get() / 100f;
        modelViewStack.scale(scale, scale, scale);
        modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / 26000f, 0);
        modelViewStack.rotate(Axis.XP.rotationDegrees(180));
    }
}

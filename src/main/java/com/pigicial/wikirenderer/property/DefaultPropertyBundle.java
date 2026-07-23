package com.pigicial.wikirenderer.property;

import com.mojang.math.Axis;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.animation.AnimationHandler;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.component.ButtonComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4fStack;

public class DefaultPropertyBundle implements PropertyBundle {

    public IntProperty scale = IntProperty.of(100, 0, 1000);
    public IntProperty rotation = IntProperty.of(this.getDefaultRotation(), 0, 360).withRollover();
    public DoubleProperty slant = DoubleProperty.of(this.getDefaultSlant(), -90, 90);

    public IntProperty xOffset = IntProperty.of(0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2);
    public IntProperty yOffset = IntProperty.of(0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2);

    public IntProperty rotationSpeed = IntProperty.of(0, -720, 720);
    public Property<Boolean> allowRotatingWithMouse = Property.of(this.allowRotatingWithMouseByDefault());

    public transient float rotationOffset = 0;
    public transient boolean rotationOffsetUpdated = false;

    private int exportResolution = this.getDefaultExportResolution();

    @Override
    public void onRenderStart() {
        this.rotationOffsetUpdated = false;
    }

    protected int getDefaultRotation() {
        return 135;
    }

    protected double getDefaultSlant() {
        return 30;
    }

    protected int getDefaultExportResolution() {
        return 1000;
    }

    protected boolean allowRotatingWithMouseByDefault() {
        return false;
    }

    @Override
    public int getExportResolution(Renderable<?> renderable) {
        return exportResolution;
    }

    @Override
    public void setExportResolution(Renderable<?> renderable, int exportResolution) {
        this.exportResolution = exportResolution;
    }

    public float getUsedRotation() {
        return this.rotation.get() + rotationOffset;
    }

    public double getUsedSlant() {
        return this.slant.get();
    }

    public double getUsedScale() {
        return this.scale.get();
    }

    public void modifyRotation(int amount) {
        if (this.allowRotatingWithMouse.get()) {
            this.rotation.modify(amount);
        }
    }

    public void modifyScale(double amount) {
        this.scale.modify(amount);
    }

    public void modifySlant(double amount) {
        if (this.allowRotatingWithMouse.get()) {
            this.slant.modify(amount);
        }
    }

    @Override
    public void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        WikiRendererUI.text(container, "transform_options", false);
        WikiRendererUI.intControl(screen, container, scale, "scale");
        WikiRendererUI.intControl(screen, container, rotation, "rotation");
        WikiRendererUI.doubleControl(screen, container, slant, "slant");
        WikiRendererUI.intControl(screen, container, rotationSpeed, "rotation_speed");
        WikiRendererUI.conditionalBooleanControl(container, GlobalProperties.get().syncRotationToAnimation, "sync_rotation_to_animation", () -> !rotationSpeed.isDefault());
        WikiRendererUI.booleanControl(container, allowRotatingWithMouse, "allow_rotating_with_mouse");

        try (WikiRendererUI.RowBuilder builder = WikiRendererUI.autoNewLineRow(container)) {
            builder.row.child(WikiRendererUI.button(Translate.gui("dimetric_recommended"), (ButtonComponent button) -> {
                this.rotation.setToDefault();
                this.slant.set(30D);
            }).margins(Insets.right(5)));

            builder.row.child(WikiRendererUI.button(Translate.gui("isometric"), (ButtonComponent button) -> {
                this.rotation.setToDefault();
                this.slant.set(35.264);
            }));
        }

        container.child(this.buildResetButton());
    }

    protected ButtonComponent buildResetButton() {
        return this.buildResetButton(() -> {});
    }

    protected ButtonComponent buildResetButton(Runnable runnable) {
        return WikiRendererUI.button(Translate.gui("reset_transformations"), _ -> {
            this.xOffset.setToDefault();
            this.yOffset.setToDefault();
            this.scale.setToDefault();
            this.rotation.setToDefault();
            this.slant.setToDefault();
            this.rotationSpeed.setToDefault();
            runnable.run();
        });
    }

    @Override
    public void applyToViewMatrix(Renderable<?> renderable, Matrix4fStack modelViewStack) {
        float scale = this.scale.get() / 100f;
        modelViewStack.scale(scale, scale, scale);

        modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / 26000f, 0);

        modelViewStack.rotate(Axis.XP.rotationDegrees(180 + this.slant.get().floatValue()));
        modelViewStack.rotate(Axis.YP.rotationDegrees(this.rotation.get() + this.updateAndGetSpinningRotationOffset()));
    }

    public float updateAndGetSpinningRotationOffset() {
        if (rotationSpeed.get() == 0) {
            this.rotationOffset = 0;
            return 0;
        }

        if (!this.rotationOffsetUpdated) {
            GlobalProperties globalProperties = GlobalProperties.get();
            if (!globalProperties.syncRotationToAnimation.get()) {
                this.rotationOffset += Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() * this.rotationSpeed.get() * .05f;
                this.rotationOffsetUpdated = true;
            } else {
                AnimationHandler animationHandler = WikiRenderer.currentAnimationHandler;
                if (animationHandler != null && !animationHandler.isFinished()) {
                    int totalFrameCount = animationHandler.getAnimationFrames();
                    int framesRenderedSoFar = totalFrameCount - animationHandler.getRemainingFrames();

                    int frameRate = globalProperties.exportFramerate.get();
                    double secondsIntoAnimation = (double) framesRenderedSoFar / (double) frameRate;
                    this.rotationOffset = (float) (secondsIntoAnimation * rotationSpeed.get());
                } else {
                    this.rotationOffset = 0;
                }
            }
        }

        return rotationOffset;
    }
}

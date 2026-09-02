package com.pigicial.wikirenderer.property;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.export.ExportPathSpec;
import com.pigicial.wikirenderer.render.export.RenderableDispatcher;
import com.pigicial.wikirenderer.render.particle.ParticleRendererAndLooper;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.util.ClipboardUtil;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;

import java.io.File;

public interface PropertyBundle {

    void buildMainGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container);

    default void onRenderStart() {

    }

    default void buildRenderOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {

    }

    default void buildLoopParticlesOption(FlowLayout container) {
        GlobalProperties globalProperties = GlobalProperties.get();

        WikiRendererUI.conditionalBooleanControl(container, globalProperties.loopParticles, "loop_particles",
                () -> globalProperties.tickParticles.get() && globalProperties.setAnimationFpsCap.get() && globalProperties.exportFramerate.get() == 20);

        WikiRendererUI.dynamicConditionalText(container, () -> globalProperties.tickParticles.get() && globalProperties.loopParticles.get() && globalProperties.setAnimationFpsCap.get() && globalProperties.exportFramerate.get() == 20, () -> {
            int existingTotal = ParticleRendererAndLooper.getAtLeastPartiallySavedParticleCount();
            int fullySavedTotal = ParticleRendererAndLooper.getFullySavedParticleCount();
            if (existingTotal == fullySavedTotal) {
                return Translate.gui("loop_particles_ready").withStyle(ChatFormatting.GREEN);
            } else {
                int percentage = (int) (100D * (fullySavedTotal / (double) existingTotal));
                return Translate.gui("loop_particles_not_ready", percentage + "%").withStyle(ChatFormatting.RED);
            }
        });
    }

    default void buildExportOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        GlobalProperties globalProperties = GlobalProperties.get();
        WikiRendererUI.booleanControl(container, globalProperties.saveIntoRoot, "dump_into_root");
        WikiRendererUI.booleanControl(container, globalProperties.overwriteLatest, "overwrite_latest");

        try (WikiRendererUI.RowBuilder builder = WikiRendererUI.autoNewLineRow(container)) {
            screen.exportButton = WikiRendererUI.button(Translate.gui("export"), _ -> screen.captureScheduled = true);
            builder.row.child(screen.exportButton);

            builder.row.child(WikiRendererUI.button(Translate.gui("open_folder"), _ -> {
                ExportPathSpec defaultExportPath = renderable.getExportPath();
                ExportPathSpec exportPath = defaultExportPath.differentFileName(renderable.getCustomFileName());
                File file = exportPath.resolveOffset().toFile();
                if (file.mkdirs()) {
                    WikiRenderer.LOGGER.info("Made possible export directory (open file button pressed) {}", file);
                }
                Util.getPlatform().openFile(file);
            }));

            builder.row.child(WikiRendererUI.button(Translate.gui("export_to_clipboard"), _ -> {
                screen.notify(Translate.gui("copied_to_clipboard"));

                float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                RenderableDispatcher.drawIntoImage(screen, renderable, tickDelta, screen.getTimeSinceCreationMs(), renderable.getExportResolution(), renderable.shouldCrop(), null)
                        .whenComplete((image, _) -> {
                            try (image) {
                                ClipboardUtil.setClipboard(image);
                            } catch (Exception e) {
                                WikiRenderer.LOGGER.error("mfw", e);
                            }
                        });
            }));
        }
    }

    default void buildExportResolutionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        GlobalProperties globalProperties = GlobalProperties.get();

        TextBoxComponent resolutionField = WikiRendererUI.labelledTextField(container, String.valueOf(renderable.getExportResolution()), "renderer_resolution", Sizing.fixed(50));
        resolutionField.setFilter(s -> s.matches("\\d{0,5}"));
        resolutionField.onChanged().subscribe(s -> {
            if (s.isBlank()) return;
            int resolution = Integer.parseInt(s);

            if ((resolution < 16 || resolution >= RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSize()) && !globalProperties.unsafe.get()) {
                screen.exportButton.active = false;
            } else {
                renderable.getProperties().setExportResolution(renderable, resolution);
                screen.exportButton.active = true;
            }
        });
    }

    default void buildFileNameGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        screen.fileNameField = WikiRendererUI.labelledTextField(container, renderable.getCustomFileName(), "file_name", Sizing.expand(90));
        screen.fileNameField.setFilter(s -> s.matches("^[^<>:\"|?*\\\\\\x00-\\x1F]*$")); // file name regex
        screen.fileNameField.onChanged().subscribe(s -> renderable.setCustomFileName(s.trim()));
    }

    void applyToViewMatrix(Renderable<?> renderable, Matrix4fStack modelViewStack);

    int getExportResolution(Renderable<?> renderable);

    void setExportResolution(Renderable<?> renderable, int resolution);
}

package com.pigicial.wikirenderer.command.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.render.skyblock.frame_based.SkyBlockTimingDataCacher;
import com.pigicial.wikirenderer.util.Translate;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SkyBlockSubCommand extends WikiRendererSubCommand {
    @Override
    public String getName() {
        return "sb";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> source, CommandBuildContext access) {
        return source
                .then(literal("cosmetics")
                    .then(literal("keybind_overrides")
                            .then(literal("enable")
                                    .executes(context -> {
                                        this.enableKeybindOverrides(context);
                                        return 0;
                                    }))
                            .then(literal("disable")
                                    .executes(context -> {
                                        this.disableKeybindOverrides(context);
                                        return 0;
                                    })))
                    .then(literal("reset").executes(context -> {
                        this.resetData(context);
                        return 0;
                    })))
                .then(literal("export_items_with_texture_file")
                        .then(literal("enable").executes(context -> {
                            enableTextureFileExports(context);
                            return 0;
                        }))
                        .then(literal("disable").executes(context -> {
                            disableTextureFileExports(context);
                            return 0;
                        }))
                );
    }

    private void enableKeybindOverrides(CommandContext<FabricClientCommandSource> context) {
        GlobalProperties.get().sbFrameRenderingKeybindOverrides.set(true);
        Translate.commandFeedback(context, "sb_cosmetic_rendering_keybind_overrides_enabled");
    }

    private void disableKeybindOverrides(CommandContext<FabricClientCommandSource> context) {
        GlobalProperties.get().sbFrameRenderingKeybindOverrides.set(false);
        Translate.commandFeedback(context, "sb_cosmetic_rendering_keybind_overrides_disabled");
    }

    private void enableTextureFileExports(CommandContext<FabricClientCommandSource> context) {
        GlobalProperties.get().sbExportItemTextureData.set(true);
        Translate.commandFeedback(context, "sb_texture_file_exporting_enabled");
    }

    private void disableTextureFileExports(CommandContext<FabricClientCommandSource> context) {
        GlobalProperties.get().sbExportItemTextureData.set(false);
        Translate.commandFeedback(context, "sb_texture_file_exporting_disabled");
    }

    private void resetData(CommandContext<FabricClientCommandSource> context) {
        Translate.commandFeedback(context, "sb_cosmetic_timing_cache_reset");
        SkyBlockTimingDataCacher.getInstance().reset();
    }
}

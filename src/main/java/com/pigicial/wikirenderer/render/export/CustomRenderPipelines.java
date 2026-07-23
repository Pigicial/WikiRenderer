package com.pigicial.wikirenderer.render.export;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class CustomRenderPipelines {
    public static final RenderPipeline CORE_TERRAIN_CUTOUT_NO_TRANSPARENCY = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
            .withLocation("pipeline/wikirenderer_terrain_cutout_no_transparency")
            .withFragmentShader(Identifier.fromNamespaceAndPath(WikiRenderer.MOD_ID, "core_terrain_no_transparency"))
            .withShaderDefine("ALPHA_CUTOUT", 0.5f)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();

    // to fix leaves with transparent leaves turned off
    public static final RenderPipeline CORE_TERRAIN_SOLID_NO_TRANSPARENCY = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
            .withLocation("pipeline/wikirenderer_terrain_solid_no_transparency")
            .withFragmentShader(Identifier.fromNamespaceAndPath(WikiRenderer.MOD_ID, "core_terrain_no_transparency"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();

    // based on RenderPipelines.TEXT
    public static final RenderPipeline ITEM_FRAME_MAP_FULL_BRIGHTNESS = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
            .withLocation("pipeline/wikirenderer_item_frame_custom_brightness")
            .withVertexShader(Identifier.fromNamespaceAndPath(WikiRenderer.MOD_ID, "item_frame_full_bright"))
            .withFragmentShader("core/text")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    private static final Function<Identifier, RenderType> ITEM_FRAME_RENDER_TYPE_CACHE = Util.memoize(
            identifier -> RenderType.create(
                    "wikirenderer_item_frame_brightness_override",
                    RenderSetup.builder(ITEM_FRAME_MAP_FULL_BRIGHTNESS).withTexture("Sampler0", identifier).createRenderSetup()
            )
    );

    public static RenderType getCustomMapPipeline(Identifier id) {
        return ITEM_FRAME_RENDER_TYPE_CACHE.apply(id);
    }

    public static void register() {
        RenderPipelines.register(CORE_TERRAIN_CUTOUT_NO_TRANSPARENCY);
        RenderPipelines.register(CORE_TERRAIN_SOLID_NO_TRANSPARENCY);
        RenderPipelines.register(ITEM_FRAME_MAP_FULL_BRIGHTNESS);
    }
}

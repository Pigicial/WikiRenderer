package com.pigicial.wikirenderer.mixin.access;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("lightmapRenderStateExtractor")
    LightmapRenderStateExtractor wikirenderer$getLightmapRenderStateExtractor();

    @Accessor("lightmap")
    Lightmap wikirenderer$getLightmap();

    @Accessor("globalSettingsUniform")
    GlobalSettingsUniform wikirenderer$getGlobalSettingsUniform();
}

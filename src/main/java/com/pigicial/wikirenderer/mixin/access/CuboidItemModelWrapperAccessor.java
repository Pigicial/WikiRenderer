package com.pigicial.wikirenderer.mixin.access;

import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CuboidItemModelWrapper.class)
public interface CuboidItemModelWrapperAccessor {

    @Accessor("properties")
    ModelRenderProperties wikirenderer$getProperties();
}

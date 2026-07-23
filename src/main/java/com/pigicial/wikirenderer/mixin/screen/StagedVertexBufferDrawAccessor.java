package com.pigicial.wikirenderer.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$Draw")
public interface StagedVertexBufferDrawAccessor {

    @Accessor("indexCount")
    int owo$getIndexCount();
}

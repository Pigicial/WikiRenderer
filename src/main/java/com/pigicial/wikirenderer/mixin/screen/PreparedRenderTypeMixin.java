package com.pigicial.wikirenderer.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// allows item atlases to render properly during gui screen renders without having the custom model view stack data applied to it
@Mixin(PreparedRenderType.class)
public class PreparedRenderTypeMixin {
    @Inject(method = "drawFromBuffer", at = @At("HEAD"))
    private void wikirenderer$onDraw(StagedVertexBuffer.ExecuteInfo info, RenderPass renderPass, CallbackInfo ci) {
        // todo redo this
        if (WikiRenderer.inContainerScreenDraw /*&& RenderSystem.outputColorTextureOverride != null*/) {
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
        }
    }

    @Inject(method = "drawFromBuffer", at = @At("TAIL"))
    private void wikirenderer$onDrawEnd(StagedVertexBuffer.ExecuteInfo info, RenderPass renderPass, CallbackInfo ci) {
        if (WikiRenderer.inContainerScreenDraw /*&& RenderSystem.outputColorTextureOverride != null*/) {
            RenderSystem.getModelViewStack().popMatrix();
        }
    }
}

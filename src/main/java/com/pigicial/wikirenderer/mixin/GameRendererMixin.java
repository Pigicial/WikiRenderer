package com.pigicial.wikirenderer.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void overrideMainRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (WikiRenderer.mainTargetOverride != null) {
            cir.setReturnValue(WikiRenderer.mainTargetOverride);
        }
    }

    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    public void dontRenderInScreen(CallbackInfo ci) {
        if (!WikiRenderer.skipWorldRender) return;

        WikiRenderer.skipWorldRender = false;
        ci.cancel();
    }
}

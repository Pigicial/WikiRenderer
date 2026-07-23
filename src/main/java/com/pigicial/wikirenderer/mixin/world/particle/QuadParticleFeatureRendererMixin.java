package com.pigicial.wikirenderer.mixin.world.particle;

import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = QuadParticleFeatureRenderer.class, priority = 2000)
public class QuadParticleFeatureRendererMixin {

    /*
    @WrapOperation(
            method = "executeGroup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;mainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    )
    private RenderTarget overrideMainTarget(GameRenderer instance, Operation<RenderTarget> original) {
        if (WikiRenderer.mainTargetOverride != null) {
            return WikiRenderer.mainTargetOverride;
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "executeGroup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;particlesTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    )
    private RenderTarget overrideParticlesTarget(LevelRenderer instance, Operation<RenderTarget> original) {
        if (WikiRenderer.mainTargetOverride != null) {
            return WikiRenderer.mainTargetOverride;
        }
        return original.call(instance);
    }
     */
}
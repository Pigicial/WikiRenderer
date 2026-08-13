package com.pigicial.wikirenderer.mixin.texture;

import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.render.export.animation.AnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.state.GameRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// used to speed up enchantment glints
@Mixin(TextureTransform.class)
public class TextureTransformMixin {

    @Unique
    private static double savedGlintSpeed = Double.NaN;

    @Inject(
            method = "setupGlintTexturing(F)Lorg/joml/Matrix4f;",
            at = @At("HEAD")
    )
    private static void overrideGlint(float scale, CallbackInfoReturnable<Matrix4f> cir) {
        if (WikiRenderer.inRenderableDraw && GlobalProperties.get().speedUpEnchantmentGlints.get()) {
            GameRenderState state = Minecraft.getInstance().gameRenderer.gameRenderState();
            savedGlintSpeed = state.optionsRenderState.glintSpeed;
            state.optionsRenderState.glintSpeed = 1F;
        }
    }

    @Inject(
            method = "setupGlintTexturing(F)Lorg/joml/Matrix4f;",
            at = @At("RETURN")
    )
    private static void resetGlint(float scale, CallbackInfoReturnable<Matrix4f> cir) {
        if (Double.isNaN(savedGlintSpeed)) return;
        GameRenderState state = Minecraft.getInstance().gameRenderer.gameRenderState();
        state.optionsRenderState.glintSpeed = savedGlintSpeed;
        savedGlintSpeed = Float.NaN;
    }

    @ModifyConstant(method = "setupGlintTexturing", constant = @Constant(longValue = 110000L))
    private static long changeHorizontalModulo(long original) {
        if (WikiRenderer.inRenderableDraw && GlobalProperties.get().speedUpEnchantmentGlints.get()) {
            return 120000L; // 120,000L and 30,000L have a lowest common denominator of 120,000, whereas 110,000L and 30,000 require 330,000
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "setupGlintTexturing", constant = @Constant(floatValue = 110000.0F))
    private static float changeHorizontalDivisor(float original) {
        if (WikiRenderer.inRenderableDraw && GlobalProperties.get().speedUpEnchantmentGlints.get()) {
            return 120000.0F;
        } else {
            return original;
        }
    }

    // this used to just replace Util.getMillis() but that broke with replay mod so this'll have to do
    @ModifyVariable(method = "setupGlintTexturing", at = @At("STORE"), name = "millis")
    private static long changeGlintTiming(long millis) {
        GlobalProperties globalProperties = GlobalProperties.get();
        if (WikiRenderer.inRenderableDraw && globalProperties.syncEnchantmentGlintsToExport.get()) {
            AnimationHandler animationHandler = WikiRenderer.currentAnimationHandler;
            if (animationHandler != null && !animationHandler.isFinished()) {
                int totalFrameCount = animationHandler.getAnimationFrames();
                int framesRenderedSoFar = totalFrameCount - animationHandler.getRemainingFrames();

                int frameRate = globalProperties.exportFramerate.get();
                double secondsIntoAnimation = (double) framesRenderedSoFar / (double) frameRate;
                double rawMillis = secondsIntoAnimation * 1000.0;

                double glintSpeed = Minecraft.getInstance().gameRenderer.gameRenderState().optionsRenderState.glintSpeed;
                return (long) (rawMillis * glintSpeed * 8.0);
            } else {
                return 0L;
            }
        }
        return millis;
    }
}

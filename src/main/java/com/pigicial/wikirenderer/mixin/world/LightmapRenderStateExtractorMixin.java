package com.pigicial.wikirenderer.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.area.AreaPropertyBundle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {

    @WrapOperation(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;F)Ljava/lang/Object;"
            )
    )
    private Object emulateDayLightColorForBlockRenders(EnvironmentAttributeProbe instance, EnvironmentAttribute<?> attribute, float f, Operation<Integer> original) {
        if (WikiRenderer.inRenderableDraw && AreaPropertyBundle.INSTANCE.emulateDaylight.get()) {
            if (attribute == EnvironmentAttributes.SKY_LIGHT_COLOR) {
                return EnvironmentAttributes.SKY_LIGHT_COLOR.defaultValue();
            } else if (attribute == EnvironmentAttributes.SKY_LIGHT_FACTOR) {
                return EnvironmentAttributes.SKY_LIGHT_FACTOR.defaultValue();
            }
        }

        return original.call(instance, attribute, f);
    }

    @WrapOperation(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private Object forceHighGamma(OptionInstance<Double> instance, Operation<Double> original) {
        return WikiRenderer.inRenderableDraw && AreaPropertyBundle.INSTANCE.useFullBrightGamma.get() && instance == Minecraft.getInstance().options.gamma() ? Double.valueOf(50D) : original.call(instance);
    }

    @ModifyExpressionValue(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
                    ordinal = 0
            )
    )
    private boolean forceHasNightVision(boolean original) {
        return WikiRenderer.inRenderableDraw ? AreaPropertyBundle.INSTANCE.useNightVision.get() : original;
    }

    @WrapOperation(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;nightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
            )
    )
    private float forceFullNightVisionScale(LivingEntity camera, float a, Operation<Float> original) {
        return WikiRenderer.inRenderableDraw ? (AreaPropertyBundle.INSTANCE.useNightVision.get() ? 1.0f : 0.0f) : original.call(camera, a);
    }
}

package com.pigicial.wikirenderer.mixin.texture;

import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.item.ItemRenderablePropertyBundle;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasFoil", at = @At("HEAD"), cancellable = true)
    private void onHasFoil(CallbackInfoReturnable<Boolean> cir) {
        if (WikiRenderer.overrideGlint && ItemRenderablePropertyBundle.INSTANCE.overrideEnchantmentGlints.get()) {
            cir.setReturnValue(ItemRenderablePropertyBundle.INSTANCE.forceEnchantmentGlints.get());
        }
    }
}

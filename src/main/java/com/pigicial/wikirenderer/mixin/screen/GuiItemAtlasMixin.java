package com.pigicial.wikirenderer.mixin.screen;

import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiItemAtlas.class)
public class GuiItemAtlasMixin {

    @Inject(method = "drawToSlot", at = @At("HEAD"))
    private void wikirenderer$onDrawToSlotStart(int slotX, int slotY, boolean clear, ItemStackRenderState item, CallbackInfo ci) {
        WikiRenderer.inGuiItemAtlasDraw = true;
    }

    @Inject(method = "drawToSlot", at = @At("TAIL"))
    private void wikirenderer$onDrawToSlotEnd(int slotX, int slotY, boolean clear, ItemStackRenderState item, CallbackInfo ci) {
        WikiRenderer.inGuiItemAtlasDraw = false;
    }
}
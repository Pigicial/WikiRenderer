package com.pigicial.wikirenderer.mixin.texture;

import com.pigicial.wikirenderer.render.item.models.SelectItemModelCapturedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SelectItemModel.UnbakedSwitch.class)
public class UnbakedSwitchMixin {

    @ModifyArg(method = "bake", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;defaultReturnValue(Ljava/lang/Object;)V"), index = 0)
    private Object wikirenderer$captureDefaultModel(Object fallback) {
        SelectItemModelCapturedData.pendingFallback = ((ItemModel) fallback);
        return fallback;
    }

    @ModifyArg(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/SelectItemModel$UnbakedSwitch;createModelGetter(Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lnet/minecraft/util/RegistryContextSwapper;)Lnet/minecraft/client/renderer/item/SelectItemModel$ModelSelector;"))
    private <T> Object2ObjectMap<T, ItemModel> captureModels(Object2ObjectMap<T, ItemModel> originalModels) {
        SelectItemModelCapturedData.pendingBakedModels = originalModels;
        return originalModels;
    }
}

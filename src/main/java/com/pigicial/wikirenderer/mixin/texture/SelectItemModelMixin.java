package com.pigicial.wikirenderer.mixin.texture;

import com.pigicial.wikirenderer.render.item.models.SelectItemModelAccessor;
import com.pigicial.wikirenderer.render.item.models.SelectItemModelCapturedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModel.class)
public class SelectItemModelMixin<T extends ItemModel> implements SelectItemModelAccessor {

    @Unique
    private ItemModel wikirenderer$fallback;

    @Unique
    private Object2ObjectMap<?, ItemModel> wikirenderer$bakedModels;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void wikirenderer$storeFallback(
            SelectItemModelProperty<T> property,
            SelectItemModel.ModelSelector<T> models,
            CallbackInfo ci
    ) {
        this.wikirenderer$fallback = SelectItemModelCapturedData.pendingFallback;
        this.wikirenderer$bakedModels = SelectItemModelCapturedData.pendingBakedModels;
        SelectItemModelCapturedData.pendingFallback = null;
        SelectItemModelCapturedData.pendingBakedModels = null;
    }

    @Override
    public ItemModel wikirenderer$getFallback() {
        return wikirenderer$fallback;
    }

    @Override
    public Object2ObjectMap<?, ItemModel> wikirenderer$getBakedModels() {
        return wikirenderer$bakedModels;
    }
}

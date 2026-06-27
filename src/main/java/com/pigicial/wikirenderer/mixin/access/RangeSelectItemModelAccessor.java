package com.pigicial.wikirenderer.mixin.access;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangeSelectItemModel.class)
public interface RangeSelectItemModelAccessor {
    @Accessor("models")
    ItemModel[] wikirenderer$getModels();

    @Accessor("fallback")
    ItemModel wikirenderer$getFallback();
}

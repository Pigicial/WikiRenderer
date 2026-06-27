package com.pigicial.wikirenderer.mixin.access;

import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConditionalItemModel.class)
public interface ConditionalItemModelAccessor {

    @Accessor("onTrue")
    ItemModel wikirenderer$getOnTrue();

    @Accessor("onFalse")
    ItemModel wikirenderer$getOnFalse();
}

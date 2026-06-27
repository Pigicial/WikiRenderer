package com.pigicial.wikirenderer.render.item.models;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.item.ItemModel;

public interface SelectItemModelAccessor {
    ItemModel wikirenderer$getFallback();

    Object2ObjectMap<?, ItemModel> wikirenderer$getBakedModels();
}

package com.pigicial.wikirenderer.render.item.models;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.item.ItemModel;

public class SelectItemModelCapturedData {
    public static ItemModel pendingFallback = null;
    public static Object2ObjectMap<?, ItemModel> pendingBakedModels = null;
}

package com.pigicial.wikirenderer.render.export;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.textures.TextureData;
import com.pigicial.wikirenderer.textures.TextureDataProvider;

public final class HeadTextureTextExporter {

    private HeadTextureTextExporter() {}

    public static void exportIfEnabled(Renderable<?> renderable, ExportPathSpec exportPath, RenderScreen screen) {
        if (!GlobalProperties.get().sbExportItemTextureData.get()) {
            return;
        }
        //Both ItemRenderable & BatchRenderable implement TextureDataProvider
        if (!(renderable instanceof TextureDataProvider textureDataProvider)) {
            return;
        }

        TextureData textureData = textureDataProvider.getTextureData(null).get("item");
        if (textureData == null) {
            return;
        }

        MinecraftProfileTexture skinTexture = textureData.payload().textures().get(MinecraftProfileTexture.Type.SKIN);
        if (skinTexture == null) {
            return;
        }

        String hash = skinTexture.getHash();
        if (hash == null || hash.isBlank()) {
            return;
        }

        String text = "{{HeadRender|" + hash + "|creator=Hypixel}}";
        FileIO.saveTextAndNotify(text, exportPath, screen, "exported_texture_data_as");
    }
}
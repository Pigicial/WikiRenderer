package com.pigicial.wikirenderer.mixin.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.util.AnimationTimingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {

    @Inject(method = "submitModel", at = @At(value = "HEAD"))
    public <S> void wikirenderer$onSubmitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, @Nullable UvMapping uvMapping, int outlineColor, CallbackInfo ci) {
        if (WikiRenderer.animationTimingDataRequestedToFill != null && uvMapping != null) {
            AnimationTimingUtil.fillTimings(uvMapping, WikiRenderer.animationTimingDataRequestedToFill);
        }
    }

    @Inject(method = "submitBlockModel", at = @At(value = "HEAD"))
    public void wikirenderer$onSubmitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> modelParts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (WikiRenderer.animationTimingDataRequestedToFill != null && modelParts != null) {
            AnimationTimingUtil.fillBlockTimings(modelParts, WikiRenderer.animationTimingDataRequestedToFill);
        }
    }

    @Inject(method = "submitItem", at = @At(value = "HEAD"))
    public void wikirenderer$onSubmitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, ItemQuads quads, ItemStackRenderState.FoilType foilType, CallbackInfo ci) {
        if (WikiRenderer.animationTimingDataRequestedToFill != null && !quads.isEmpty()) {
            AnimationTimingUtil.fillTimings(quads, WikiRenderer.animationTimingDataRequestedToFill);
        }
    }

    @Inject(method = "submitFlame", at = @At(value = "HEAD"))
    public void wikirenderer$onSubmitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci) {
        if (WikiRenderer.animationTimingDataRequestedToFill != null) {
            AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
            TextureAtlasSprite fire0 = atlasManager.get(ModelBakery.FIRE_0);
            TextureAtlasSprite fire1 = atlasManager.get(ModelBakery.FIRE_1);
            AnimationTimingUtil.fillTimings(fire0, WikiRenderer.animationTimingDataRequestedToFill);
            AnimationTimingUtil.fillTimings(fire1, WikiRenderer.animationTimingDataRequestedToFill);
        }
    }
}

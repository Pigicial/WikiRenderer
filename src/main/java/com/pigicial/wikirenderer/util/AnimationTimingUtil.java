package com.pigicial.wikirenderer.util;

import com.pigicial.wikirenderer.mixin.access.ItemStackRenderStateAccessor;
import com.pigicial.wikirenderer.mixin.access.SpriteContentsAccessor;
import com.pigicial.wikirenderer.property.IntProperty;
import com.pigicial.wikirenderer.render.item.ItemRenderable;
import com.pigicial.wikirenderer.render.item.ItemRenderablePropertyBundle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnimationTimingUtil {

    private static final RandomSource RANDOM = RandomSource.create();

    public static void scanTicksToFullyAnimateItem(ItemRenderable renderable, List<Integer> animationTimings) {
        ItemStack stack = renderable.stack;

        ItemModelResolver itemModelResolver = Minecraft.getInstance().getItemModelResolver();
        Identifier modelId = stack.get(DataComponents.ITEM_MODEL);
        ModelManager modelManager = Minecraft.getInstance().getModelManager();

        List<ItemModel> models = renderable.getModels();
        IntProperty currentModelIndex = renderable.getCurrentModelIndex();

        ItemStackRenderState renderState = new ItemStackRenderState();
        if (ItemRenderablePropertyBundle.INSTANCE.useModelOverrides.get() && modelId != null && models != null && !models.isEmpty()) {
            renderState.setOversizedInGui(modelManager.getItemProperties(modelId).oversizedInGui());

            ItemModel itemModel = models.get(currentModelIndex == null ? 0 : Math.min(currentModelIndex.get(), models.size()) - 1);
            itemModel.update(renderState, stack, itemModelResolver, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
        } else {
            itemModelResolver.appendItemLayers(
                    renderState,
                    stack,
                    ItemDisplayContext.GUI,
                    Minecraft.getInstance().level,
                    null,
                    0
            );
        }

        for (ItemStackRenderState.LayerRenderState layer : ((ItemStackRenderStateAccessor) renderState).wikirenderer$getLayers()) {
            fillTimings(layer.prepareQuadList(), animationTimings);
        }

        renderState.clear();
    }

    public static void scanTicksToFullyAnimateItem(ItemStack itemStack, List<Integer> animationTimings) {
        Identifier modelIdentifier = itemStack.get(DataComponents.ITEM_MODEL);
        if (modelIdentifier == null) {
            return;
        }

        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().appendItemLayers(
                renderState,
                itemStack,
                ItemDisplayContext.GUI,
                Minecraft.getInstance().level,
                null,
                0
        );

        for (ItemStackRenderState.LayerRenderState layer : ((ItemStackRenderStateAccessor) renderState).wikirenderer$getLayers()) {
            fillTimings(layer.prepareQuadList(), animationTimings);
        }

        renderState.clear();
    }

    public static void scanTicksToFullyAnimateBlock(BlockState state, List<Integer> animationCompletionTimes, Long randomSeed) {
        scanTicksToFullyAnimateBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(state), animationCompletionTimes, randomSeed);
    }

    // synchronized for the random instance
    public static synchronized void scanTicksToFullyAnimateBlock(BlockStateModel model, List<Integer> animationCompletionTimes, Long randomSeed) {
        List<BlockModelPart> parts = new ArrayList<>();
        if (randomSeed != null) {
            RANDOM.setSeed(randomSeed);
        }
        model.collectParts(RANDOM, parts);

        for (BlockModelPart part : parts) {
            List<BakedQuad> quads = part instanceof SimpleModelWrapper wrapper ? wrapper.quads().getAll() : part.getQuads(null);
            fillTimings(quads, animationCompletionTimes);
        }
    }

    public static void fillTimings(Collection<BakedQuad> quads, List<Integer> animationCompletionTimes) {
        for (BakedQuad quad : quads) {
            fillTimings(quad.sprite(), animationCompletionTimes);
        }
    }

    public static void fillTimings(TextureAtlasSprite sprite, List<Integer> animationCompletionTimes) {
        SpriteContents.AnimatedTexture animatedTexture = ((SpriteContentsAccessor) sprite.contents()).wikirender$getAnimatedTexture();
        if (animatedTexture != null) {
            int time = 0;
            for (SpriteContents.FrameInfo frame : animatedTexture.frames) {
                time += frame.time();
            }
            animationCompletionTimes.add(time);
        }
    }

    public static long getSeamlessLoopDuration(List<List<Integer>> timings) {
        if (timings.isEmpty() || timings.getFirst().isEmpty()) {
            return 0;
        }

        long result = timings.getFirst().getFirst();
        for (List<Integer> timingSet : timings) {
            for (int time : timingSet) {
                result = getLowestCommonDenominator(result, time);
            }
        }

        return result;
    }

    private static long getLowestCommonDenominator(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return Math.abs(a * b) / getGreatestCommonDivisor(a, b);
    }

    private static long getGreatestCommonDivisor(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}

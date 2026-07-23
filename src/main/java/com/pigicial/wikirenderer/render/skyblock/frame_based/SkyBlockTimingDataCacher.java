package com.pigicial.wikirenderer.render.skyblock.frame_based;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.pigicial.wikirenderer.mixin.access.LevelAccessor;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.event.ClientRenderCallback;
import com.pigicial.wikirenderer.textures.PlayerTextureUtils;
import com.pigicial.wikirenderer.textures.TextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SkyBlockTimingDataCacher {

    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();
    private static final SkyBlockTimingDataCacher INSTANCE = new SkyBlockTimingDataCacher();

    public static SkyBlockTimingDataCacher getInstance() {
        return INSTANCE;
    }

    private final Cache<UUID, HeadTexturesTiming> textureData = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();
    private final Cache<GameProfile, InterpolatedTimings> combinedTextureAnimationFrameTimings = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();

    private final Cache<UUID, DyeColorTiming> dyeColorData = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();
    private final Cache<DyedArmorColorData, InterpolatedTimings> combinedDyeColorAnimationFrameTimings = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();

    @Nullable
    private TextureData textureMarkedAsFirstForNextRender = null;

    public void startTickEvent() {
        ClientRenderCallback.AFTER.register(_ -> scanEntitiesForData());
    }

    private void scanEntitiesForData() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        for (Entity entity : ((LevelAccessor) level).wikirenderer$getEntities().getAll()) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            boolean foundTextureDataOnArmor = false;
            for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
                if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR &&  Minecraft.getInstance().player == entity) {
                    continue; // ignore non-self-worn helmet skins
                }

                ItemStack item = livingEntity.getItemBySlot(slot);
                TextureData textureData = PlayerTextureUtils.getTextureDataFromPlayerHead(item);
                if (textureData != null) {
                    foundTextureDataOnArmor = slot.isArmor();
                    HeadTexturesTiming headTextures = this.textureData.getIfPresent(entity.getUUID());
                    if (headTextures == null) {
                        this.textureData.put(entity.getUUID(), new HeadTexturesTiming(textureData));
                    } else {
                        headTextures.submitTimings(textureData);
                    }
                }
            }

            if (!foundTextureDataOnArmor && Minecraft.getInstance().player == entity && !(Minecraft.getInstance().gui.screen() instanceof RenderScreen)) {
                // make it easier to swap helmets on and off yourself
                this.textureData.invalidate(entity.getUUID());
            }

            DyedItemColor helmetColor = livingEntity.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.DYED_COLOR);
            DyedItemColor chestplateColor = livingEntity.getItemBySlot(EquipmentSlot.CHEST).get(DataComponents.DYED_COLOR);
            DyedItemColor leggingsColor = livingEntity.getItemBySlot(EquipmentSlot.LEGS).get(DataComponents.DYED_COLOR);
            DyedItemColor bootsColor = livingEntity.getItemBySlot(EquipmentSlot.FEET).get(DataComponents.DYED_COLOR);

            if (helmetColor != null && chestplateColor != null && leggingsColor != null && bootsColor != null) {
                DyedArmorColorData colorData = new DyedArmorColorData(helmetColor.rgb(), chestplateColor.rgb(), leggingsColor.rgb(), bootsColor.rgb());
                DyeColorTiming colorTiming = this.dyeColorData.getIfPresent(entity.getUUID());
                if (colorTiming == null) {
                    this.dyeColorData.put(entity.getUUID(), new DyeColorTiming(colorData));
                } else {
                    colorTiming.submitTimings(colorData);
                }
            }
        }
    }

    public HeadTexturesTiming getTextureData(LivingEntity livingEntity) {
        return textureData.getIfPresent(livingEntity.getUUID());
    }

    public DyeColorTiming getDyeColorData(LivingEntity livingEntity) {
        return dyeColorData.getIfPresent(livingEntity.getUUID());
    }

    public void markTextureAsFirst(TextureData textureData) {
        textureMarkedAsFirstForNextRender = textureData;
    }

    @Nullable
    public TextureData getMarkedFirstTexture() {
        return this.textureMarkedAsFirstForNextRender;
    }

    public InterpolatedTimings getTextureTimings(List<FrameData<TextureData>> dataSet, int framesCount) {
        for (int i = 0, dataSetSize = dataSet.size(); i < Math.min(dataSetSize, framesCount); i++) {
            FrameData<TextureData> data = dataSet.get(i);
            InterpolatedTimings possibleTimings = this.combinedTextureAnimationFrameTimings.getIfPresent(data.sourceData().profile());
            if (possibleTimings != null) {
                if (possibleTimings.getFrameCount() < framesCount) {
                    this.combinedTextureAnimationFrameTimings.invalidate(data.sourceData().profile());
                } else if (possibleTimings.getFrameCount() == framesCount) {
                    return possibleTimings;
                }
            }
        }

        InterpolatedTimings timings = new InterpolatedTimings(framesCount);
        this.combinedTextureAnimationFrameTimings.put(dataSet.getFirst().sourceData().profile(), timings);
        return timings;
    }

    public InterpolatedTimings getColorTimings(List<FrameData<DyedArmorColorData>> dataSet, int framesCount) {
        for (int i = 0, dataSetSize = dataSet.size(); i < Math.min(dataSetSize, framesCount); i++) {
            FrameData<DyedArmorColorData> data = dataSet.get(i);
            InterpolatedTimings possibleTimings = this.combinedDyeColorAnimationFrameTimings.getIfPresent(data.sourceData());
            if (possibleTimings != null) {
                if (possibleTimings.getFrameCount() < framesCount) {
                    this.combinedDyeColorAnimationFrameTimings.invalidate(data.sourceData());
                } else if (possibleTimings.getFrameCount() == framesCount) {
                    return possibleTimings;
                }
            }
        }

        InterpolatedTimings timings = new InterpolatedTimings(framesCount);
        this.combinedDyeColorAnimationFrameTimings.put(dataSet.getFirst().sourceData(), timings);
        return timings;
    }

    public void reset() {
        textureData.invalidateAll();
        dyeColorData.invalidateAll();
        combinedTextureAnimationFrameTimings.invalidateAll();
        combinedDyeColorAnimationFrameTimings.invalidateAll();
    }
}

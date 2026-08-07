package com.pigicial.wikirenderer.render.entity.options;

import com.pigicial.wikirenderer.mixin.access.BlockEntityRenderDispatcherAccessor;
import com.pigicial.wikirenderer.mixin.access.LivingEntityRendererAccessor;
import com.pigicial.wikirenderer.mixin.access.ModelPartAccessor;
import com.pigicial.wikirenderer.render.entity.options.types.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.animal.frog.TadpoleModel;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.golem.CopperGolemState;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

public class EntityTypeSpecificOverrides<S extends EntityRenderState> {

    private static final Map<Class<? extends EntityRenderState>, Consumer<EntityTypeSpecificOverrides<?>>> REGISTERED_OVERRIDES = new HashMap<>();

    // this is possibly the worst piece of code in this entire mod, but at least 26.1 will enable doing most this via reflection
    static {
        registerOverrides(AllayRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isDancing", s -> s.isDancing, (s, value) -> s.isDancing = value);
            overrides.registerBooleanOverride("isSpinning", s -> s.isSpinning, (s, value) -> s.isSpinning = value);
            overrides.registerFloatOverride("spinningProgress", s -> s.spinningProgress, (s, value) -> s.spinningProgress = value);
            overrides.registerFloatOverride("holdingAnimationProgress", s -> s.holdingAnimationProgress, (s, value) -> s.holdingAnimationProgress = value);
        });

        registerOverrides(ArmadilloRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isHidingInShell", s -> s.isHidingInShell, (s, value) -> s.isHidingInShell = value);
            overrides.registerAnimationStateOverride("rollOutAnimationState", s -> s.rollOutAnimationState);
            overrides.registerAnimationStateOverride("rollUpAnimationState", s -> s.rollUpAnimationState);
            overrides.registerAnimationStateOverride("peekAnimationState", s -> s.peekAnimationState);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(ArmedEntityRenderState.class, overrides -> {
            overrides.registerEnumOverrideInCategory("hands", "mainArm", HumanoidArm.class, s -> s.mainArm, (s, value) -> s.mainArm = value);
            overrides.registerEnumOverrideInCategory("hands", "rightArmPose", HumanoidModel.ArmPose.class, s -> s.rightArmPose, (s, value) -> s.rightArmPose = value);
            overrides.registerItemStackOverrideInCategory("hands", "rightHandItem", s -> s.rightHandItemStack, (state, value) -> {
                state.rightHandItemStack = value;
                state.rightHandItemState.clear();
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForLiving(state.rightHandItemState, value, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, Minecraft.getInstance().player);
                }
            });
            overrides.registerEnumOverrideInCategory("hands", "leftArmPose", HumanoidModel.ArmPose.class, s -> s.leftArmPose, (s, value) -> s.leftArmPose = value);
            overrides.registerItemStackOverrideInCategory("hands", "leftHandItem", s -> s.leftHandItemStack, (state, value) -> {
                state.leftHandItemStack = value;
                state.leftHandItemState.clear();
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForLiving(state.leftHandItemState, value, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, Minecraft.getInstance().player);
                }
            });

            // todo: re-enable this
            // overrides.registerEnumOverrideInCategory("hands", "swingAnimationType", SwingAnimationType.class, s -> s.swingAnimationType, (s, value) -> s.swingAnimationType = value);
            // overrides.registerFloatOverrideInCategory("hands", "attackTime", s -> s.attackTime, (s, value) -> s.attackTime = value);
        });

        registerOverrides(ArmorStandRenderState.class, overrides -> {
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerFloatOverride("wiggle", s -> s.wiggle, (s, value) -> s.wiggle = value);
            overrides.registerBooleanOverride("isMarker", s -> s.isMarker, (s, value) -> s.isMarker = value);
            overrides.registerBooleanOverride("isSmall", s -> s.isSmall, (s, value) -> s.isSmall = value);
            overrides.registerBooleanOverride("showArms", s -> s.showArms, (s, value) -> s.showArms = value);
            overrides.registerBooleanOverride("showBasePlate", s -> s.showBasePlate, (s, value) -> s.showBasePlate = value);
            overrides.registerRotationsOverrides("headPose", s -> s.headPose, (s, value) -> s.headPose = value);
            overrides.registerRotationsOverrides("bodyPose", s -> s.bodyPose, (s, value) -> s.bodyPose = value);
            overrides.registerRotationsOverrides("leftArmPose", s -> s.leftArmPose, (s, value) -> s.leftArmPose = value);
            overrides.registerRotationsOverrides("rightArmPose", s -> s.rightArmPose, (s, value) -> s.rightArmPose = value);
            overrides.registerRotationsOverrides("leftLegPose", s -> s.leftLegPose, (s, value) -> s.leftLegPose = value);
            overrides.registerRotationsOverrides("rightLegPose", s -> s.rightLegPose, (s, value) -> s.rightLegPose = value);
        });

        registerOverrides(ArrowRenderState.class, overrides -> {
            overrides.registerFloatOverride("xRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerFloatOverride("shake", s -> s.shake, (s, value) -> s.shake = value);
        });

        registerOverrides(AvatarRenderState.class, overrides -> {
            overrides.registerBooleanOverrideInCategory("capes", "showCape", s -> s.showCape, (s, value) -> s.showCape = value);
            overrides.registerFloatOverrideInCategory("capes", "capeFlap", s -> s.capeFlap, (s, value) -> s.capeFlap = value);
            overrides.registerFloatOverrideInCategory("capes", "capeLean", s -> s.capeLean, (s, value) -> s.capeLean = value);
            overrides.registerFloatOverrideInCategory("capes", "capeLean2", s -> s.capeLean2, (s, value) -> s.capeLean2 = value);
            overrides.registerFloatOverrideInCategory("flight", "fallFlyingTimeInTicks", s -> s.fallFlyingTimeInTicks, (s, value) -> s.fallFlyingTimeInTicks = value);
            overrides.registerFloatOverrideInCategory("flight", "flyingYRotation", s -> s.flyingYRot, (state, value) -> {
                state.flyingYRot = value;
                state.shouldApplyFlyingYRot = true; // this is effectively replaced already
            });
            overrides.registerIntOverrideInCategory("extra_visuals", "arrowCount", s -> s.arrowCount, (s, value) -> s.arrowCount = value);
            overrides.registerIntOverrideInCategory("extra_visuals", "stingerCount", s -> s.stingerCount, (s, value) -> s.stingerCount = value);
            overrides.registerEnumOverrideInCategory("extra_visuals", "parrotOnLeftShoulder", Parrot.Variant.class, s -> s.parrotOnLeftShoulder, (s, value) -> s.parrotOnLeftShoulder = value);
            overrides.registerEnumOverrideInCategory("extra_visuals", "parrotOnRightShoulder", Parrot.Variant.class, s -> s.parrotOnRightShoulder, (s, value) -> s.parrotOnRightShoulder = value);
            overrides.registerBooleanOverrideInCategory("extra_visuals", "showExtraEars", s -> s.showExtraEars, (s, value) -> s.showExtraEars = value);
            overrides.registerItemStackOverrideInCategory("hands", "heldOnHeadItem", _ -> null, (state, value) -> {
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForNonLiving(state.heldOnHead, value, ItemDisplayContext.HEAD, Minecraft.getInstance().player);
                }
            });
        });

        registerOverrides(AxolotlRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", Axolotl.Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerFloatOverride("playingDeadFactor", s -> s.playingDeadFactor, (s, value) -> s.playingDeadFactor = value);
            overrides.registerFloatOverride("movingFactor", s -> s.movingFactor, (s, value) -> s.movingFactor = value);
            overrides.registerFloatOverride("inWaterFactor", s -> s.inWaterFactor, (s, value) -> s.inWaterFactor = value);
            overrides.registerFloatOverride("onGroundFactor", s -> s.onGroundFactor, (s, value) -> s.onGroundFactor = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(BatRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isResting", s -> s.isResting, (s, value) -> s.isResting = value);
            overrides.registerAnimationStateOverride("flyAnimationState", s -> s.flyAnimationState);
            overrides.registerAnimationStateOverride("restAnimationState", s -> s.restAnimationState);
        });

        registerOverrides(BeeRenderState.class, overrides -> {
            overrides.registerFloatOverride("rollAmount", s -> s.rollAmount, (s, value) -> s.rollAmount = value);
            overrides.registerBooleanOverride("hasStinger", s -> s.hasStinger, (s, value) -> s.hasStinger = value);
            overrides.registerBooleanOverride("isOnGround", s -> s.isOnGround, (s, value) -> s.isOnGround = value);
            overrides.registerBooleanOverride("isAngry", s -> s.isAngry, (s, value) -> s.isAngry = value);
            overrides.registerBooleanOverride("hasNectar", s -> s.hasNectar, (s, value) -> s.hasNectar = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        // block display entity

        registerOverrides(BoatRenderState.class, overrides -> {
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerIntOverride("hurtDir", s -> s.hurtDir, (s, value) -> s.hurtDir = value);
            overrides.registerFloatOverride("hurtTime", s -> s.hurtTime, (s, value) -> s.hurtTime = value);
            overrides.registerFloatOverride("damageTime", s -> s.damageTime, (s, value) -> s.damageTime = value);
            overrides.registerFloatOverride("bubbleAngle", s -> s.bubbleAngle, (s, value) -> s.bubbleAngle = value);
            overrides.registerBooleanOverride("boolean", s -> s.isUnderWater, (s, value) -> s.isUnderWater = value);
            overrides.registerFloatOverride("rowingTimeLeft", s -> s.rowingTimeLeft, (s, value) -> s.rowingTimeLeft = value);
            overrides.registerFloatOverride("rowingTimeRight", s -> s.rowingTimeRight, (s, value) -> s.rowingTimeRight = value);
        });

        registerOverrides(BoggedRenderState.class, overrides ->
                overrides.registerBooleanOverride("isSheared", s -> s.isSheared, (s, value) -> s.isSheared = value));

        registerOverrides(BreezeRenderState.class, overrides -> {
            overrides.registerAnimationStateOverride("idle", s -> s.idle);
            overrides.registerAnimationStateOverride("shoot", s -> s.shoot);
            overrides.registerAnimationStateOverride("slide", s -> s.slide);
            overrides.registerAnimationStateOverride("slideBack", s -> s.slideBack);
            overrides.registerAnimationStateOverride("inhale", s -> s.inhale);
            overrides.registerAnimationStateOverride("longJump", s -> s.longJump);
        });

        registerOverrides(CamelRenderState.class, overrides -> {
            overrides.registerItemStackOverride("saddle", s -> s.saddle, (s, value) -> s.saddle = value);
            overrides.registerBooleanOverride("isRidden", s -> s.isRidden, (s, value) -> s.isRidden = value);
            overrides.registerFloatOverride("jumpCooldown", s -> s.jumpCooldown, (s, value) -> s.jumpCooldown = value);
            overrides.registerAnimationStateOverride("sitAnimationState", s -> s.sitAnimationState);
            overrides.registerAnimationStateOverride("sitPoseAnimationState", s -> s.sitPoseAnimationState);
            overrides.registerAnimationStateOverride("sitUpAnimationState", s -> s.sitUpAnimationState);
            overrides.registerAnimationStateOverride("idleAnimationState", s -> s.idleAnimationState);
            overrides.registerAnimationStateOverride("dashAnimationState", s -> s.dashAnimationState);

            if (!(overrides.renderer instanceof CamelHuskRenderer)) {
                overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            }
        });

        registerOverrides(CatRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isLyingOnTopOfSleepingPlayer", s -> s.isLyingOnTopOfSleepingPlayer, (s, value) -> s.isLyingOnTopOfSleepingPlayer = value);
            overrides.registerEnumOverride("collarColor", DyeColor.class, s -> s.collarColor, (s, value) -> s.collarColor = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);

            overrides.registerRegistryOverrideWithFallback("variant", Registries.CAT_VARIANT, _ -> null, (s, value) -> {
                    // jank way of figuring out the non-applied variant
                    if (value == null) {
                        for (Holder.Reference<CatVariant> variant : RegistryOverride.getHolderValues(Registries.CAT_VARIANT)) {
                            CatVariant possibleVariant = variant.value();

                            Identifier babyTexture = possibleVariant.babyAssetInfo().texturePath();
                            Identifier adultTexture = possibleVariant.adultAssetInfo().texturePath();
                            if (Objects.equals(s.texture, babyTexture) || Objects.equals(s.texture, adultTexture)) {
                                value = possibleVariant;
                                break;
                            }
                        }
                    }
                    if (value == null) {
                        value = RegistryOverride.getHolderValue(Registries.CAT_VARIANT, CatVariants.TABBY).value();
                    }
                    s.texture = value.assetInfo(s.isBaby).texturePath();
                },
                (_, s) -> {
                    String fullString = s.assetInfo(false).id().toString();
                    String type = fullString.substring(fullString.lastIndexOf("/") + 1);
                    return OptionalOverride.toDisplayName(type);
                }, null, true);
        });

        registerOverrides(ChickenRenderState.class, overrides -> {
            overrides.registerFloatOverride("flap", s -> s.flap, (s, value) -> s.flap = value);
            overrides.registerFloatOverride("flapSpeed", s -> s.flapSpeed, (s, value) -> s.flapSpeed = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            overrides.registerRegistryOverride("variant", Registries.CHICKEN_VARIANT, s -> s.variant, (s, value) -> s.variant = value, (_, s) -> {
                String fullString = s.modelAndTexture().asset().id().toString();
                String type = fullString.substring(fullString.lastIndexOf("/") + 1);
                return OptionalOverride.toDisplayName(type);
            });
        });

        registerOverrides(CopperGolemRenderState.class, overrides -> {
            overrides.registerEnumOverride("weathering", WeatheringCopper.WeatherState.class, s -> s.weathering, (s, value) -> s.weathering = value);
            overrides.registerEnumOverride("copperGolemState", CopperGolemState.class, s -> s.copperGolemState, (s, value) -> s.copperGolemState = value);
            overrides.registerAnimationStateOverride("idleAnimationState", s -> s.idleAnimationState);
            overrides.registerAnimationStateOverride("interactionGetItem", s -> s.interactionGetItem);
            overrides.registerAnimationStateOverride("interactionGetNoItem", s -> s.interactionGetNoItem);
            overrides.registerAnimationStateOverride("interactionDropItem", s -> s.interactionDropItem);
            overrides.registerAnimationStateOverride("interactionDropNoItem", s -> s.interactionDropNoItem);
            // block state
        });

        registerOverrides(CowRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            overrides.registerRegistryOverride("variant", Registries.COW_VARIANT, s -> s.variant, (s, value) -> s.variant = value, (_, s) -> {
                String fullString = s.modelAndTexture().asset().id().toString();
                String type = fullString.substring(fullString.lastIndexOf("/") + 1);
                return OptionalOverride.toDisplayName(type);
            });
        });

        registerOverrides(CreakingRenderState.class, overrides -> {
            overrides.registerAnimationStateOverride("invulnerabilityAnimationState", s -> s.invulnerabilityAnimationState);
            overrides.registerAnimationStateOverride("attackAnimationState", s -> s.attackAnimationState);
            overrides.registerAnimationStateOverride("deathAnimationState", s -> s.deathAnimationState);
            overrides.registerBooleanOverride("eyesGlowing", s -> s.eyesGlowing, (s, value) -> s.eyesGlowing = value);
            overrides.registerBooleanOverride("canMove", s -> s.canMove, (s, value) -> s.canMove = value);
        });

        registerOverrides(CreeperRenderState.class, overrides -> {
            overrides.registerFloatOverride("swelling", s -> s.swelling, (s, value) -> s.swelling = value);
            overrides.registerBooleanOverride("isPowered", s -> s.isPowered, (s, value) -> s.isPowered = value);
        });

        // display entity

        registerOverrides(DolphinRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isMoving", s -> s.isMoving, (s, value) -> s.isMoving = value);
            overrides.registerFloatOverride("xRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(DonkeyRenderState.class, overrides -> {
            overrides.registerBooleanOverride("hasChest", s -> s.hasChest, (s, value) -> s.hasChest = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(EndCrystalRenderState.class, overrides -> {
            overrides.registerBooleanOverride("showsBottom", s -> s.showsBottom, (s, value) -> s.showsBottom = value);
            // vec3 beam offset
        });

        registerOverrides(EnderDragonRenderState.class, overrides -> {
            overrides.registerFloatOverride("flapTime", s -> s.flapTime, (s, value) -> s.flapTime = value);
            overrides.registerFloatOverride("deathTime", s -> s.deathTime, (s, value) -> s.deathTime = value);
            overrides.registerBooleanOverride("hasRedOverlay", s -> s.hasRedOverlay, (s, value) -> s.hasRedOverlay = value);
            overrides.registerDoubleOverride("beamOffsetX", s -> s.beamOffset == null ? 0 : s.beamOffset.x,
                    (s, value) -> s.beamOffset = s.beamOffset == null ? new Vec3(value, 0, 0) : new Vec3(value, s.beamOffset.y, s.beamOffset.z));
            overrides.registerDoubleOverride("beamOffsetY", s -> s.beamOffset == null ? 0 : s.beamOffset.y,
                    (s, value) -> s.beamOffset = s.beamOffset == null ? new Vec3(0, value, 0) : new Vec3(s.beamOffset.x, value, s.beamOffset.z));
            overrides.registerDoubleOverride("beamOffsetZ", s -> s.beamOffset == null ? 0 : s.beamOffset.x,
                    (s, value) -> s.beamOffset = s.beamOffset == null ? new Vec3(0, 0, value) : new Vec3(s.beamOffset.x, s.beamOffset.y, value));

            overrides.registerBooleanOverride("isLandingOrTakingOff", s -> s.isLandingOrTakingOff, (s, value) -> s.isLandingOrTakingOff = value);
            overrides.registerBooleanOverride("isSitting", s -> s.isSitting, (s, value) -> s.isSitting = value);
            overrides.registerDoubleOverride("distanceToEgg", s -> s.distanceToEgg, (s, value) -> s.distanceToEgg = value);
            overrides.registerFloatOverride("partialTicks", s -> s.partialTicks, (s, value) -> s.partialTicks = value);
            // flight history
        });

        registerOverrides(EndermanRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isCreepy", s -> s.isCreepy, (s, value) -> s.isCreepy = value);
            overrides.registerBlockStateOverride("carriedBlock", _ -> null, (s, value) -> {
                if (value != null) {
                    BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                    blockModelResolver.update(s.carriedBlock, value, EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
                }
            });
        });

        registerOverrides(EntityRenderState.class, overrides -> {
            overrides.registerBooleanOverrideInCategory("visibility", "isInvisible", s -> s.isInvisible, (s, value) -> s.isInvisible = value);
            overrides.registerFloatOverride("ageInTicks", s -> s.ageInTicks, (s, value) -> s.ageInTicks = value);

            if (overrides.renderer instanceof LivingEntityRenderer && !(overrides.renderer instanceof GuardianRenderer)) {
                overrides.registerFloatOverrideInCategory("sleeping", "bedOffset", s -> s.eyeHeight, (s, value) -> s.eyeHeight = value);
            }
            overrides.registerBooleanOverrideInCategory("extra_visuals", "displayFireAnimation", s -> s.displayFireAnimation, (s, value) -> s.displayFireAnimation = value);
        });

        registerOverrides(EquineRenderState.class, overrides -> {
            overrides.registerBooleanOverride("hasSaddle", s -> s.saddle.getItem() == Items.SADDLE, (state, value) -> {
                if (value) {
                    state.saddle = new ItemStack(Items.SADDLE);
                } else {
                    state.saddle = ItemStack.EMPTY;
                }
            });
            overrides.registerItemStackOverride("bodyArmorItem", s -> s.bodyArmorItem, (s, value) -> s.bodyArmorItem = value);
            overrides.registerBooleanOverride("isRidden", s -> s.isRidden, (s, value) -> s.isRidden = value);
            overrides.registerBooleanOverride("animateTail", s -> s.animateTail, (s, value) -> s.animateTail = value);
            overrides.registerFloatOverride("eatAnimation", s -> s.eatAnimation, (s, value) -> s.eatAnimation = value);
            overrides.registerFloatOverride("standAnimation", s -> s.standAnimation, (s, value) -> s.standAnimation = value);
            overrides.registerFloatOverride("feedingAnimation", s -> s.feedingAnimation, (s, value) -> s.feedingAnimation = value);
        });

        registerOverrides(EvokerFangsRenderState.class, overrides -> {
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerFloatOverride("biteProgress", s -> s.biteProgress, (s, value) -> s.biteProgress = value);
        });

        registerOverrides(EvokerRenderState.class, overrides ->
                overrides.registerBooleanOverride("isCastingSpell", s -> s.isCastingSpell, (s, value) -> s.isCastingSpell = value));

        registerOverrides(ExperienceOrbRenderState.class, overrides ->
                overrides.registerIntOverride("icon", s -> s.icon, (s, value) -> s.icon = value));

        // falling block

        registerOverrides(FelineRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isCrouching", s -> s.isCrouching, (s, value) -> s.isCrouching = value);
            overrides.registerBooleanOverride("isSprinting", s -> s.isSprinting, (s, value) -> s.isSprinting = value);
            overrides.registerBooleanOverride("isSitting", s -> s.isSitting, (s, value) -> s.isSitting = value);
            overrides.registerFloatOverride("lieDownAmount", s -> s.lieDownAmount, (s, value) -> s.lieDownAmount = value);
            overrides.registerFloatOverride("lieDownAmountTail", s -> s.lieDownAmountTail, (s, value) -> s.lieDownAmountTail = value);
            overrides.registerFloatOverride("relaxStateOneAmount", s -> s.relaxStateOneAmount, (s, value) -> s.relaxStateOneAmount = value);
            overrides.registerFloatOverride("ageScale", s -> s.ageScale, (s, value) -> s.ageScale = value, 0.5f);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(FireworkRocketRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isShotAtAngle", s -> s.isShotAtAngle, (s, value) -> s.isShotAtAngle = value);
            overrides.registerItemStackOverride("item", _ -> null, (state, value) -> {
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForNonLiving(state.item, value, ItemDisplayContext.GROUND, Minecraft.getInstance().player);
                }
            });
        });



        registerOverrides(FoxRenderState.class, overrides -> {
            overrides.registerFloatOverride("headRollAngle", s -> s.headRollAngle, (s, value) -> s.headRollAngle = value);
            overrides.registerFloatOverride("crouchAmount", s -> s.crouchAmount, (s, value) -> s.crouchAmount = value);
            overrides.registerBooleanOverride("isCrouching", s -> s.isCrouching, (s, value) -> s.isCrouching = value);
            overrides.registerBooleanOverride("isSleeping", s -> s.isSleeping, (s, value) -> s.isSleeping = value);
            overrides.registerBooleanOverride("isSitting", s -> s.isSitting, (s, value) -> s.isSitting = value);
            overrides.registerBooleanOverride("isFaceplanted", s -> s.isFaceplanted, (s, value) -> s.isFaceplanted = value);
            overrides.registerBooleanOverride("isPouncing", s -> s.isPouncing, (s, value) -> s.isPouncing = value);
            overrides.registerEnumOverride("variant", Fox.Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerFloatOverride("ageScale", s -> s.ageScale, (s, value) -> s.ageScale = value, 1f);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(FrogRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isSwimming", s -> s.isSwimming, (s, value) -> s.isSwimming = value);
            overrides.registerAnimationStateOverride("jumpAnimationState", s -> s.jumpAnimationState);
            overrides.registerAnimationStateOverride("croakAnimationState", s -> s.croakAnimationState);
            overrides.registerAnimationStateOverride("tongueAnimationState", s -> s.tongueAnimationState);
            overrides.registerAnimationStateOverride("swimIdleAnimationState", s -> s.swimIdleAnimationState);

            overrides.registerRegistryOverrideWithFallback("variant", Registries.FROG_VARIANT, s -> null, (s, value) -> {
                s.texture = value.assetInfo().texturePath();
            }, (key, s) -> {
                String id = s.assetInfo().id().toString();
                return OptionalOverride.toDisplayName(id.substring(id.lastIndexOf("/") + 1));
            }, FrogVariants.TEMPERATE, true);
        });

        registerOverrides(GhastRenderState.class, overrides ->
                overrides.registerBooleanOverride("isCharging", s -> s.isCharging, (s, value) -> s.isCharging = value));

        registerOverrides(GoatRenderState.class, overrides -> {
            overrides.registerBooleanOverride("hasLeftHorn", s -> s.hasLeftHorn, (s, value) -> s.hasLeftHorn = value);
            overrides.registerBooleanOverride("hasRightHorn", s -> s.hasRightHorn, (s, value) -> s.hasRightHorn = value);
            overrides.registerFloatOverride("rammingXHeadRotation", s -> s.rammingXHeadRot, (s, value) -> s.rammingXHeadRot = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(GuardianRenderState.class, overrides -> {
            overrides.registerFloatOverride("spikesAnimation", s -> s.spikesAnimation, (s, value) -> s.spikesAnimation = value);
            overrides.registerFloatOverride("tailAnimation", s -> s.tailAnimation, (s, value) -> s.tailAnimation = value);
            overrides.registerFloatOverride("eyeHeight", s -> s.eyeHeight, (s, value) -> s.eyeHeight = value);
            // vec3s
            // attack time and attack scale is based on the vec3s, which aren't modifiable atm - they should probably be based on offsets from the guardian rather than world pos
        });

        registerOverrides(HappyGhastRenderState.class, overrides -> {
            overrides.registerEnumOverride("harnessColor", DyeColor.class, _ -> null, (state, value) -> {
                if (value == null) {
                    state.bodyItem = ItemStack.EMPTY;
                } else {
                    state.bodyItem = Items.HARNESS.pick(value).getDefaultInstance();
                }
            });
            overrides.registerBooleanOverride("isRidden", s -> s.isRidden, (s, value) -> s.isRidden = value);
            overrides.registerBooleanOverride("isLeashHolder", s -> s.isLeashHolder, (s, value) -> s.isLeashHolder = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(HoglinRenderState.class, overrides -> {
            overrides.registerIntOverride("attackAnimationRemainingTicks", s -> s.attackAnimationRemainingTicks, (s, value) -> s.attackAnimationRemainingTicks = value);
            overrides.registerBooleanOverride("isConverting", s -> s.isConverting, (s, value) -> s.isConverting = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(HorseRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerEnumOverride("markings", Markings.class, s -> s.markings, (s, value) -> s.markings = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(HumanoidRenderState.class, overrides -> {
            if (overrides.renderer instanceof AvatarRenderer) {
                overrides.registerBooleanOverrideInCategory("water", "isVisuallySwimming", s -> s.isVisuallySwimming, (s, value) -> s.isVisuallySwimming = value);
            }

            overrides.registerBooleanOverrideInCategory("hands", "isUsingItem", s -> s.isUsingItem, (s, value) -> s.isUsingItem = value);
            overrides.registerEnumOverrideInCategory("hands", "useItemHand", InteractionHand.class, s -> s.useItemHand, (s, value) -> s.useItemHand = value);

            if (overrides.hasModelType(m -> m instanceof HumanoidModel)) {
                overrides.registerFloatOverrideInCategory("water", "swimAmount", s -> s.swimAmount, (s, value) -> s.swimAmount = value);
                overrides.registerFloatOverrideInCategory("walking", "walkSpeed", s -> s.speedValue, (s, value) -> s.speedValue = value, 1f);
                //overrides.registerFloatOverride("maxCrossbowChargeDuration", s -> s.maxCrossbowChargeDuration, (s, value) -> s.maxCrossbowChargeDuration = value);
                overrides.registerFloatOverrideInCategory("hands", "ticksUsingItem", s -> s.ticksUsingItem, (s, value) -> s.ticksUsingItem = value);

                // todo: re-enable this
                //overrides.registerEnumOverrideInCategory("hands", "attackArm", HumanoidArm.class, s -> s.attackArm, (s, value) -> s.attackArm = value);
            }

            overrides.registerBooleanOverride("isCrouching", s -> s.isCrouching, (s, value) -> s.isCrouching = value);
            overrides.registerBooleanOverrideInCategory("flight", "isFallFlying", s -> s.isFallFlying, (s, value) -> s.isFallFlying = value);
            overrides.registerBooleanOverride("isPassenger", s -> s.isPassenger, (s, value) -> s.isPassenger = value);

            if (overrides.hasLayerType(layer -> layer instanceof WingsLayer)) {
                overrides.registerFloatOverrideInCategory("capes", "elytraXRotation", s -> s.elytraRotX, (s, value) -> s.elytraRotX = value);
                overrides.registerFloatOverrideInCategory("capes", "elytraYRotation", s -> s.elytraRotY, (s, value) -> s.elytraRotY = value);
                overrides.registerFloatOverrideInCategory("capes", "elytraZRotation", s -> s.elytraRotZ, (s, value) -> s.elytraRotZ = value);
            }
            if (overrides.hasLayerType(layer -> layer instanceof HumanoidArmorLayer || layer instanceof CustomHeadLayer)) {
                overrides.registerItemStackOverride("helmet", s -> s.headEquipment, (state, value) -> {
                    state.headEquipment = value;

                    if (value.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock abstractSkullBlock) {
                        state.wornHeadType = abstractSkullBlock.getType();
                        state.wornHeadProfile = value.get(DataComponents.PROFILE);
                        state.headItem.clear();
                    } else {
                        state.wornHeadType = null;
                        state.wornHeadProfile = null;
                        state.headItem.clear();

                        if (!HumanoidArmorLayer.shouldRender(value, EquipmentSlot.HEAD)) {
                            Minecraft.getInstance().getItemModelResolver().updateForLiving(state.headItem, value, ItemDisplayContext.HEAD, Minecraft.getInstance().player);
                        }
                    }
                });
            }
            if (overrides.hasLayerType(layer -> layer instanceof HumanoidArmorLayer)) {
                overrides.registerItemStackOverride("chestplate", s -> s.chestEquipment, (s, value) -> s.chestEquipment = value);
                overrides.registerItemStackOverride("leggings", s -> s.legsEquipment, (s, value) -> s.legsEquipment = value);
                overrides.registerItemStackOverride("boots", s -> s.feetEquipment, (s, value) -> s.feetEquipment = value);
            }
        });


        registerOverrides(IllagerRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isRiding", s -> s.isRiding, (s, value) -> s.isRiding = value);
            overrides.registerBooleanOverride("isAggressive", s -> s.isAggressive, (s, value) -> s.isAggressive = value);
            overrides.registerEnumOverrideInCategory("hands", "mainArm", HumanoidArm.class, s -> s.mainArm, (s, value) -> s.mainArm = value);
            overrides.registerEnumOverrideInCategory("hands", "armPose", AbstractIllager.IllagerArmPose.class, s -> s.armPose, (s, value) -> s.armPose = value);
            //overrides.registerIntOverride("maxCrossbowChargeDuration", s -> s.maxCrossbowChargeDuration, (s, value) -> s.maxCrossbowChargeDuration = value);
            overrides.registerFloatOverrideInCategory("hands", "ticksUsingItem", s -> s.ticksUsingItem, (s, value) -> s.ticksUsingItem = value);

            // todo: re-enable this
            // overrides.registerFloatOverride("attackAnim", s -> s.attackAnim, (s, value) -> s.attackAnim = value);
        });

        // illusioner (vec3[])

        registerOverrides(IronGolemRenderState.class, overrides -> {
            overrides.registerFloatOverride("attackTicksRemaining", s -> s.attackTicksRemaining, (s, value) -> s.attackTicksRemaining = value);
            overrides.registerIntOverride("offerFlowerTick", s -> s.offerFlowerTick, (s, value) -> s.offerFlowerTick = value);
            overrides.registerEnumOverride("crackiness", Crackiness.Level.class, s -> s.crackiness, (s, value) -> s.crackiness = value);
        });

        registerOverrides(ItemClusterRenderState.class, overrides -> {
            overrides.registerItemStackOverride("item", _ -> null, (state, value) -> {
                state.item.clear();
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForNonLiving(state.item, value, ItemDisplayContext.GROUND, Minecraft.getInstance().player);
                }
            });
            overrides.registerIntOverride("count", s -> s.count, (s, value) -> s.count = value, 1);
            overrides.registerIntOverride("seed", s -> s.seed, (s, value) -> s.seed = value);
        });

        // item display entity

        registerOverrides(ItemEntityRenderState.class, overrides ->
                overrides.registerFloatOverride("bobOffset", s -> s.bobOffset, (s, value) -> s.bobOffset = value));

        registerOverrides(ItemFrameRenderState.class, overrides -> {
            overrides.registerEnumOverride("direction", Direction.class, s -> s.direction, (s, value) -> s.direction = value);
            overrides.registerItemStackOverride("item", _ -> null, (state, value) -> {
                state.item.clear();
                if (value != null) {
                    Minecraft.getInstance().getItemModelResolver().updateForNonLiving(state.item, value, ItemDisplayContext.FIXED, Minecraft.getInstance().player);
                }
            });
            overrides.registerIntOverride("rotation", s -> s.rotation, (s, value) -> s.rotation = value);
            overrides.registerBooleanOverride("isGlowFrame", s -> s.isGlowFrame, (s, value) -> s.isGlowFrame = value);
            overrides.registerIntOverride("mapId", s -> s.mapId.id(), (s, value) -> s.mapId = new MapId(value));
            // map render state
        });

        registerOverrides(LightningBoltRenderState.class, overrides ->
                overrides.registerIntOverride("seed", state -> (int) state.seed, (s, value) -> s.seed = value));

        registerOverrides(LivingEntityRenderState.class, overrides -> {
            overrides.registerBooleanOverrideInCategory("visibility", "isInvisibleToPlayer", s -> s.isInvisibleToPlayer, (s, value) -> s.isInvisibleToPlayer = value);

            overrides.registerFloatOverride("bodyRotation", s -> s.bodyRot, (s, value) -> s.bodyRot = value);
            if (overrides.hasModelPart((s, _) -> s.contains("head"))) {
                overrides.registerFloatOverride("xHeadRotation", s -> s.xRot, (s, value) -> s.xRot = value);
                overrides.registerFloatOverride("yHeadRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            }

            if (overrides.hasModelPart((s, _) -> s.contains("leg"))) {
                overrides.registerFloatOverrideInCategory("walking", "walkAnimationPosition", s -> s.walkAnimationPos, (s, value) -> s.walkAnimationPos = value);
                overrides.registerFloatOverrideInCategory("walking", "walkAnimationSpeed", s -> s.walkAnimationSpeed, (s, value) -> s.walkAnimationSpeed = value);
            }

            overrides.registerFloatOverride("scale", s -> s.scale, (s, value) -> s.scale = value, 1f);

            if (overrides.hasLayerType(l -> l instanceof ItemInHandLayer)) {
                overrides.registerFloatOverrideInCategory("hands", "ticksSinceSpearHitFeedback", s -> s.ticksSinceKineticHitFeedback, (s, value) -> s.ticksSinceKineticHitFeedback = value);
            }
            overrides.registerBooleanOverride("isUpsideDown", s -> s.isUpsideDown, (s, value) -> s.isUpsideDown = value);
            overrides.registerBooleanOverride("isFullyFrozen", s -> s.isFullyFrozen, (s, value) -> s.isFullyFrozen = value);


            // i hate this
            if (overrides.hasModelType(model -> model instanceof AbstractEquineModel || model instanceof CodModel || model instanceof SalmonModel || model instanceof TropicalFishSmallModel || model instanceof TropicalFishLargeModel || model instanceof TadpoleModel) || overrides.renderer instanceof SalmonRenderer || overrides.renderer instanceof TropicalFishRenderer || overrides.renderer instanceof AvatarRenderer) {
                overrides.registerBooleanOverrideInCategory("water", "isInWater", s -> s.isInWater, (s, value) -> s.isInWater = value);
            }

            overrides.registerBooleanOverrideInCategory("extra_visuals", "hasRedOverlay", s -> s.hasRedOverlay, (s, value) -> s.hasRedOverlay = value);
            overrides.registerFloatOverride("deathTime", s -> s.deathTime, (s, value) -> s.deathTime = value);
            overrides.registerBooleanOverride("isAutoSpinAttack", s -> s.isAutoSpinAttack, (s, value) -> s.isAutoSpinAttack = value);
            // other pose types aren't checked anywhere, only sleeping is used, so just have an option for that instead
            overrides.registerBooleanOverrideInCategory("sleeping", "isSleeping", s -> s.hasPose(Pose.SLEEPING), (s, value) -> s.pose = value ? Pose.SLEEPING : Pose.STANDING);
            overrides.registerEnumOverrideInCategory("sleeping", "bedOrientation", Direction.class, s -> s.bedOrientation, (s, value) -> s.bedOrientation = value);

            if (overrides.hasLayerType(l -> l instanceof CustomHeadLayer) && !overrides.hasLayerType(l -> l instanceof HumanoidArmorLayer)) {
                overrides.registerEnumOverride("wornHeadType", SkullBlock.Types.class, state -> (SkullBlock.Types) state.wornHeadType, (s, value) -> s.wornHeadType = value);
                overrides.registerItemStackOverride("headItem", _ -> null, (state, value) -> {
                    if (value.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock abstractSkullBlock) {
                        state.headItem.clear();
                        state.wornHeadType = abstractSkullBlock.getType();
                        state.wornHeadProfile = value.get(DataComponents.PROFILE);
                    } else {
                        state.headItem.clear();
                        state.wornHeadType = null;
                        state.wornHeadProfile = null;

                        if (!HumanoidArmorLayer.shouldRender(value, EquipmentSlot.HEAD)) {
                            Minecraft.getInstance().getItemModelResolver().updateForLiving(state.headItem, value, ItemDisplayContext.HEAD, Minecraft.getInstance().player);
                        }
                    }
                });
                // for custom head items it seems
                overrides.registerFloatOverride("wornHeadAnimationPosition", s -> s.wornHeadAnimationPos, (s, value) -> s.wornHeadAnimationPos = value);
            }
        });

        registerOverrides(LlamaRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", Llama.Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerBooleanOverride("hasChest", s -> s.hasChest, (s, value) -> s.hasChest = value);
            overrides.registerEnumOverride("swagColor", DyeColor.class, _ -> null, (state, value) -> {
                if (value == null) {
                    state.bodyItem = null;
                } else {
                    state.bodyItem = new ItemStack(Items.CARPET.white());
                    state.bodyItem.set(DataComponents.EQUIPPABLE, Equippable.llamaSwag(value));
                }
            });
            overrides.registerBooleanOverride("isTraderLlama", s -> s.isTraderLlama, (s, value) -> s.isTraderLlama = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(LlamaSpitRenderState.class, overrides -> {
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerFloatOverride("xRotation", s -> s.xRot, (s, value) -> s.xRot = value);
        });

        registerOverrides(MinecartRenderState.class, overrides -> {
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerFloatOverride("xRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerIntOverride("offsetSeed", state -> (int) state.offsetSeed, (s, value) -> s.offsetSeed = value);
            overrides.registerIntOverride("hurtDir", s -> s.hurtDir, (s, value) -> s.hurtDir = value);
            overrides.registerFloatOverride("hurtTime", s -> s.hurtTime, (s, value) -> s.hurtTime = value);
            overrides.registerFloatOverride("damageTime", s -> s.damageTime, (s, value) -> s.damageTime = value);
            overrides.registerIntOverride("displayOffset", s -> s.displayOffset, (s, value) -> s.displayOffset = value);
            overrides.registerBlockStateOverride("displayedBlockState", _ -> null, (s, value) -> {
                if (value != null) {
                    BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                    blockModelResolver.update(s.displayBlockModel, value, AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT);
                }
            });
            overrides.registerBooleanOverride("isNewRender", s -> s.isNewRender, (s, value) -> s.isNewRender = value);
            // position stuff vec3s
        });

        registerOverrides(MinecartTntRenderState.class, overrides ->
                overrides.registerFloatOverride("fuseRemainingInTicks", s -> s.fuseRemainingInTicks, (s, value) -> s.fuseRemainingInTicks = value));

        registerOverrides(MushroomCowRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", MushroomCow.Variant.class, s -> s.variant, (s, value) -> {
                s.variant = value;
                if (value != null) {
                    s.mushroomModel.clear();
                    BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                    blockModelResolver.update(s.mushroomModel, value.getBlockState(), MushroomCowRenderer.BLOCK_DISPLAY_CONTEXT);
                }
            });
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(NautilusRenderState.class, overrides -> {
            overrides.registerItemStackOverride("saddle", s -> s.saddle, (s, value) -> s.saddle = value);
            overrides.registerItemStackOverride("bodyArmorItem", s -> s.bodyArmorItem, (s, value) -> s.bodyArmorItem = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);

            if (overrides.renderer instanceof ZombieNautilusRenderer) {
                overrides.registerRegistryOverride("variant", Registries.ZOMBIE_NAUTILUS_VARIANT, s -> s.variant, (s, value) -> s.variant = value, (_, s) -> {
                    String fullString = s.modelAndTexture().asset().id().toString();
                    String type = fullString.substring(fullString.lastIndexOf("/") + 1);
                    return OptionalOverride.toDisplayName(type);
                });
            }

            overrides.registerFloatOverride("xHeadRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yHeadRotation", s -> s.yRot, (s, value) -> s.yRot = value);
        });

        registerOverrides(PaintingRenderState.class, overrides -> {
            overrides.registerEnumOverride("direction", Direction.class, s -> s.direction, (s, value) -> s.direction = value);
            // lighting

            overrides.registerRegistryOverride("variant", Registries.PAINTING_VARIANT, s -> s.variant, (s, value) -> {
                s.variant = value;
                if (value != null) {
                    int width = value.width();
                    int height = value.height();
                    int firstLighting = s.lightCoordsPerBlock[0];
                    if (s.lightCoordsPerBlock.length != width * height) {
                        s.lightCoordsPerBlock = new int[width * height];
                    }
                    // janky since it isnt guaranteed to match actual world lighting but whatever
                    Arrays.fill(s.lightCoordsPerBlock, firstLighting);
                }
            }, (key, _) -> OptionalOverride.toDisplayName(key.identifier().getPath()));
        });

        registerOverrides(PandaRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", Panda.Gene.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerBooleanOverride("variant", s -> s.isUnhappy, (s, value) -> s.isUnhappy = value);
            overrides.registerBooleanOverride("variant", s -> s.isSneezing, (s, value) -> s.isSneezing = value);
            overrides.registerIntOverride("variant", s -> s.sneezeTime, (s, value) -> s.sneezeTime = value);
            overrides.registerBooleanOverride("isEating", s -> s.isEating, (s, value) -> s.isEating = value);
            overrides.registerBooleanOverride("isScared", s -> s.isScared, (s, value) -> s.isScared = value);
            overrides.registerBooleanOverride("isSitting", s -> s.isSitting, (s, value) -> s.isSitting = value);
            overrides.registerFloatOverride("sitAmount", s -> s.sitAmount, (s, value) -> s.sitAmount = value);
            overrides.registerFloatOverride("lieOnBackAmount", s -> s.lieOnBackAmount, (s, value) -> s.lieOnBackAmount = value);
            overrides.registerFloatOverride("rollAmount", s -> s.rollAmount, (s, value) -> s.rollAmount = value);
            overrides.registerFloatOverride("rollTime", s -> s.rollTime, (s, value) -> s.rollTime = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(ParrotRenderState.class, overrides -> {
            overrides.registerEnumOverride("variant", Parrot.Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerFloatOverride("flapAngle", s -> s.flapAngle, (s, value) -> s.flapAngle = value);
            overrides.registerEnumOverride("pose", ParrotModel.Pose.class, s -> s.pose, (s, value) -> s.pose = value);
        });

        registerOverrides(PhantomRenderState.class, overrides -> {
            overrides.registerFloatOverride("flapTime", s -> s.flapTime, (s, value) -> s.flapTime = value);
            overrides.registerIntOverride("size", s -> s.size, (s, value) -> s.size = value);
        });

        registerOverrides(PhantomRenderState.class, overrides -> {
            overrides.registerFloatOverride("flapTime", s -> s.flapTime, (s, value) -> s.flapTime = value);
            overrides.registerIntOverride("size", s -> s.size, (s, value) -> s.size = value);
        });

        registerOverrides(PiglinRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isBrute", s -> s.isBrute, (s, value) -> s.isBrute = value);
            overrides.registerBooleanOverride("isConverting", s -> s.isConverting, (s, value) -> s.isConverting = value);
            overrides.registerFloatOverride("maxCrossbowChageDuration", s -> s.maxCrossbowChageDuration, (s, value) -> s.maxCrossbowChageDuration = value);
            overrides.registerEnumOverride("armPose", PiglinArmPose.class, s -> s.armPose, (s, value) -> s.armPose = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(PigRenderState.class, overrides -> {
            overrides.registerBooleanOverride("hasSaddle", s -> s.saddle != null, (state, value) -> {
                if (value) {
                    state.saddle = new ItemStack(Items.SADDLE);
                } else {
                    state.saddle = ItemStack.EMPTY;
                }
            });
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            overrides.registerRegistryOverride("variant", Registries.PIG_VARIANT, s -> s.variant, (s, value) -> s.variant = value, (_, s) -> {
                String fullString = s.modelAndTexture().asset().id().toString();
                String type = fullString.substring(fullString.lastIndexOf("/") + 1);
                return OptionalOverride.toDisplayName(type);
            });
        });

        registerOverrides(PolarBearRenderState.class, overrides -> {
            overrides.registerFloatOverride("standScale", s -> s.standScale, (s, value) -> s.standScale = value);
            overrides.registerFloatOverride("ageScale", s -> s.ageScale, (s, value) -> s.ageScale = value, 1f);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(PufferfishRenderState.class, overrides ->
                overrides.registerIntOverride("puffState", s -> s.puffState, (s, value) -> s.puffState = value));

        registerOverrides(RabbitRenderState.class, overrides -> {
            overrides.registerFloatOverride("jumpCompletion", s -> s.jumpCompletion, (s, value) -> s.jumpCompletion = value);
            overrides.registerBooleanOverride("isToast", s -> s.isToast, (s, value) -> s.isToast = value);
            overrides.registerEnumOverride("variant", Rabbit.Variant.class, s -> s.variant, (s, value) -> s.variant = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            overrides.registerAnimationStateOverride("hopAnimationState", s -> s.hopAnimationState);
            overrides.registerAnimationStateOverride("idleHeadTiltAnimationState", s -> s.idleHeadTiltAnimationState);
        });

        registerOverrides(RavagerRenderState.class, overrides -> {
            overrides.registerFloatOverride("stunnedTicksRemaining", s -> s.stunnedTicksRemaining, (s, value) -> s.stunnedTicksRemaining = value);
            overrides.registerFloatOverride("attackTicksRemaining", s -> s.attackTicksRemaining, (s, value) -> s.attackTicksRemaining = value);
            overrides.registerFloatOverride("roarAnimation", s -> s.roarAnimation, (s, value) -> s.roarAnimation = value);
        });

        registerOverrides(SalmonRenderState.class, overrides -> overrides.registerEnumOverride("variant", Salmon.Variant.class, s -> s.variant, (s, value) -> s.variant = value));

        registerOverrides(SheepRenderState.class, overrides -> {
            overrides.registerFloatOverride("headEatPositionScale", s -> s.headEatPositionScale, (s, value) -> s.headEatPositionScale = value);
            overrides.registerFloatOverride("headEatAngleScale", s -> s.headEatAngleScale, (s, value) -> s.headEatAngleScale = value);
            overrides.registerBooleanOverride("isSheared", s -> s.isSheared, (s, value) -> s.isSheared = value);
            overrides.registerEnumOverride("woolColor", DyeColor.class, s -> s.woolColor, (s, value) -> s.woolColor = value);
            overrides.registerBooleanOverride("isJebSheep", s -> s.isJebSheep, (s, value) -> s.isJebSheep = value);
            overrides.registerFloatOverride("ageScale", s -> s.ageScale, (s, value) -> s.ageScale = value, 1f);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(ShulkerBulletRenderState.class, overrides -> {
            overrides.registerFloatOverride("xRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yRotation", s -> s.yRot, (s, value) -> s.yRot = value);
        });

        registerOverrides(ShulkerRenderState.class, overrides -> {
            // vec3 renderOffset
            overrides.registerEnumOverride("color", DyeColor.class, s -> s.color, (s, value) -> s.color = value);
            overrides.registerFloatOverride("peekAmount", s -> s.peekAmount, (s, value) -> s.peekAmount = value);
            overrides.registerFloatOverride("yHeadRotation", s -> s.yHeadRot, (s, value) -> s.yHeadRot = value);
            overrides.registerFloatOverride("yBodyRotation", s -> s.yBodyRot, (s, value) -> s.yBodyRot = value);
            overrides.registerEnumOverride("yBodyRotation", Direction.class, s -> s.attachFace, (s, value) -> s.attachFace = value);
        });

        registerOverrides(SkeletonRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isAggressive", s -> s.isAggressive, (s, value) -> s.isAggressive = value);
            overrides.registerBooleanOverride("isShaking", s -> s.isShaking, (s, value) -> s.isShaking = value);
            overrides.registerBooleanOverride("isHoldingBow", s -> s.isHoldingBow, (s, value) -> s.isHoldingBow = value);
        });

        registerOverrides(SlimeRenderState.class, overrides -> {
            overrides.registerFloatOverride("squish", s -> s.squish, (s, value) -> s.squish = value);
            overrides.registerIntOverride("size", s -> s.size, (s, value) -> s.size = value, 1);
        });

        registerOverrides(SnifferRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isSearching", s -> s.isSearching, (s, value) -> s.isSearching = value);
            overrides.registerAnimationStateOverride("diggingAnimationState", s -> s.diggingAnimationState);
            overrides.registerAnimationStateOverride("sniffingAnimationState", s -> s.sniffingAnimationState);
            overrides.registerAnimationStateOverride("risingAnimationState", s -> s.risingAnimationState);
            overrides.registerAnimationStateOverride("feelingHappyAnimationState", s -> s.feelingHappyAnimationState);
            overrides.registerAnimationStateOverride("scentingAnimationState", s -> s.scentingAnimationState);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(SnowGolemRenderState.class, overrides ->
                overrides.registerBlockStateOverride("headBlock", _ -> null, (s, value) -> {
                    if (value != null) {
                        BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                        blockModelResolver.update(s.headBlock, value, SnowGolemRenderer.BLOCK_DISPLAY_CONTEXT);
                    }
                }));

        registerOverrides(SquidRenderState.class, overrides -> {
            overrides.registerFloatOverride("tentacleAngle", s -> s.tentacleAngle, (s, value) -> s.tentacleAngle = value);
            overrides.registerFloatOverride("xBodyRotation", s -> s.xBodyRot, (s, value) -> s.xBodyRot = value);
            overrides.registerFloatOverride("zBodyRotation", s -> s.zBodyRot, (s, value) -> s.zBodyRot = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(StriderRenderState.class, overrides -> {
            overrides.registerBooleanOverride("hasSaddle", s -> s.saddle != null, (state, value) -> {
                if (value) {
                    state.saddle = new ItemStack(Items.SADDLE);
                } else {
                    state.saddle = ItemStack.EMPTY;
                }
            });
            overrides.registerBooleanOverride("isSuffocating", s -> s.isSuffocating, (s, value) -> s.isSuffocating = value);
            overrides.registerBooleanOverride("isRidden", s -> s.isRidden, (s, value) -> s.isRidden = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);

            overrides.registerFloatOverride("xHeadRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yHeadRotation", s -> s.yRot, (s, value) -> s.yRot = value);
        });

        registerOverrides(SulfurCubeRenderState.class, overrides -> {
            overrides.registerBlockStateOverride("containedBlock", s -> null, (s, value) -> {
                if (value != null) {
                    BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                    blockModelResolver.update(s.containedBlock, value, SulfurCubeRenderer.BLOCK_DISPLAY_CONTEXT);
                }
            });
            overrides.registerFloatOverride("fuseRemainingTicks", s -> s.fuseRemainingTicks, (s, value) -> s.fuseRemainingTicks = value);
        });

        // text display

        registerOverrides(ThrownItemRenderState.class, overrides ->
                overrides.registerItemStackOverride("item", _ -> null, (state, value) -> {
                    state.item.clear();
                    if (value != null) {
                        Minecraft.getInstance().getItemModelResolver().updateForLiving(state.item, value, ItemDisplayContext.GROUND, Minecraft.getInstance().player);
                    }
                }));

        registerOverrides(ThrownTridentRenderState.class, overrides -> {
            overrides.registerFloatOverride("xHeadRotation", s -> s.xRot, (s, value) -> s.xRot = value);
            overrides.registerFloatOverride("yHeadRotation", s -> s.yRot, (s, value) -> s.yRot = value);
            overrides.registerBooleanOverride("isFoil", s -> s.isFoil, (s, value) -> s.isFoil = value);
        });

        registerOverrides(TippableArrowRenderState.class, overrides ->
                overrides.registerBooleanOverride("isTipped", s -> s.isTipped, (s, value) -> s.isTipped = value));

        registerOverrides(TntRenderState.class, overrides -> {
            overrides.registerFloatOverride("fuseRemainingInTicks", s -> s.fuseRemainingInTicks, (s, value) -> s.fuseRemainingInTicks = value);
            overrides.registerBlockStateOverride("blockState", _ -> null, (s, value) -> {
                if (value != null) {
                    BlockModelResolver blockModelResolver = ((BlockEntityRenderDispatcherAccessor) Minecraft.getInstance().getBlockEntityRenderDispatcher()).wikirenderer$getBlockModelResolver();
                    blockModelResolver.update(s.blockState, value, TntMinecartRenderer.BLOCK_DISPLAY_CONTEXT);
                }
            });
        });

        registerOverrides(TropicalFishRenderState.class, overrides -> {
            overrides.registerEnumOverride("pattern", TropicalFish.Pattern.class, s -> s.pattern, (s, value) -> s.pattern = value);
            overrides.registerIntOverride("baseColor", s -> s.baseColor, (s, value) -> s.baseColor = value, -1);
            overrides.registerIntOverride("patternColor", s -> s.patternColor, (s, value) -> s.patternColor = value, -1);
        });

        registerOverrides(TurtleRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isOnLand", s -> s.isOnLand, (s, value) -> s.isOnLand = value);
            overrides.registerBooleanOverride("isLayingEgg", s -> s.isLayingEgg, (s, value) -> s.isLayingEgg = value);
            overrides.registerBooleanOverride("hasEgg", s -> s.hasEgg, (s, value) -> s.hasEgg = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(VexRenderState.class, overrides ->
                overrides.registerBooleanOverride("isCharging", s -> s.isCharging, (s, value) -> s.isCharging = value));

        registerOverrides(VillagerRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isUnhappy", s -> s.isUnhappy, (s, value) -> s.isUnhappy = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);

            overrides.registerRegistryOverrideWithFallback("type", Registries.VILLAGER_TYPE, _ -> null, (s, value) -> {
                Holder<VillagerType> type = RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, value);
                Holder<VillagerProfession> profession = s.villagerData != null ? s.villagerData.profession() : RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, VillagerProfession.NONE);
                int level = s.villagerData != null ? s.villagerData.level() : 1;
                s.villagerData = new VillagerData(type, profession, level);
            }, (key, _) -> OptionalOverride.toDisplayName(key.identifier().getPath()), VillagerType.PLAINS, false);

            overrides.registerRegistryOverrideWithFallback("profession", Registries.VILLAGER_PROFESSION, _ -> null, (s, value) -> {
                Holder<VillagerType> type = s.villagerData != null ? s.villagerData.type() : RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, VillagerType.PLAINS);
                Holder<VillagerProfession> profession = RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, value);
                int level = s.villagerData != null ? s.villagerData.level() : 1;
                s.villagerData = new VillagerData(type, profession, level);
            }, (key, _) -> OptionalOverride.toDisplayName(key.identifier().getPath()), VillagerProfession.NONE, false);

            overrides.registerIntOverride("level", s -> s.villagerData == null ? 1 : s.villagerData.level(), (s, value) -> {
                Holder<VillagerType> type = s.villagerData != null ? s.villagerData.type() : RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, VillagerType.PLAINS);
                Holder<VillagerProfession> profession = s.villagerData != null ? s.villagerData.profession() : RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, VillagerProfession.NONE);
                s.villagerData = new VillagerData(type, profession, value);
            });
        });

        registerOverrides(WardenRenderState.class, overrides -> {
            overrides.registerFloatOverride("tendrilAnimation", s -> s.tendrilAnimation, (s, value) -> s.tendrilAnimation = value);
            overrides.registerFloatOverride("heartAnimation", s -> s.heartAnimation, (s, value) -> s.heartAnimation = value);
            overrides.registerAnimationStateOverride("roarAnimationState", s -> s.roarAnimationState);
            overrides.registerAnimationStateOverride("sniffAnimationState", s -> s.sniffAnimationState);
            overrides.registerAnimationStateOverride("emergeAnimationState", s -> s.emergeAnimationState);
            overrides.registerAnimationStateOverride("diggingAnimationState", s -> s.diggingAnimationState);
            overrides.registerAnimationStateOverride("attackAnimationState", s -> s.attackAnimationState);
            overrides.registerAnimationStateOverride("sonicBoomAnimationState", s -> s.sonicBoomAnimationState);
        });

        registerOverrides(WitchRenderState.class, overrides -> {
            overrides.registerIntOverride("entityId", s -> s.entityId, (s, value) -> s.entityId = value);
            overrides.registerBooleanOverride("isHoldingItem", s -> s.isHoldingItem, (s, value) -> s.isHoldingItem = value);
            overrides.registerBooleanOverride("isHoldingPotion", s -> s.isHoldingPotion, (s, value) -> s.isHoldingPotion = value);
        });

        registerOverrides(WitherRenderState.class, overrides -> {
            overrides.registerFloatOverride("xHeadRot 1", s -> s.xHeadRots[0], (s, value) -> s.xHeadRots[0] = value);
            overrides.registerFloatOverride("xHeadRot 2", s -> s.xHeadRots[1], (s, value) -> s.xHeadRots[1] = value);
            overrides.registerFloatOverride("yHeadRot 1", s -> s.yHeadRots[0], (s, value) -> s.yHeadRots[0] = value);
            overrides.registerFloatOverride("yHeadRot 2", s -> s.yHeadRots[1], (s, value) -> s.yHeadRots[1] = value);
            overrides.registerFloatOverride("invulnerableTicks", s -> s.invulnerableTicks, (s, value) -> s.invulnerableTicks = value);
            overrides.registerBooleanOverride("isPowered", s -> s.isPowered, (s, value) -> s.isPowered = value);
        });

        registerOverrides(WitherSkullRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isDangerous", s -> s.isDangerous, (s, value) -> s.isDangerous = value);
            overrides.registerFloatOverride("animationPosition", s -> s.modelState.animationPos, (s, value) -> s.modelState.animationPos = value);
            overrides.registerFloatOverride("xHeadRotation", s -> s.modelState.xRot, (s, value) -> s.modelState.xRot = value);
            overrides.registerFloatOverride("yHeadRotation", s -> s.modelState.yRot, (s, value) -> s.modelState.yRot = value);
        });

        registerOverrides(WolfRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isAngry", s -> s.isAngry, (s, value) -> s.isAngry = value);
            overrides.registerBooleanOverride("isSitting", s -> s.isSitting, (s, value) -> s.isSitting = value);
            overrides.registerFloatOverride("tailAngle", s -> s.tailAngle, (s, value) -> s.tailAngle = value, (float) (Math.PI / 5));
            overrides.registerFloatOverride("headRollAngle", s -> s.headRollAngle, (s, value) -> s.headRollAngle = value);
            overrides.registerFloatOverride("shakeAnim", s -> s.shakeAnim, (s, value) -> s.shakeAnim = value);
            overrides.registerFloatOverride("wetShade", s -> s.wetShade, (s, value) -> s.wetShade = value, 1);
            // texture
            overrides.registerEnumOverride("collarColor", DyeColor.class, s -> s.collarColor, (s, value) -> s.collarColor = value);
            overrides.registerBooleanOverride("hasWolfArmor", state -> !state.bodyArmorItem.isEmpty(), (state, value) -> {
                if (value) {
                    state.bodyArmorItem = new ItemStack(Items.WOLF_ARMOR);
                } else {
                    state.bodyArmorItem = ItemStack.EMPTY;
                }
            });
            overrides.registerFloatOverride("ageScale", s -> s.ageScale, (s, value) -> s.ageScale = value, 1f);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            overrides.registerRegistryOverrideWithFallback("variant", Registries.WOLF_VARIANT, _ -> null, (s, value) -> {
                // extremely jank way of figuring out the non-applied variant
                if (value == null) {
                    for (Holder.Reference<WolfVariant> variant : RegistryOverride.getHolderValues(Registries.WOLF_VARIANT)) {
                        WolfVariant possibleVariant = variant.value();

                        if (Objects.equals(s.texture, possibleVariant.babyInfo().tame().texturePath())
                            || Objects.equals(s.texture, possibleVariant.babyInfo().angry().texturePath())
                            || Objects.equals(s.texture, possibleVariant.babyInfo().wild().texturePath())
                            || Objects.equals(s.texture, possibleVariant.adultInfo().tame().texturePath())
                            || Objects.equals(s.texture, possibleVariant.adultInfo().angry().texturePath())
                            || Objects.equals(s.texture, possibleVariant.adultInfo().wild().texturePath())) {
                            value = possibleVariant;
                            break;
                        }
                    }
                }
                if (value == null) {
                    value = RegistryOverride.getHolderValue(Registries.WOLF_VARIANT, WolfVariants.DEFAULT).value();
                }

                WolfVariant.AssetInfo assetInfo = s.isBaby ? value.babyInfo() : value.adultInfo();
                if (s.collarColor != null) {
                    s.texture = assetInfo.tame().texturePath();
                } else {
                    s.texture = s.isAngry ? assetInfo.angry().texturePath() : assetInfo.wild().texturePath();
                }
            }, (_, s) -> {
                String id = s.adultInfo().tame().id().toString();
                return OptionalOverride.toDisplayName(id.substring(id.lastIndexOf("/") + 1).replace("_tame", ""));
            }, null, true);

        });

        registerOverrides(ZombieRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isAggressive", s -> s.isAggressive, (s, value) -> s.isAggressive = value);
            if (!(overrides.renderer instanceof GiantMobRenderer)) {
                overrides.registerBooleanOverride("isConverting", s -> s.isConverting, (s, value) -> s.isConverting = value);
                overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
            }
        });

        // zombie villager
        registerOverrides(ZombieVillagerRenderState.class, overrides -> {
            overrides.registerRegistryOverrideWithFallback("type", Registries.VILLAGER_TYPE, _ -> null, (s, value) -> {
                Holder<VillagerType> type = RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, value);
                Holder<VillagerProfession> profession = s.villagerData != null ? s.villagerData.profession() : RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, VillagerProfession.NONE);
                int level = s.villagerData != null ? s.villagerData.level() : 1;
                s.villagerData = new VillagerData(type, profession, level);
            }, (key, _) -> OptionalOverride.toDisplayName(key.identifier().getPath()), VillagerType.PLAINS, false);

            overrides.registerRegistryOverrideWithFallback("profession", Registries.VILLAGER_PROFESSION, _ -> null, (s, value) -> {
                Holder<VillagerType> type = s.villagerData != null ? s.villagerData.type() : RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, VillagerType.PLAINS);
                Holder<VillagerProfession> profession = RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, value);
                int level = s.villagerData != null ? s.villagerData.level() : 1;
                s.villagerData = new VillagerData(type, profession, level);
            }, (key, _) -> OptionalOverride.toDisplayName(key.identifier().getPath()), VillagerProfession.NONE, false);

            overrides.registerIntOverride("level", s -> s.villagerData == null ? 1 : s.villagerData.level(), (s, value) -> {
                Holder<VillagerType> type = s.villagerData != null ? s.villagerData.type() : RegistryOverride.getHolderValue(Registries.VILLAGER_TYPE, VillagerType.PLAINS);
                Holder<VillagerProfession> profession = s.villagerData != null ? s.villagerData.profession() : RegistryOverride.getHolderValue(Registries.VILLAGER_PROFESSION, VillagerProfession.NONE);
                s.villagerData = new VillagerData(type, profession, value);
            });
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });

        registerOverrides(ZombifiedPiglinRenderState.class, overrides -> {
            overrides.registerBooleanOverride("isAggressive", s -> s.isAggressive, (s, value) -> s.isAggressive = value);
            overrides.registerBooleanOverride("isBaby", s -> s.isBaby, (s, value) -> s.isBaby = value);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends EntityRenderState> void registerOverrides(Class<T> clazz, Consumer<EntityTypeSpecificOverrides<T>> overrides) {
        REGISTERED_OVERRIDES.put(clazz, (Consumer<EntityTypeSpecificOverrides<?>>) (Consumer<?>) overrides);
    }

    @Nullable
    public static <S extends EntityRenderState> EntityTypeSpecificOverrides<S> getOverrides(S renderState) {
        EntityTypeSpecificOverrides<S> overrides = new EntityTypeSpecificOverrides<>(renderState);

        boolean overridesApplied = false;

        for (Class<?> clazz = renderState.getClass(); EntityRenderState.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            Consumer<EntityTypeSpecificOverrides<?>> overrideRegistry = REGISTERED_OVERRIDES.get(clazz);
            if (overrideRegistry != null) {
                overridesApplied = true;
                overrideRegistry.accept(overrides);
            }
        }

        overrides.copyFromRenderState(renderState);
        return overridesApplied ? overrides : null;
    }

    private final BooleanOverride<S> invisible;
    private final EntityRenderer<?, ?> renderer;
    private final Class<S> stateClass;

    public EntityTypeSpecificOverrides(S renderState) {
        // noinspection unchecked
        this.stateClass = (Class<S>) renderState.getClass();
        this.invisible = new BooleanOverride<>("dontRender");
        this.registerOverride("visibility", "dontRender", invisible);
        this.renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(renderState);
    }

    private final Map<String, Map<String, OptionalOverride<S, ?>>> overrides = new LinkedHashMap<>();

    public void applyOverridesIfPossible(EntityRenderState possibleRenderState) {
        if (stateClass.isInstance(possibleRenderState)) {
            S renderState = stateClass.cast(possibleRenderState);
            applyOverrides(renderState);
        }
    }

    public void applyOverrides(S state) {
        getOverrides().forEach(override -> override.apply(state));
    }

    public Collection<OptionalOverride<S, ?>> getOverrides() {
        Set<OptionalOverride<S, ?>> overrides = new LinkedHashSet<>();
        for (Map<String, OptionalOverride<S, ?>> overridesCategory : this.overrides.values()) {
            overrides.addAll(overridesCategory.values());
        }
        return overrides;
    }

    public void copyFromRenderState(S renderState) {
        for (OptionalOverride<S, ?> override : getOverrides()) {
            override.copyFromRenderState(renderState);
        }
    }

    public boolean isInvisible() {
        return invisible.getValue();
    }

    protected void registerOverride(String category, String key, OptionalOverride<S, ?> override) {
        overrides.computeIfAbsent(category, o -> new LinkedHashMap<>()).put(key, override);
    }

    protected void registerFloatOverride(String key, Function<S, Float> getter, BiConsumer<S, Float> setter) {
        registerFloatOverrideInCategory("main", key, getter, setter);
    }

    protected void registerFloatOverride(String key, Function<S, Float> getter, BiConsumer<S, Float> setter, float defaultValue) {
        this.registerOverride("main", key, new FloatOverride<>(key, getter, setter, defaultValue));
    }

    protected void registerFloatOverrideInCategory(String category, String key, Function<S, Float> getter, BiConsumer<S, Float> setter) {
        this.registerOverride(category, key, new FloatOverride<>(key, getter, setter));
    }

    protected void registerFloatOverrideInCategory(String category, String key, Function<S, Float> getter, BiConsumer<S, Float> setter, float defaultValue) {
        this.registerOverride(category, key, new FloatOverride<>(key, getter, setter, defaultValue));
    }

    protected void registerIntOverride(String key, Function<S, Integer> getter, BiConsumer<S, Integer> setter) {
        this.registerOverride("main", key, new IntegerOverride<>(key, getter, setter));
    }

    protected void registerIntOverrideInCategory(String category, String key, Function<S, Integer> getter, BiConsumer<S, Integer> setter) {
        this.registerOverride(category, key, new IntegerOverride<>(key, getter, setter));
    }

    protected void registerIntOverride(String key, Function<S, Integer> getter, BiConsumer<S, Integer> setter, int defaultValue) {
        this.registerOverride("main", key, new IntegerOverride<>(key, getter, setter, defaultValue));
    }

    protected void registerBooleanOverride(String key, Function<S, Boolean> getter, BiConsumer<S, Boolean> setter) {
        this.registerOverride("main", key, new BooleanOverride<>(key, getter, setter));
    }

    protected void registerBooleanOverrideInCategory(String category, String key, Function<S, Boolean> getter, BiConsumer<S, Boolean> setter) {
        this.registerOverride(category, key, new BooleanOverride<>(key, getter, setter));
    }

    protected <R> void registerRegistryOverride(String key, ResourceKey<? extends Registry<? extends R>> registryKey, Function<S, R> getter, BiConsumer<S, R> setter, BiFunction<ResourceKey<R>, R, String> toString) {
        this.registerOverride("main", key, new RegistryOverride<>(key, registryKey, getter, setter, toString, null, false));
    }

    protected <R> void registerRegistryOverrideWithFallback(String key, ResourceKey<? extends Registry<? extends R>> registryKey, Function<S, R> getter, BiConsumer<S, R> setter, BiFunction<ResourceKey<R>, R, String> toString, ResourceKey<R> defaultValue, boolean forcedFallback) {
        this.registerOverride("main", key, new RegistryOverride<>(key, registryKey, getter, setter, toString, defaultValue, forcedFallback));
    }

    protected void registerDoubleOverride(String key, Function<S, Double> getter, BiConsumer<S, Double> setter) {
        this.registerOverride("main", key, new DoubleOverride<>(key, getter, setter));
    }

    protected <E extends Enum<E>> void registerEnumOverride(String key, Class<E> enumClass, Function<S, E> getter, BiConsumer<S, @Nullable E> setter) {
        this.registerOverride("main", key, new EnumOverride<>(key, enumClass, getter, setter));
    }

    protected <E extends Enum<E>> void registerEnumOverrideInCategory(String category, String key, Class<E> enumClass, Function<S, E> getter, BiConsumer<S, @Nullable E> setter) {
        this.registerOverride(category, key, new EnumOverride<>(key, enumClass, getter, setter));
    }

    protected void registerItemStackOverride(String key, Function<S, ItemStack> getter, BiConsumer<S, ItemStack> setter) {
        this.registerOverride("main", key, new ItemStackOverride<>(key, getter, setter));
    }

    protected void registerItemStackOverrideInCategory(String category, String key, Function<S, ItemStack> getter, BiConsumer<S, ItemStack> setter) {
        this.registerOverride(category, key, new ItemStackOverride<>(key, getter, setter));
    }

    protected void registerBlockStateOverride(String key, Function<S, BlockState> getter, BiConsumer<S, BlockState> setter) {
        this.registerOverride("main", key, new BlockStateOverride<>(key, getter, setter));
    }

    protected void registerAnimationStateOverride(String key, Function<S, AnimationState> getter) {
        registerIntOverride(key, _ -> 0, (s, value) -> getter.apply(s).start(value));
    }

    protected void registerRotationsOverrides(String key, Function<S, Rotations> getter, BiConsumer<S, Rotations> setter) {
        registerFloatOverride(key + "X", state -> getter.apply(state).x(), (state, value) -> {
            Rotations current = getter.apply(state);
            setter.accept(state, new Rotations(value, current.y(), current.z()));
        });
        registerFloatOverride(key + "Y", state -> getter.apply(state).y(), (state, value) -> {
            Rotations current = getter.apply(state);
            setter.accept(state, new Rotations(value, current.y(), current.z()));
        });
        registerFloatOverride(key + "Z", state -> getter.apply(state).z(), (state, value) -> {
            Rotations current = getter.apply(state);
            setter.accept(state, new Rotations(value, current.y(), current.z()));
        });
    }

    public boolean hasLayerType(Predicate<RenderLayer<?, ?>> predicate) {
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
            List<RenderLayer<?, ?>> layers = ((LivingEntityRendererAccessor) livingEntityRenderer).wikirenderer$getLayers();
            for (RenderLayer<?, ?> layer : layers) {
                if (predicate.test(layer)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasModelType(Predicate<EntityModel<?>> predicate) {
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
            EntityModel<?> model = livingEntityRenderer.getModel();
            return predicate.test(model);
        } else {
            return false;
        }
    }

    public boolean hasModelPart(BiPredicate<String, ModelPart> predicate) {
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
            EntityModel<?> model = livingEntityRenderer.getModel();
            ModelPart rootPart = model.root();
            return hasModelPart(rootPart, predicate);
        } else {
            return false;
        }
    }

    public boolean hasModelPart(ModelPart rootPart, BiPredicate<String, ModelPart> predicate) {
        Map<String, ModelPart> childParts = ((ModelPartAccessor) (Object) rootPart).wikirenderer$getChildren();
        AtomicBoolean value = new AtomicBoolean(false);

        childParts.forEach((identifier, modelPart) -> {
            if (predicate.test(identifier, modelPart)) {
                value.set(true);
            } else if (hasModelPart(modelPart, predicate)) {
                value.set(true);
            }
        });
        return value.get();
    }
}

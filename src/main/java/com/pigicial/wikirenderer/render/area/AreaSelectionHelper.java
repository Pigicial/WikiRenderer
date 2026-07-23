package com.pigicial.wikirenderer.render.area;

import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.render.entity.EntityRenderBoundsUtil;
import com.pigicial.wikirenderer.render.entity.EntityVertexBounds;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.ScreenSchedulerAndSaver;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AreaSelectionHelper {

    public static BlockPos pos1 = null;
    public static BlockPos pos2 = null;

    public static boolean shouldDrawOverlay() {
        return pos1 != null && !(Minecraft.getInstance().gui.screen() instanceof RenderScreen);
    }

    public static boolean shouldDrawBox() {
        return pos1 != null;
    }

    public static void clear() {
        AreaSelectionHelper.pos1 = null;
        AreaSelectionHelper.pos2 = null;
        Translate.actionBar("selection_cleared");
    }

    public static void renderSelectionBox() {
        if (!AreaSelectionHelper.shouldDrawBox()) return;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        BlockPos pos1 = AreaSelectionHelper.pos1;

        HitResult result = player.pick(player.getAbilities().instabuild ? 5.0F : 4.5F, 0, false);
        BlockPos pos2 = AreaSelectionHelper.pos2 != null ? AreaSelectionHelper.pos2 : (result.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) result).getBlockPos() : BlockPos.containing(result.getLocation()));

        // recalibrate to make math easier
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        pos1 = new BlockPos(minX, minY, minZ);
        pos2 = new BlockPos(maxX + 1, maxY + 1, maxZ + 1);

        AABB areaBounds = new AABB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
        Gizmos.cuboid(areaBounds, GizmoStyle.stroke(ARGB.colorFromFloat(1, 1, 1, 1f), 5), true);

        if (GlobalProperties.get().debugShowCollidingEntityBoundsForAreas.get()) {
            Level level = client.level;
            if (level == null) return;

            AABB areaEntityBounds = new AABB(pos1.getX() - 8, pos1.getY() - 8, pos1.getZ() - 8, pos2.getX() + 9, pos2.getY() + 9, pos2.getZ() + 9);
            for (Entity entity : level.getEntities((Entity) null, areaEntityBounds, e -> true)) {
                if (entity instanceof EnderDragonPart) continue; // crash fix

                EntityVertexBounds entityBounds = EntityRenderBoundsUtil.getPositionOffsetBasedBounds(entity);
                if (entityBounds != null && entityBounds.getBounds().intersects(areaBounds)) {
                    for (AABB entitySubBound : entityBounds.getClipBounds()) {
                        Gizmos.cuboid(entitySubBound, GizmoStyle.stroke(ARGB.colorFromFloat(1, 1, 1, 0.6f), 3), true);
                    }
                }
            }
        }

    }

    public static void select() {
        HitResult target = Minecraft.getInstance().hitResult;
        if (target == null) {
            return;
        }

        BlockPos targetPos = target.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) target).getBlockPos() : BlockPos.containing(target.getLocation());

        if (pos1 == null) {
            pos1 = targetPos;
            Translate.actionBar("selection_started");
        } else {
            Translate.actionBar("selection_finished");
            pos2 = targetPos;
        }
    }

    public static void expand() {
        HitResult target = Minecraft.getInstance().hitResult;
        if (target == null) {
            return;
        }

        BlockPos targetPos = target.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) target).getBlockPos() : BlockPos.containing(target.getLocation());

        if (pos1 == null) {
            pos1 = targetPos;
            Translate.actionBar("selection_started");
        } else {
            if (pos2 == null) {
                Translate.actionBar("selection_finished");
                pos2 = targetPos;
            } else {
                Translate.actionBar("selection_expanded");

                int minX = Math.min(Math.min(pos1.getX(), pos2.getX()), targetPos.getX());
                int maxX = Math.max(Math.max(pos1.getX(), pos2.getX()), targetPos.getX());
                int minY = Math.min(Math.min(pos1.getY(), pos2.getY()), targetPos.getY());
                int maxY = Math.max(Math.max(pos1.getY(), pos2.getY()), targetPos.getY());
                int minZ = Math.min(Math.min(pos1.getZ(), pos2.getZ()), targetPos.getZ());
                int maxZ = Math.max(Math.max(pos1.getZ(), pos2.getZ()), targetPos.getZ());
                pos1 = new BlockPos(minX, minY, minZ);
                pos2 = new BlockPos(maxX, maxY, maxZ);
            }
        }
    }

    public static boolean tryOpenScreen() {
        if (pos1 == null || pos2 == null) return false;

        ScreenSchedulerAndSaver.schedule(new RenderScreen(AreaRenderable.of(pos1, pos2)));
        return true;
    }
}

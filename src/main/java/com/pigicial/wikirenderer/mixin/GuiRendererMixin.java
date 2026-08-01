package com.pigicial.wikirenderer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.mixin.screen.StagedVertexBufferDrawAccessor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// note: disabling this breaks tooltip rendering, maybe more idk
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;mainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget overrideRenderFramebuffer(GameRenderer instance, Operation<RenderTarget> original) {
        if (WikiRenderer.mainTargetOverride != null) return WikiRenderer.mainTargetOverride;
        return original.call(instance);
    }

    // If it works, it's not stupid.
    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicGpuData;writeTransform(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
    private GpuBufferSlice overrideDynamicTransforms(DynamicGpuData instance, Matrix4f modelView, Operation<GpuBufferSlice> original) {
        if (WikiRenderer.inRenderableDraw) {
            return original.call(instance, RenderSystem.getModelViewMatrixCopy());
        } else {
            return original.call(instance, modelView);
        }
    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"))
    private void cancelProjectionSet(GpuBufferSlice projectionMatrixBuffer, ProjectionType projectionType, Operation<Void> original) {
        // Something else may have overridden the projection matrix by this point, restore the original one used for the renderable.
        if (WikiRenderer.inRenderableDraw) {
            original.call(WikiRenderer.renderableDrawProjectionBuffer, ProjectionType.ORTHOGRAPHIC);
        } else {
            original.call(projectionMatrixBuffer, projectionType);
        }
    }

    @ModifyArgs(
            method = "executeDraw",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setIndexBuffer(Lcom/mojang/renderpearl/api/buffers/GpuBuffer;Lcom/mojang/renderpearl/api/pipeline/IndexType;)V")
    )
    private void fixNonQuadIndexing(Args args, @Local(argsOnly = true, name = "draw") GuiRenderer.Draw draw) {
        var pipeline = draw.pipeline();
        if (!pipeline.getLocation().getNamespace().equals("owo")) return;

        if (pipeline.getPrimitiveTopology() != PrimitiveTopology.QUADS) {
            var shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getPrimitiveTopology());
            args.set(0, shapeIndexBuffer.getBuffer(((StagedVertexBufferDrawAccessor) draw.draw()).owo$getIndexCount()));
            args.set(1, shapeIndexBuffer.type());
        }
    }
}
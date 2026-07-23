package com.pigicial.wikirenderer.mixin;

import com.mojang.blaze3d.platform.Window;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.event.ClientRenderCallback;
import com.pigicial.wikirenderer.screen.owo.event.WindowResizeCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Final
    @Shadow
    public Gui gui;

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceGameTime(J)I"))
    private void onRenderStart(boolean tick, CallbackInfo ci) {
        if (gui.screen() instanceof RenderScreen renderScreen) {
            renderScreen.renderable.getProperties().onRenderStart();
        }
    }

    @Shadow
    @Final
    private Window window;

    @Inject(method = "resizeGui", at = @At("TAIL"))
    private void captureResize(CallbackInfo ci) {
        WindowResizeCallback.EVENT.invoker().onResized((Minecraft) (Object) this, this.window);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setErrorSection(Ljava/lang/String;)V", ordinal = 1))
    private void beforeRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;present()V", shift = At.Shift.AFTER))
    private void afterRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.AFTER.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;frameTimeNs:J", opcode = Opcodes.PUTFIELD))
    private void beforeSwap(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE_SWAP.invoker().onRender((Minecraft) (Object) this);
    }
}

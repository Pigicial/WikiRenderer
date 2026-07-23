package com.pigicial.wikirenderer.mixin;

import com.pigicial.wikirenderer.screen.ScreenSchedulerAndSaver;
import com.pigicial.wikirenderer.screen.owo.base.BaseOwoScreen;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void openScheduled(Screen screen, CallbackInfo ci) {
        if (screen != null || !ScreenSchedulerAndSaver.hasScheduled()) return;

        ScreenSchedulerAndSaver.openScheduledScreen();
        ci.cancel();
    }

    @Unique
    private final Set<BaseOwoScreen> screensToDispose = new HashSet<>();

    @Shadow
    @Nullable
    private Screen screen;

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private void captureSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && this.screen instanceof BaseOwoScreen disposable) {
            this.screensToDispose.add(disposable);
        } else if (screen == null) {
            if (this.screen instanceof BaseOwoScreen disposable) {
                this.screensToDispose.add(disposable);
            }

            for (var disposable : this.screensToDispose) {
                try {
                    disposable.dispose();
                } catch (Throwable error) {
                    var report = new CrashReport("Failed to dispose screen", error);
                    report.addCategory("Screen being disposed: ")
                            .setDetail("Screen class", disposable.getClass())
                            .setDetail("Screen being closed", this.screen)
                            .setDetail("Total screens to dispose", this.screensToDispose.size());

                    throw new ReportedException(report);
                }
            }

            this.screensToDispose.clear();
        }
    }
}

package com.pigicial.wikirenderer.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public class ScreenSchedulerAndSaver {

    private static RenderScreen SCHEDULED_SCREEN = null;
    private static RenderScreen SAVED_SCREEN = null;
    @Nullable
    private static AbstractContainerScreen<?> SKIP_REMOVED_FOR_CONTAINER = null;

    public static void schedule(RenderScreen screen) {
        if (Minecraft.getInstance().screen == null) {
            SCHEDULED_SCREEN = screen;
            openScheduledScreen();
        } else {
            SCHEDULED_SCREEN = screen;
        }
    }

    public static void setSavedScreen(RenderScreen screen) {
        SAVED_SCREEN = screen;
    }

    public static RenderScreen getSavedScreen() {
        return SAVED_SCREEN;
    }

    public static void openImmediately(RenderScreen screen) {
        SCHEDULED_SCREEN = screen;
        openScheduledScreen();
    }

    public static <T extends Screen & ContainerPreservingScreen> void openPreservingCurrentContainer(T screen) {
        openScreenPreservingCurrentContainer(screen);
    }

    public static boolean hasScheduled() {
        return SCHEDULED_SCREEN != null;
    }

    public static RenderScreen getScheduledScreen() {
        return SCHEDULED_SCREEN;
    }

    public static void openScheduledScreen() {
        if (SCHEDULED_SCREEN == null) return;

        if (SAVED_SCREEN != null && SAVED_SCREEN != SCHEDULED_SCREEN) {
            SAVED_SCREEN.removed();
            SAVED_SCREEN = null;
        }

        openScreenPreservingCurrentContainer(SCHEDULED_SCREEN);
        SCHEDULED_SCREEN = null;
    }

    private static <T extends Screen & ContainerPreservingScreen> void openScreenPreservingCurrentContainer(T screen) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AbstractContainerScreen<?> containerScreen) {
            screen.setPreviouslyOpenedContainerScreen(containerScreen);
            SKIP_REMOVED_FOR_CONTAINER = containerScreen;
        } else if (currentScreen instanceof ContainerPreservingScreen containerPreservingScreen) {
            screen.setPreviouslyOpenedContainerScreen(containerPreservingScreen.getPreviouslyOpenedContainerScreen());
        }

        Minecraft.getInstance().setScreen(screen);
    }

    public static boolean shouldSkipRemoved(Screen screen) {
        if (screen != SKIP_REMOVED_FOR_CONTAINER) return false;

        SKIP_REMOVED_FOR_CONTAINER = null;
        return true;
    }

    public static boolean restorePreviouslyOpenedContainer(ContainerPreservingScreen screen) {
        AbstractContainerScreen<?> containerScreen = screen.getPreviouslyOpenedContainerScreen();
        screen.setPreviouslyOpenedContainerScreen(null);

        if (!isValidPreviouslyOpenedContainer(containerScreen)) {
            return false;
        }

        Minecraft.getInstance().setScreen(containerScreen);
        return true;
    }

    private static boolean isValidPreviouslyOpenedContainer(@Nullable AbstractContainerScreen<?> containerScreen) {
        Minecraft client = Minecraft.getInstance();
        return containerScreen != null
            && client.player != null
            && client.player.containerMenu == containerScreen.getMenu();
    }

}

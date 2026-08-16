package com.pigicial.wikirenderer.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public interface ContainerPreservingScreen {

    void setPreviouslyOpenedContainerScreen(@Nullable AbstractContainerScreen<?> screen);

    @Nullable
    AbstractContainerScreen<?> getPreviouslyOpenedContainerScreen();

}

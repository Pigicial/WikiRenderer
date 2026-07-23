package com.pigicial.wikirenderer.mixin.screen;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("bordered")
    boolean owo$bordered();

    @Invoker("updateTextPosition")
    void owo$updateTextPosition();
}

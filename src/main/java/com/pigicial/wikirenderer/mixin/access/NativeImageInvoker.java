package com.pigicial.wikirenderer.mixin.access;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.channels.WritableByteChannel;

@Mixin(NativeImage.class)
public interface NativeImageInvoker {

    @Invoker("writeToChannel")
    boolean wikirenderer$writeToChannel(WritableByteChannel channel);

    @Invoker("checkAllocated")
    void wikirenderer$checkAllocated();

}
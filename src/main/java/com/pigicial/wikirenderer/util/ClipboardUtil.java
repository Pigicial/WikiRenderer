package com.pigicial.wikirenderer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.pigicial.wikirenderer.mixin.access.NativeImageInvoker;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class ClipboardUtil {

    public static void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    public static void setClipboard(NativeImage nativeImage) throws IOException {
        byte[] bytes = encodeImageForClipboardUsage(nativeImage);
        String mimeType = "image/png";

        int dataSize = bytes.length;
        long dataPointer = MemoryUtil.nmemAlloc(dataSize);
        if (dataPointer == 0L) return;

        ByteBuffer nativeBuffer = MemoryUtil.memByteBuffer(dataPointer, dataSize);
        nativeBuffer.put(bytes);

        long userdata = MemoryUtil.nmemAlloc(16);
        if (userdata == 0L) {
            MemoryUtil.nmemFree(dataPointer);
            return;
        }
        MemoryUtil.memPutLong(userdata, dataPointer);
        MemoryUtil.memPutLong(userdata + 8, dataSize);

        final SDL_ClipboardDataCallback dataCallback = SDL_ClipboardDataCallback.create((user, mime_type, size) -> {
            String requestedMime = MemoryUtil.memASCII(mime_type);

            if (requestedMime.equalsIgnoreCase(mimeType)) {
                long pointer = MemoryUtil.memGetLong(user);
                long length = MemoryUtil.memGetLong(user + 8);

                MemoryUtil.memPutAddress(size, length);
                return pointer;
            }

            MemoryUtil.memPutAddress(size, 0);
            return MemoryUtil.NULL;
        });

        SDL_ClipboardCleanupCallback[] cleanupHolder = new SDL_ClipboardCleanupCallback[1];
        cleanupHolder[0] = SDL_ClipboardCleanupCallback.create(user -> {
            long pointer = MemoryUtil.memGetLong(user);
            if (pointer != 0L) {
                MemoryUtil.nmemFree(pointer);
            }

            MemoryUtil.nmemFree(user);
            dataCallback.free();
            if (cleanupHolder[0] != null) {
                cleanupHolder[0].free();
            }
        });

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer mimeTypes = stack.mallocPointer(1);
            mimeTypes.put(0, stack.ASCII(mimeType));

            SDLClipboard.SDL_SetClipboardData(
                    dataCallback,
                    cleanupHolder[0],
                    userdata,
                    mimeTypes
            );
        }
    }

    public static byte[] encodeImageForClipboardUsage(@NotNull NativeImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (WritableByteChannel channel = Channels.newChannel(out)) {
            ((NativeImageInvoker) (Object) image).wikirenderer$checkAllocated();
            ((NativeImageInvoker) (Object) image).wikirenderer$writeToChannel(channel);
            return out.toByteArray();
        }
    }
}

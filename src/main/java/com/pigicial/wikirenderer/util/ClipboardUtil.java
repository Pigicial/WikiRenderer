package com.pigicial.wikirenderer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spng.SPNG;
import org.lwjgl.util.spng.spng_ihdr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
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

    // Based on NativeImage#writeToFile
    public static byte[] encodeImageForClipboardUsage(NativeImage image) throws IOException {
        image.checkAllocated();
        long context = SPNG.spng_ctx_new(2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (
                WritableByteChannel channel = Channels.newChannel(out);
                Arena arena = Arena.ofConfined();
                MemoryStack stack = MemoryStack.stackPush();
        ) {
            int width = image.getWidth();
            int height = Math.min(image.getHeight(), Integer.MAX_VALUE / width / image.format().components());
            if (height < image.getHeight()) {
                WikiRenderer.LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", image.getHeight(), height);
            }

            NativeImage.WriteCallback writer = new NativeImage.WriteCallback(channel);
            MemorySegment writerUpcall = writer.createUpcall(arena);
            NativeImage.checkSpngError("set output", SPNG.nspng_set_png_stream(context, writerUpcall.address(), 0L));
            spng_ihdr header = spng_ihdr.calloc(stack).width(width).height(height).color_type((byte)image.format().pngColorType).bit_depth((byte)8);
            NativeImage.checkSpngError("set header", SPNG.spng_set_ihdr(context, header));
            NativeImage.checkSpngError("write image", SPNG.nspng_encode_image(context, image.getPointer(), image.size, 256, 2));
            writer.throwIfException();

            return out.toByteArray();
        } catch (IOException e) {
            throw new IOException("Could not write image to the byte array", e);
        } finally {
            SPNG.spng_ctx_free(context);
        }
    }
}

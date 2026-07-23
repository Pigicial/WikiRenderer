package com.pigicial.wikirenderer.screen.owo.util;

import com.pigicial.wikirenderer.screen.owo.core.CursorStyle;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLMouse;

import java.util.EnumMap;

public class CursorAdapter {

    protected static final CursorStyle[] ACTIVE_STYLES = {CursorStyle.POINTER, CursorStyle.TEXT, CursorStyle.HAND, CursorStyle.CROSSHAIR, CursorStyle.MOVE, CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE, CursorStyle.NWSE_RESIZE, CursorStyle.NESW_RESIZE, CursorStyle.NOT_ALLOWED};

    protected final EnumMap<CursorStyle, Long> cursors = new EnumMap<>(CursorStyle.class);
    protected final long windowHandle;

    protected CursorStyle lastCursorStyle = CursorStyle.POINTER;
    protected boolean disposed = false;

    protected CursorAdapter(long windowHandle) {
        this.windowHandle = windowHandle;
        for (var style : ACTIVE_STYLES) {
            var pointer = SDLMouse.SDL_CreateSystemCursor(style.glfw);
            if (pointer == 0) continue;

            this.cursors.put(style, pointer);
        }
    }

    public static CursorAdapter ofClientWindow() {
        return new CursorAdapter(Minecraft.getInstance().getWindow().handle());
    }

    public void applyStyle(CursorStyle style) {
        if (this.disposed || this.lastCursorStyle == style) return;

        if (style == CursorStyle.NONE) {
            SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor());
        } else {
            long handle = this.cursors.getOrDefault(style, 0L);
            long cursor = handle == 0L ? SDLMouse.SDL_GetDefaultCursor() : handle;
            SDLMouse.SDL_SetCursor(cursor);
        }
        this.lastCursorStyle = style;
    }

    public void dispose() {
        if (this.disposed) return;

        this.cursors.values().forEach(SDLMouse::SDL_DestroyCursor);
        this.disposed = true;
    }

}

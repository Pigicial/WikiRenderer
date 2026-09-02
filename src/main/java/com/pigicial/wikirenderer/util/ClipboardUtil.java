package com.pigicial.wikirenderer.util;

import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardUtil {
    private static final boolean HAS_TEXT_CLIPBOARD;
    private static final boolean IS_SYSTEM_MAC;

    static {
        boolean hasClipboard;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard();
            hasClipboard = true;
        } catch (HeadlessException e) {
            hasClipboard = false;
        }
        HAS_TEXT_CLIPBOARD = hasClipboard;
        IS_SYSTEM_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean hasTextClipboardAccess() {
        return HAS_TEXT_CLIPBOARD;
    }

    public static boolean hasImageClipboardAccess() {
        return !GraphicsEnvironment.isHeadless() && !IS_SYSTEM_MAC;
    }

    public static void setClipboard(String text) {
        if (HAS_TEXT_CLIPBOARD) {
            System.out.println("Hi 2.");
            StringSelection selection = new StringSelection(text);
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(selection, selection);

            try {
                System.out.println("systemClipboard.getContents(null) = " + systemClipboard.getContents(null).getTransferData(DataFlavor.stringFlavor));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setClipboard(ImageTransferable imageTransferable) {
        if (hasImageClipboardAccess()) {
            System.out.println("Hi.");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageTransferable, imageTransferable);
        }
    }
}

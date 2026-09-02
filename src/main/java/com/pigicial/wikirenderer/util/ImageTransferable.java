package com.pigicial.wikirenderer.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.*;

public class ImageTransferable implements Transferable, ClipboardOwner {

    private final Image image;

    public ImageTransferable(Image image) {
        this.image = image;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        boolean equals = flavor.equals(DataFlavor.imageFlavor);
        System.out.println("It is " + equals);
        return equals;
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!this.isDataFlavorSupported(flavor)) {
            System.out.println("Ruh roh");
            throw new UnsupportedFlavorException(flavor);
        }

        return image;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {}
}

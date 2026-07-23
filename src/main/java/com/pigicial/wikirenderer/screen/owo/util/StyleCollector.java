package com.pigicial.wikirenderer.screen.owo.util;

import com.pigicial.wikirenderer.mixin.screen.ClickableStyleFinderAccessor;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;

public class StyleCollector extends ActiveTextCollector.ClickableStyleFinder {

    public StyleCollector(Font font, int clickX, int clickY) {
        super(font, clickX, clickY);
        ((ClickableStyleFinderAccessor) this).owo$setStyleScanner(((ClickableStyleFinderAccessor) this)::owo$setResult);
    }
}
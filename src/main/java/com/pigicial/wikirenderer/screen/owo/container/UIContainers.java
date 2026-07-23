package com.pigicial.wikirenderer.screen.owo.container;

import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;

public final class UIContainers {

    private UIContainers() {}

    // ------
    // Layout
    // ------

    public static GridLayout grid(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        return new GridLayout(horizontalSizing, verticalSizing, rows, columns);
    }

    public static FlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.VERTICAL);
    }

    public static FlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);
    }

    public static FlowLayout ltrTextFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.LTR_TEXT);
    }

    // ------
    // Scroll
    // ------

    public static <C extends UIComponent> ScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public static <C extends UIComponent> ScrollContainer<C> horizontalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child);
    }
}

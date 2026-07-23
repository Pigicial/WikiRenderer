package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.ScrollContainer;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;

public class NonResettingScrollContainer extends ScrollContainer<FlowLayout> {
    public NonResettingScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, FlowLayout child) {
        super(direction, horizontalSizing, verticalSizing, child);
    }

    public double getScrollOffset() {
        return this.scrollOffset;
    }

    public void setScrollPosition(double scrollPosition) {
        this.scrollOffset = Math.min(scrollPosition, this.maxScroll);
        this.currentScrollPosition = scrollPosition;
    }

    @Override
    protected void scrollBy(double offset, boolean instant, boolean showScrollbar) {
        super.scrollBy(offset, instant, showScrollbar);
    }
}

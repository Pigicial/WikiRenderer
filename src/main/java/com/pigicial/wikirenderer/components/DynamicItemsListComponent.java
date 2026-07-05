package com.pigicial.wikirenderer.components;

import com.pigicial.wikirenderer.property.PropertyBundle;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.render.skyblock.frame_based.FrameBasedRenderable;
import com.pigicial.wikirenderer.render.skyblock.frame_based.FrameData;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;

import java.util.ArrayList;
import java.util.List;

public class DynamicItemsListComponent<S, R extends Renderable<P>, P extends PropertyBundle> extends FlowLayout {
    private final FrameBasedRenderable<S, R, P> frameBasedRenderable;
    private int lastDataSetSize = 0;

    public DynamicItemsListComponent(FrameBasedRenderable<S, R, P> frameBasedRenderable) {
        super(Sizing.fill(100), Sizing.content(), FlowLayout.Algorithm.LTR_TEXT);
        this.frameBasedRenderable = frameBasedRenderable;
        this.margins(Insets.of(0, 5, 0, 0));
        this.verticalAlignment(VerticalAlignment.CENTER);

        update();
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        super.parentUpdate(delta, mouseX, mouseY);
        update();
    }

    private void update() {
        int dataSetSize = frameBasedRenderable.getCurrentDataSet().size();

        if (dataSetSize != lastDataSetSize) {
            List<ItemComponent> items = new ArrayList<>();
            for (int i = 0; i < dataSetSize; i++) {
                FrameData<S> frame = frameBasedRenderable.getFrame(i);
                items.add(this.frameBasedRenderable.createItemComponentForPreview(frame));
            }
            this.clearChildren();
            this.children(items);
            lastDataSetSize = items.size();
        }
    }
}

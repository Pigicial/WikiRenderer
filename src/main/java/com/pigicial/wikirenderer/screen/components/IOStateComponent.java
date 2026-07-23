package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.render.export.FileIO;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.Surface;

public class IOStateComponent extends FlowLayout {

    public IOStateComponent() {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);

        this.padding(Insets.of(10));
        this.surface(Surface.flat(0x77000000).and(Surface.outline(0x77000000)));

        this.child(new DynamicLabelComponent(FileIO::progressText).margins(Insets.bottom(10)));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        if (FileIO.taskCount() > 0) {
            super.draw(graphics, mouseX, mouseY, partialTicks, delta);
            WikiRendererUI.drawExportProgressBar(graphics, this.x + 10, this.y + 30, 70, 35, 15);
        }
    }
}

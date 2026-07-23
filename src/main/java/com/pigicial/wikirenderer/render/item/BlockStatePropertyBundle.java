package com.pigicial.wikirenderer.render.item;

import com.pigicial.wikirenderer.property.DefaultCroppablePropertyBundle;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.property.SerializablePropertyBundle;
import com.pigicial.wikirenderer.property.config.WikiRendererConfigs;
import com.pigicial.wikirenderer.render.Renderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.WikiRendererUI;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;

public class BlockStatePropertyBundle extends DefaultCroppablePropertyBundle implements SerializablePropertyBundle {

    public static final BlockStatePropertyBundle INSTANCE = WikiRendererConfigs.loadOrDefault(new BlockStatePropertyBundle());

    @Override
    public String getConfigFileName() {
        return "block_state_render_settings";
    }

    @Override
    public void buildRenderOptionGUIControls(Renderable<?> renderable, RenderScreen screen, FlowLayout container) {
        WikiRendererUI.booleanControl(container, GlobalProperties.get().tickParticles, "particles");
        this.buildLoopParticlesOption(container);
    }

    @Override
    protected boolean allowRotatingWithMouseByDefault() {
        return true;
    }
}

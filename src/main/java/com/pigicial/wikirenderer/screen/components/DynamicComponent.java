package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Size;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;
import java.util.function.Supplier;

public class DynamicComponent extends FlowLayout {
    private final UIComponent component;
    private final Supplier<Boolean> displayCondition;
    private boolean exists = true;

    public DynamicComponent(UIComponent component, Supplier<Boolean> displayCondition) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.component = component;
        this.displayCondition = displayCondition;

        this.child(component);
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        return exists && super.isInBoundingBox(x, y);
    }

    @Override
    public List<ClientTooltipComponent> tooltip() {
        return exists ? super.tooltip() : List.of();
    }

    @Override
    public void inflate(Size space) {
        if (!exists) {
            // not doing this can cause the component to sometimes not appear
            this.space = space;
            component.inflate(this.calculateChildSpace(space));
        } else {
            super.inflate(space);
        }
    }

    @Override
    public Size fullSize() {
        if (!exists) {
            int gap = 0;
            if (this.parent() instanceof FlowLayout flow) {
                gap = flow.gap();
            }
            return Size.of(-gap, -gap);
        }
        return super.fullSize();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return exists ? component.fullSize().height() : 0;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return exists ? component.fullSize().width() : 0;
    }

    private void update() {
        boolean shouldShow = displayCondition.get();
        if (exists != shouldShow) {
            exists = shouldShow;
            if (this.parent() != null) {
                this.parent().onChildMutated(this);
            }
        }
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        update();
        super.parentUpdate(delta, mouseX, mouseY);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!exists) return;
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
    }
}
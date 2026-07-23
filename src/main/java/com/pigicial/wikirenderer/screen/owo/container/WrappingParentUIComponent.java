package com.pigicial.wikirenderer.screen.owo.container;

import com.pigicial.wikirenderer.screen.owo.base.BaseParentUIComponent;
import com.pigicial.wikirenderer.screen.owo.core.ParentUIComponent;
import com.pigicial.wikirenderer.screen.owo.core.Size;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;

import java.util.Collections;
import java.util.List;

public abstract class WrappingParentUIComponent<C extends UIComponent> extends BaseParentUIComponent {

    protected C child;
    protected List<UIComponent> childView;

    protected WrappingParentUIComponent(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing);
        this.child = child;
        this.childView = Collections.singletonList(this.child);
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.child.fullSize().width() + this.padding.get().horizontal();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.child.fullSize().height() + this.padding.get().vertical();
    }

    @Override
    public void layout(Size space) {
        this.child.inflate(this.calculateChildSpace(space));
        this.child.mount(this, this.childMountX(), this.childMountY());
    }

    /**
     * @return The x-coordinate at which to mount the child
     */
    protected int childMountX() {
        return this.x + child.margins().get().left() + this.padding.get().left();
    }

    /**
     * @return The y-coordinate at which to mount the child
     */
    protected int childMountY() {
        return this.y + child.margins().get().top() + this.padding.get().top();
    }

    public WrappingParentUIComponent<C> child(C newChild) {
        if (this.child != null) {
            this.child.dismount(DismountReason.REMOVED);
        }

        this.child = newChild;
        this.childView = Collections.singletonList(this.child);

        this.updateLayout();
        return this;
    }

    public C child() {
        return this.child;
    }

    @Override
    public List<UIComponent> children() {
        return this.childView;
    }

    @Override
    public ParentUIComponent removeChild(UIComponent child) {
        throw new UnsupportedOperationException("Cannot remove the child of a wrapping component");
    }
}

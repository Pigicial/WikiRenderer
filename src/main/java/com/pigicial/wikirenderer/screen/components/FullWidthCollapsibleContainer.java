package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.screen.owo.component.LabelComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.UIContainers;
import com.pigicial.wikirenderer.screen.owo.core.*;
import com.pigicial.wikirenderer.screen.owo.event.EventStream;
import com.pigicial.wikirenderer.screen.owo.util.Delta;
import com.pigicial.wikirenderer.screen.owo.util.UISounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class FullWidthCollapsibleContainer extends FlowLayout {

    protected final EventStream<OnToggled> toggledEvents = OnToggled.newStream();

    protected final List<UIComponent> collapsibleChildren = new ArrayList<>();
    protected boolean expanded;

    protected final SpinnyBoiComponent spinningArrowComponent;
    protected final FlowLayout textAndTitleComponent;
    protected final FlowLayout expansionComponent;
    protected final FlowLayout contentLayout;

    public FullWidthCollapsibleContainer(UIComponent mainText, Supplier<MutableComponent> configureTextSupplier, boolean expanded) {
        super(Sizing.fill(), Sizing.content(), Algorithm.VERTICAL);
        this.allowOverflow(true);

        // Expansion
        this.textAndTitleComponent = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        this.textAndTitleComponent.horizontalSizing(Sizing.content());

        this.expansionComponent = UIContainers.horizontalFlow(Sizing.expand(100), Sizing.content());
        this.expansionComponent.horizontalAlignment(HorizontalAlignment.RIGHT);
        this.expansionComponent.padding(Insets.vertical(2));
        this.expansionComponent.child(new DynamicLabelComponent(() -> configureTextSupplier.get().withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE))
                .horizontalTextAlignment(HorizontalAlignment.RIGHT)
                .horizontalSizing(Sizing.fill(90))
                .cursorStyle(CursorStyle.HAND));

        this.spinningArrowComponent = new SpinnyBoiComponent();
        this.expansionComponent.child(spinningArrowComponent);

        this.textAndTitleComponent.child(mainText);
        this.textAndTitleComponent.child(this.expansionComponent);

        this.expanded = expanded;
        this.spinningArrowComponent.targetRotation = expanded ? 90 : 0;
        this.spinningArrowComponent.rotation = this.spinningArrowComponent.targetRotation;

        super.child(this.textAndTitleComponent);

        // Content
        this.contentLayout = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        this.contentLayout.padding(Insets.left(15));

        super.child(this.contentLayout);
    }

    public void toggleExpansion() {
        if (expanded) {
            this.contentLayout.clearChildren();
            this.spinningArrowComponent.targetRotation = 0;
        } else {
            this.contentLayout.children(this.collapsibleChildren);
            this.spinningArrowComponent.targetRotation = 90;
        }

        this.expanded = !this.expanded;
        this.toggledEvents.sink().onToggle(this.expanded);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        if (input.isSelection()) {
            this.toggleExpansion();

            super.onKeyPress(input);
            return true;
        }

        return super.onKeyPress(input);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        final var superResult = super.onMouseDown(click, doubled);

        if (click.y() <= this.expansionComponent.fullSize().height() && !superResult) {
            this.toggleExpansion();
            UISounds.playInteractionSound();
            return true;
        } else {
            return superResult;
        }
    }

    @Override
    public FlowLayout child(UIComponent child) {
        this.collapsibleChildren.add(child);
        if (this.expanded) this.contentLayout.child(child);
        return this;
    }

    @Override
    public FlowLayout children(Collection<? extends UIComponent> children) {
        this.collapsibleChildren.addAll(children);
        if (this.expanded) this.contentLayout.children(children);
        return this;
    }

    @Override
    public FlowLayout child(int index, UIComponent child) {
        this.collapsibleChildren.add(index, child);
        if (this.expanded) this.contentLayout.child(index, child);
        return this;
    }

    @Override
    public FlowLayout children(int index, Collection<? extends UIComponent> children) {
        this.collapsibleChildren.addAll(index, children);
        if (this.expanded) this.contentLayout.children(index, children);
        return this;
    }

    @Override
    public FlowLayout removeChild(UIComponent child) {
        this.collapsibleChildren.remove(child);
        return this.contentLayout.removeChild(child);
    }

    protected static class SpinnyBoiComponent extends LabelComponent {

        protected float rotation = 90;
        protected float targetRotation = 90;

        public SpinnyBoiComponent() {
            super(Component.literal(">"));
            this.margins(Insets.of(0, 0, 5, 0));
            this.cursorStyle(CursorStyle.HAND);
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += (float) Delta.compute(this.rotation, this.targetRotation, delta * .65);
        }

        @Override
        public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
            Matrix3x2fStack matrices = graphics.pose();

            matrices.pushMatrix();
            matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1);
            matrices.rotate((float) Math.toRadians(this.rotation));
            matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1));

            super.draw(graphics, mouseX, mouseY, partialTicks, delta);
            matrices.popMatrix();
        }
    }

    public interface OnToggled {
        void onToggle(boolean nowExpanded);

        static EventStream<OnToggled> newStream() {
            return new EventStream<>(subscribers -> nowExpanded -> {
                for (var subscriber : subscribers) {
                    subscriber.onToggle(nowExpanded);
                }
            });
        }
    }
}

package com.pigicial.wikirenderer.screen.owo.component;

import com.pigicial.wikirenderer.mixin.screen.EditBoxAccessor;
import com.pigicial.wikirenderer.screen.owo.core.CursorStyle;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.event.EventSource;
import com.pigicial.wikirenderer.screen.owo.event.EventStream;
import com.pigicial.wikirenderer.screen.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TextBoxComponent extends EditBox {

    protected final Observable<Boolean> showsBackground = Observable.of(((EditBoxAccessor) this).owo$bordered());

    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();

    protected Predicate<String> filter = Objects::nonNull;

    public TextBoxComponent(Sizing horizontalSizing) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty());

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        this.sizing(horizontalSizing, Sizing.content());

        this.showsBackground.observe(a -> this.widgetWrapper().notifyParentIfMounted());
    }

    /**
     * @deprecated Subscribe to {@link #onChanged()} instead
     */
    @Override
    @Deprecated(forRemoval = true)
    public void setResponder(Consumer<String> changedListener) {
        super.setResponder(changedListener);
    }

    @Override
    public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean result = super.keyPressed(input);

        if (input.isCycleFocus()) {
            this.insertText("    ");
            return true;
        } else {
            return result;
        }
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        ((EditBoxAccessor) this).owo$updateTextPosition();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        ((EditBoxAccessor) this).owo$updateTextPosition();
    }

    @Override
    public void setBordered(boolean drawsBackground) {
        super.setBordered(drawsBackground);
        this.showsBackground.set(drawsBackground);
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public TextBoxComponent text(String text) {
        this.setValue(text);
        this.moveCursorToStart(false);
        return this;
    }

    public void setFilter(Predicate<String> predicate) {
        this.filter = predicate;
    }

    public Predicate<String> getFilter() {
        return this.filter;
    }

    @Override
    public void setValue(String value) {
        if (!this.filter.test(value)) {
            return;
        }

        super.setValue(value);
    }
    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.TEXT;
    }

    public interface OnChanged {
        void onChanged(String value);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}

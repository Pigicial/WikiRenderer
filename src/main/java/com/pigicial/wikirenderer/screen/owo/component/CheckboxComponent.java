package com.pigicial.wikirenderer.screen.owo.component;

import com.pigicial.wikirenderer.mixin.screen.CheckboxAccessor;
import com.pigicial.wikirenderer.screen.owo.core.CursorStyle;
import com.pigicial.wikirenderer.screen.owo.core.Size;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class CheckboxComponent extends Checkbox {

    protected final Observable<Boolean> listeners;

    protected CheckboxComponent(Component message) {
        super(0, 0, 0, message, Minecraft.getInstance().font, false, (checkbox, checked) -> {});
        this.listeners = Observable.of(this.selected());
        this.sizing(Sizing.content(), Sizing.fixed(20));
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);
        this.listeners.set(this.selected());
    }

    public CheckboxComponent checked(boolean checked) {
        ((CheckboxAccessor) this).owo$setSelected(checked);
        this.listeners.set(this.selected());
        return this;
    }

    public CheckboxComponent onChanged(Consumer<Boolean> listener) {
        this.listeners.observe(listener);
        return this;
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);
        ((CheckboxAccessor) this).owo$getTextWidget().setMaxWidth(this.width);
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        ((CheckboxAccessor)this).owo$getTextWidget().setMessage(message);
    }

    public CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.HAND;
    }
}

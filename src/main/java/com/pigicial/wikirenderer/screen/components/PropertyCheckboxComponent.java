package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.mixin.screen.CheckboxAccessor;
import com.pigicial.wikirenderer.property.Property;
import com.pigicial.wikirenderer.screen.owo.component.CheckboxComponent;
import com.pigicial.wikirenderer.screen.owo.core.Size;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class PropertyCheckboxComponent extends CheckboxComponent {

    private final Property<Boolean> property;

    public PropertyCheckboxComponent(Component message, Property<Boolean> property) {
        super(message);

        this.property = property;
        this.checked(this.property.get());
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        Boolean checked = this.property.get();
        if (checked != this.selected()) {
            this.checked(checked);
        }

        super.update(delta, mouseX, mouseY);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        property.set(this.selected());
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);
        ((CheckboxAccessor) this).owo$getTextWidget().setMaxWidth(Math.max(space.width() - 29, 20));
    }
}

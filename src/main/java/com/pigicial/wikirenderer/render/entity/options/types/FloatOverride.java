package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.MiniEditBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.UnknownNullability;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FloatOverride<S extends EntityRenderState>  extends OptionalOverride<S, Float> {

    public FloatOverride(String key, Function<S, Float> getter, BiConsumer<S, Float> setter) {
        super(key, getter, setter);
    }

    public FloatOverride(String key, Function<S, Float> getter, BiConsumer<S, Float> setter, Float defaultValue) {
        super(key, getter, setter, defaultValue);
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.fixed(50), toString(this.getValue()));
        editBox.setFilter(this::isValidNumber);
        editBox.onChanged().subscribe(text -> this.setValue(isWorkInProgressNumber(text) ? 0 : Float.parseFloat(text.trim())));
        editBox.focusLost().subscribe(() -> editBox.text(toString(this.getValue())));

        row.child(editBox);
    }

    private String toString(float value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private boolean isWorkInProgressNumber(String s) {
        return s.isBlank() || s.trim().equals("-");
    }

    private boolean isValidNumber(String s) {
        if (isWorkInProgressNumber(s)) {
            return true;
        }
        try {
            Float.parseFloat(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Float getDefaultValue() {
        return 0f;
    }
}

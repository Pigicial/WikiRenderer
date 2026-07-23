package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.MiniEditBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.UnknownNullability;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class IntegerOverride<S extends EntityRenderState> extends OptionalOverride<S, Integer> {

    public IntegerOverride(String key, Function<S, Integer> getter, BiConsumer<S, Integer> setter) {
        super(key, getter, setter);
    }

    public IntegerOverride(String key, Function<S, Integer> getter, BiConsumer<S, Integer> setter, Integer defaultValue) {
        super(key, getter, setter, defaultValue);
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.fixed(50), toString(this.getValue()));
        editBox.setFilter(this::isValidNumber);
        editBox.onChanged().subscribe(text -> this.setValue(isWorkInProgressNumber(text) ? 0 : Integer.parseInt(text.trim())));
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
            Integer.parseInt(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}

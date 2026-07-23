package com.pigicial.wikirenderer.render.entity.options.types;

import com.pigicial.wikirenderer.screen.components.MiniEditBoxComponent;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.UnknownNullability;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DoubleOverride<S extends EntityRenderState>  extends OptionalOverride<S, Double> {

    public DoubleOverride(String key, Function<S, Double> getter, BiConsumer<S, Double> setter) {
        super(key, getter, setter);
    }

    @Override
    protected void addToComponentRow(@UnknownNullability FlowLayout row) {
        MiniEditBoxComponent editBox = new MiniEditBoxComponent(Sizing.fixed(50), toString(this.getValue()));
        editBox.setFilter(this::isValidNumber);
        editBox.onChanged().subscribe(text -> this.setValue(isWorkInProgressNumber(text) ? 0 : Double.parseDouble(text.trim())));
        editBox.focusLost().subscribe(() -> editBox.text(toString(this.getValue())));

        row.child(editBox);
    }

    private String toString(double value) {
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
            Double.parseDouble(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Double getDefaultValue() {
        return 0d;
    }
}

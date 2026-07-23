package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.property.DoubleProperty;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;

import java.util.Objects;
import java.util.function.Predicate;

public class DoublePropertyTextFieldComponent extends TextBoxComponent {

    private final DoubleProperty setting;
    private String content = "";
    private boolean ignoringChange = false;

    private boolean previouslyFocused = false;

    public DoublePropertyTextFieldComponent(RenderScreen renderScreen, Sizing horizontalSizing, DoubleProperty setting) {
        super(horizontalSizing);
        this.setting = setting;

        this.text(String.format("%.1f", setting.get()));
        this.setFilter(makeMatcher());

        this.onChanged().subscribe(s -> {
            if (s.endsWith(".")) {
                return;
            }

            if (Objects.equals(s, content) || s.isEmpty() || s.equals("-")) {
                return;
            }

            this.content = s;
            if (!this.ignoringChange) {
                this.ignoringChange = true;
                this.setting.set(Double.parseDouble(s));
                this.ignoringChange = false;
            }
        });

        this.setting.instantListen(renderScreen, (doubleSetting, value) -> {
            if (!this.ignoringChange) {
                this.ignoringChange = true;
                this.text(this.formatNumber(value));
                this.ignoringChange = false;
            }
        });
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
        if (this.isFocused()) {
            this.previouslyFocused = true;
            return;
        }

        if (this.setting.hasRollover() && !this.isFocused() && previouslyFocused) {
            this.ignoringChange = true;
            this.text(this.formatNumber(setting.get()));
            this.ignoringChange = false;
        }
    }

    private String formatNumber(double value) {
        String number = String.format("%.3f", value);
        return number.endsWith(".000") ? number.substring(0, number.length() - 4) : number;
    }

    private Predicate<String> makeMatcher() {
        StringBuilder builder = new StringBuilder();
        if (this.setting.min() < 0 || this.setting.hasRollover()) builder.append("-?");

        builder.append("\\d{0,");
        int maxNumberLength = String.valueOf(Math.max(Math.abs(this.setting.min()), Math.abs(this.setting.max()))).length();
        if (setting.hasRollover()) {
            maxNumberLength += 2; // probably enough extra
        }
        builder.append(maxNumberLength);
        builder.append("}");
        builder.append("\\.?\\d{0,3}"); // up to 3 decimals

        String regex = builder.toString();
        return s -> {
            boolean matches = s.matches(regex);
            if (matches && !s.isEmpty() && !s.endsWith(".") && !s.equals("-")) {
                double number = Double.parseDouble(s);
                return number >= this.setting.min() && number <= this.setting.max();
            }

            return matches;
        };
    }
}


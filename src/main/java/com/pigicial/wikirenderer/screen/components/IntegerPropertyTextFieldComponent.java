package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.property.IntProperty;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.screen.owo.component.TextBoxComponent;
import com.pigicial.wikirenderer.screen.owo.core.OwoUIGraphics;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;

import java.util.Objects;
import java.util.function.Predicate;

public class IntegerPropertyTextFieldComponent extends TextBoxComponent {

    private final IntProperty setting;
    private final boolean formatAsPercentage;
    private String content = "";
    private boolean ignoringChange = false;

    private boolean previouslyFocused = false;

    public IntegerPropertyTextFieldComponent(RenderScreen screen, Sizing horizontalSizing, IntProperty setting, boolean formatAsPercentage) {
        super(horizontalSizing);
        this.setting = setting;
        this.formatAsPercentage = formatAsPercentage;

        this.text(setting.get() + (formatAsPercentage ? "%" : ""));
        this.setFilter(makeMatcher());

        this.onChanged().subscribe(s -> {
            s = s.replace("%", "");
            if (Objects.equals(s, content) || s.isEmpty() || s.equals("-") || s.replace("%", "").isEmpty()) {
                return;
            }

            this.content = s;

            if (!this.ignoringChange) {
                this.ignoringChange = true;
                this.setting.set(Integer.parseInt(s));
                this.ignoringChange = false;
            }
        });

        this.setting.futureListen(screen, (integerSetting, integer) -> {
            if (!this.ignoringChange) {
                this.ignoringChange = true;
                this.text(integer + (formatAsPercentage ? "%" : ""));
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

        if ((this.setting.hasRollover() || formatAsPercentage) && !this.isFocused() && previouslyFocused) {
            this.ignoringChange = true;
            this.text(setting.get() + (formatAsPercentage ? "%" : ""));
            this.ignoringChange = false;
        }
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
        if (formatAsPercentage) {
            builder.append("%?");
        }

        String regex = builder.toString();
        return s -> {
            s = s.replace("%", "");
            boolean matches = s.matches(regex);
            if (matches && !this.setting.hasRollover() && !s.isEmpty() && !s.equals("-")) {
                int number = Integer.parseInt(s);
                return number >= this.setting.min() && number <= this.setting.max();
            }
            return matches;
        };
    }
}

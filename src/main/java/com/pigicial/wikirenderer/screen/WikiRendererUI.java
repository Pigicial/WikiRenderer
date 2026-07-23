package com.pigicial.wikirenderer.screen;

import com.pigicial.wikirenderer.property.DoubleProperty;
import com.pigicial.wikirenderer.property.IntProperty;
import com.pigicial.wikirenderer.property.Property;
import com.pigicial.wikirenderer.screen.components.*;
import com.pigicial.wikirenderer.screen.owo.base.BaseUIComponent;
import com.pigicial.wikirenderer.screen.owo.component.*;
import com.pigicial.wikirenderer.screen.owo.container.FlowLayout;
import com.pigicial.wikirenderer.screen.owo.container.UIContainers;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.screen.owo.core.UIComponent;
import com.pigicial.wikirenderer.screen.owo.core.VerticalAlignment;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WikiRendererUI {

    private static void addTooltipIfPossible(UIComponent component, String key) {
        MutableComponent tooltip = Translate.guiIfExists(key + ".tooltip");
        if (tooltip != null) {
            List<ClientTooltipComponent> components = new ArrayList<>();
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(tooltip, 300)) {
                components.add(ClientTooltipComponent.create(line));
            }
            component.tooltip(components);
        }
    }

    public static TextBoxComponent labelledTextField(FlowLayout container, String content, String key, Sizing sizing) {
        try (RowBuilder builder = rowBuilder(container)) {
            TextBoxComponent textBox = textBox(sizing, content);
            textBox.setMaxLength(100); // allow more characters
            textBox.text(content); // fixes if it truncates early
            builder.row.child(textBox);

            BaseUIComponent label = label(Translate.gui(key)).margins(Insets.left(8));
            addTooltipIfPossible(label, key);
            builder.row.child(label);

            return textBox;
        }
    }

    public static void labelledTextField(RenderScreen screen, FlowLayout container, IntProperty property, String key, Sizing sizing) {
        try (RowBuilder builder = rowBuilder(container)) {
            TextBoxComponent textBox = new IntegerPropertyTextFieldComponent(screen, sizing, property, false);
            builder.row.child(textBox);

            BaseUIComponent label = new AutoResizingLabelComponent(Translate.gui(key), textBox.width() - 10).margins(Insets.left(8));
            addTooltipIfPossible(label, key);
            builder.row.child(label);
        }
    }

    public static void intControl(RenderScreen screen, FlowLayout container, IntProperty property, String name) {
        try (RowBuilder builder = rowBuilder(container)) {
            builder.row.child(new IntegerPropertyTextFieldComponent(screen, Sizing.fixed(45), property, false));
            builder.row.child(new PropertySliderComponent(screen, Sizing.expand(100), Translate.gui(name), property).margins(Insets.horizontal(5)));
            builder.row.child(new ResetPropertyButton(property).margins(Insets.right(5)));
        }
    }

    public static void conditionalIntControl(RenderScreen screen, FlowLayout container, IntProperty property, String name, Supplier<Boolean> displayCondition) {
        FlowLayout row = row();
        row.child(new IntegerPropertyTextFieldComponent(screen, Sizing.fixed(45), property, false));
        row.child(new PropertySliderComponent(screen, Sizing.expand(100), Translate.gui(name), property).margins(Insets.horizontal(5)));
        row.child(new ResetPropertyButton(property).margins(Insets.right(5)));
        container.child(new DynamicComponent(row, displayCondition));
    }

    public static void intPercentageControl(RenderScreen screen, FlowLayout container, IntProperty property, String key) {
        try (RowBuilder builder = rowBuilder(container)) {
            builder.row.child(new IntegerPropertyTextFieldComponent(screen, Sizing.fixed(45), property, true));

            UIComponent slider = new PropertySliderComponent(screen, Sizing.expand(100), Translate.gui(key), property).margins(Insets.horizontal(5));
            addTooltipIfPossible(slider, key);

            builder.row.child(slider);
            builder.row.child(new ResetPropertyButton(property).margins(Insets.right(5)));
        }
    }

    public static void doubleControl(RenderScreen screen, FlowLayout container, DoubleProperty property, String name) {
        try (RowBuilder builder = rowBuilder(container)) {
            builder.row.child(new DoublePropertyTextFieldComponent(screen, Sizing.fixed(45), property));
            builder.row.child(new PropertySliderComponent(screen, Sizing.expand(100), Translate.gui(name), property).margins(Insets.horizontal(5)));
            builder.row.child(new ResetPropertyButton(property).margins(Insets.right(5)));
        }
    }

    public static void booleanControl(FlowLayout container, Property<Boolean> property, String key, Object... args) {
        UIComponent checkbox = new PropertyCheckboxComponent(Translate.gui(key, args), property).margins(Insets.of(2, 1, 0, 0));
        addTooltipIfPossible(checkbox, key);
        container.child(checkbox);
    }

    public static void conditionalBooleanControl(FlowLayout container, Property<Boolean> property, String key, Supplier<Boolean> displayCondition) {
        UIComponent checkbox = new PropertyCheckboxComponent(Translate.gui(key), property).margins(Insets.of(2, 1, 0, 0));
        addTooltipIfPossible(checkbox, key);
        container.child(new DynamicComponent(checkbox, displayCondition));
    }

    public static ButtonComponent button(Component message, Consumer<ButtonComponent> onPress) {
        ButtonComponent button = new ButtonComponent(message, onPress);
        button.margins(Insets.of(2, 3, 0, 0));
        return button;
    }

    public static LabelComponent text(FlowLayout container, String key, boolean extraVerticalMargins) {
        LabelComponent label = new AutoResizingLabelComponent(Translate.gui(key));
        label.shadow(true);
        if (extraVerticalMargins) {
            label.margins(Insets.top(15));
        }
        label.margins(label.margins().get().withBottom(5));

        addTooltipIfPossible(label, key);

        container.child(label);
        return label;
    }

    public static LabelComponent text(FlowLayout container, String key, int topMargins) {
        LabelComponent label = new AutoResizingLabelComponent(Translate.gui(key));
        label.shadow(true);
        label.margins(Insets.top(topMargins));
        label.margins(label.margins().get().withBottom(5));

        container.child(label);
        return label;
    }

    public static LabelComponent text(FlowLayout container, Component component, int topMargins) {
        LabelComponent label = new AutoResizingLabelComponent(component);
        label.shadow(true);
        label.margins(Insets.top(topMargins));
        label.margins(label.margins().get().withBottom(5));

        container.child(label);
        return label;
    }

    public static DynamicLabelComponent dynamicText(FlowLayout container, Supplier<Component> content) {
        DynamicLabelComponent label = new DynamicLabelComponent(content).shadow(false);
        label.margins(Insets.of(3, 5, 0, 0));

        container.child(label);
        return label;
    }

    public static void dynamicConditionalText(FlowLayout container, Supplier<Boolean> predicate, Supplier<Component> content) {
        DynamicLabelComponent label = new DynamicLabelComponent(content);
        label.shadow(false);
        label.margins(Insets.vertical(5));

        container.child(new DynamicComponent(label, predicate));
    }

    public static void drawExportProgressBar(GuiGraphicsExtractor context, int x, int y, int drawWidth, int barWidth, double speed) {
        int end = x + drawWidth + barWidth;

        int offset = (int) (System.currentTimeMillis() / speed % (drawWidth + barWidth));

        int endWithOffset = x + offset;
        if (endWithOffset > end) endWithOffset = end;

        context.fill(Math.max(x + offset - barWidth, x), y, Math.min(endWithOffset, x + drawWidth), y + 2, 0xFF00FF00);
    }

    public static RowBuilder rowBuilder(FlowLayout container) {
        return new RowBuilder(row(), container);
    }

    public static FlowLayout row() {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        layout.margins(Insets.of(3, 3, 0, 0)).verticalAlignment(VerticalAlignment.CENTER);
        return layout;
    }

    public static RowBuilder autoNewLineRow(FlowLayout container) {
        FlowLayout layout = UIContainers.ltrTextFlow(Sizing.fill(100), Sizing.content());
        layout.margins(Insets.of(3, 3, 0, 0)).verticalAlignment(VerticalAlignment.CENTER);
        return new RowBuilder(layout, container);
    }

    public static TextBoxComponent textBox(Sizing horizontalSizing, String text) {
        TextBoxComponent textBox = new TextBoxComponent(horizontalSizing);
        textBox.text(text);
        return textBox;
    }

    public static ItemComponent item(ItemStack item) {
        return new ItemComponent(item);
    }

    public static LabelComponent label(Component text) {
        return new LabelComponent(text);
    }

    public static DropdownComponent dropdown(Sizing horizontalSizing) {
        return new DropdownComponent(horizontalSizing);
    }

    public static class RowBuilder implements AutoCloseable {

        public final FlowLayout row;
        private final FlowLayout container;

        private RowBuilder(FlowLayout row, FlowLayout container) {
            this.row = row;
            this.container = container;
        }

        @Override
        public void close() {
            this.container.child(this.row);
        }
    }
}

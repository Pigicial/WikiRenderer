package com.pigicial.wikirenderer.screen.owo.core;

public interface Surface {

    Surface BLANK = (_, _) -> {};
    Surface PANEL = (context, component) -> context.drawPanel(component.x(), component.y(), component.width(), component.height(), false);
    Surface VANILLA_TRANSLUCENT = (context, component) -> context.drawGradientRect(
        component.x(), component.y(), component.width(), component.height(),
        0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010
    );

    static Surface flat(int color) {
        return (context, component) -> context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), color);
    }

    void draw(OwoUIGraphics context, ParentUIComponent component);

    default Surface and(Surface surface) {
        return (context, component) -> {
            this.draw(context, component);
            surface.draw(context, component);
        };
    }
}

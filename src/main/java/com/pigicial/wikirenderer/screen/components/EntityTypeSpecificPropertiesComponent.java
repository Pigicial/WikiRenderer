package com.pigicial.wikirenderer.screen.components;

import com.pigicial.wikirenderer.render.entity.options.EntityTypeSpecificOverrides;
import com.pigicial.wikirenderer.render.entity.options.types.OptionalOverride;
import com.pigicial.wikirenderer.screen.owo.component.DropdownComponent;
import com.pigicial.wikirenderer.screen.owo.component.LabelComponent;
import com.pigicial.wikirenderer.screen.owo.core.Color;
import com.pigicial.wikirenderer.screen.owo.core.Insets;
import com.pigicial.wikirenderer.screen.owo.core.Sizing;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.ChatFormatting;

import java.util.Objects;
import java.util.function.Supplier;

public class EntityTypeSpecificPropertiesComponent extends DropdownComponent {

    private final Supplier<Integer> entitySupplier;
    private final Supplier<EntityTypeSpecificOverrides<?>> overridesSupplier;

    private Integer lastSavedEntityId = null;
    private boolean freezeUpdates = false; // prevents the scroll position from resetting

    public EntityTypeSpecificPropertiesComponent(Supplier<Integer> entityIdSupplier, Supplier<EntityTypeSpecificOverrides<?>> overridesSupplier) {
        super(Sizing.content());
        this.entitySupplier = entityIdSupplier;
        this.overridesSupplier = overridesSupplier;

        this.closeWhenNotHovered(false);
        this.padding(Insets.of(7, 0, 0, 5));
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        this.update();
        super.parentUpdate(delta, mouseX, mouseY);
    }

    @Override
    protected void updateLayout() {
        if (freezeUpdates) return;
        super.updateLayout();
    }

    public void update() {
        this.freezeUpdates = false;
        Integer entityId = entitySupplier.get();
        if (!Objects.equals(entityId, lastSavedEntityId)) {
            this.freezeUpdates = true;
            this.entries.clearChildren();
            this.lastSavedEntityId = entityId;

            if (entityId == null) {
                this.freezeUpdates = false;
                this.updateLayout();
                return;
            }

            EntityTypeSpecificOverrides<?> overrides = this.overridesSupplier.get();
            if (overrides == null) {
                this.freezeUpdates = false;
                this.updateLayout();
                return;
            }

            if (!overrides.getOverrides().isEmpty()) {
                this.text(Translate.gui("advanced_entity_data").withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));

                LabelComponent label = new AutoResizingLabelComponent(Translate.gui("advanced_entity_data_notice"));
                label.color(Color.ofFormatting(ChatFormatting.GRAY));
                label.margins(Insets.of(2));
                this.entries.child(label);

                this.button(Translate.gui("reset_advanced_entity_overrides").withStyle(ChatFormatting.UNDERLINE), comp -> overrides.getOverrides().forEach(OptionalOverride::reset));

                for (OptionalOverride<?, ?> override : overrides.getOverrides()) {
                    this.entries.child(override.buildComponent());
                }
            }

            this.freezeUpdates = false;
            this.updateLayout();
        }
    }
}

package com.pigicial.wikirenderer.screen;

import com.pigicial.wikirenderer.render.batch.ItemBatchRenderTask;
import com.pigicial.wikirenderer.util.Translate;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SelectRenderTaskScreen extends BaseOwoScreen<FlowLayout> implements ContainerPreservingScreen {

    private final Collection<ItemStack> items;
    @Nullable
    private AbstractContainerScreen<?> previouslyOpenedContainerScreen = null;

    public SelectRenderTaskScreen(Collection<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void setPreviouslyOpenedContainerScreen(@Nullable AbstractContainerScreen<?> screen) {
        this.previouslyOpenedContainerScreen = screen;
    }

    @Override
    public @Nullable AbstractContainerScreen<?> getPreviouslyOpenedContainerScreen() {
        return this.previouslyOpenedContainerScreen;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout mainPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        mainPanel.surface(Surface.PANEL).padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.CENTER);

        mainPanel.child(UIComponents.label(Translate.gui("select_batch_operation")).shadow(true).margins(Insets.of(5).withBottom(10)));

        FlowLayout contentPanel = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        contentPanel.verticalAlignment(VerticalAlignment.CENTER);

        contentPanel.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                .child(UIComponents.button(Translate.gui("select_item_batch"), _ -> {
                    ItemBatchRenderTask.BATCH_ITEM.action.accept("inventory", this.items);
                    this.onClose();
                }).horizontalSizing(Sizing.fixed(80)).margins(Insets.bottom(5)))
                .child(UIComponents.button(Translate.gui("select_block_batch"), _ -> {
                    ItemBatchRenderTask.BATCH_BLOCK.action.accept("inventory", this.items);
                    this.onClose();
                }).horizontalSizing(Sizing.fixed(80)).margins(Insets.bottom(5)))
                .child(UIComponents.button(Translate.gui("select_tooltip_batch"), _ -> {
                    ItemBatchRenderTask.BATCH_TOOLTIP.action.accept("inventory", this.items);
                    this.onClose();
                }).horizontalSizing(Sizing.fixed(80)).margins(Insets.bottom(5)))
                .child(UIComponents.button(Translate.gui("select_atlas"), _ -> {
                    ItemBatchRenderTask.ITEM_ATLAS.action.accept("inventory", this.items);
                    this.onClose();
                }).horizontalSizing(Sizing.fixed(80)))
                .padding(Insets.of(5))
        );

        FlowLayout itemPreviewPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

        itemPreviewPanel.child(UIComponents.label(Translate.gui("render_task_size", this.items.size())).margins(Insets.of(7)))
                .horizontalAlignment(HorizontalAlignment.CENTER).padding(Insets.of(3))
                .surface(Surface.flat(0x77000000).and(Surface.outline(0x77000000)))
                .margins(Insets.left(10));

        FlowLayout itemContainer = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

        List<ItemStack> itemList = this.items.stream().toList();
        int rows = Mth.positiveCeilDiv(itemList.size(), 9);
        for (int row = 0; row < rows; row++) {
            FlowLayout rowContainer = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());

            for (int column = 0; column < 9; column++) {
                int index = row * 9 + column;
                if (index >= itemList.size()) break;

                rowContainer.child(UIComponents.item(itemList.get(index)));
            }

            itemContainer.child(rowContainer);
        }

        itemPreviewPanel.child(UIContainers.verticalScroll(Sizing.content(), Sizing.fixed(Math.min(250, rows * 16)), itemContainer));

        contentPanel.child(itemPreviewPanel);
        mainPanel.child(contentPanel);
        rootComponent.child(mainPanel);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!ScreenSchedulerAndSaver.hasScheduled() && ScreenSchedulerAndSaver.restorePreviouslyOpenedContainer(this)) return;

        super.onClose();
    }

}

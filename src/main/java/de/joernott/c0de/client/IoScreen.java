package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.Panels;
import de.joernott.c0de.io.IoMenu;
import de.joernott.c0de.network.BindPayload;
import de.joernott.c0de.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The dialog of an I/O block (docs/concept/19-input-output.md), laid out like that of the trigger block: the input
 * and output inventories and the player inventory on the left, the program and function of the binding on the
 * right.
 */
public class IoScreen extends AbstractContainerScreen<IoMenu> {
    private static final int PANEL_X = 180;
    private static final int BACKGROUND = 0xFFC6C6C6;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_INSIDE = 0xFF8B8B8B;

    private final BindingPanel binding;

    public IoScreen(IoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        binding = new BindingPanel(menu.data());
        imageWidth = PANEL_X + BindingPanel.WIDTH;
        imageHeight = IoMenu.HOTBAR_Y + 24;
        inventoryLabelY = IoMenu.PLAYER_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        binding.init(this::addRenderableWidget, leftPos + PANEL_X + 2, topPos, () -> {
            Platform.INSTANCE.sendToServer(new BindPayload(menu.pos(), binding.program(), binding.function()));
            onClose();
        });
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        Panels.draw(graphics, leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFF555555, BACKGROUND);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, SLOT_DARK);
            graphics.fill(x + 1, y + 1, x + 18, y + 18, SLOT_LIGHT);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INSIDE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int dark = 0xFF404040;
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.input"), 8, 6, dark, false);
        graphics.drawString(
                font, Component.translatable("dialog.c0de.trigger.output"), 8, IoMenu.OUTPUT_Y - 11, dark, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, dark, false);
        binding.renderLabels(graphics, font, PANEL_X + 2, 0, dark);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

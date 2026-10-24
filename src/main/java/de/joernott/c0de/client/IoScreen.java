package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.Panels;
import de.joernott.c0de.io.IoMenu;
import de.joernott.c0de.network.OpenDevicePayload;
import de.joernott.c0de.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The dialog of an I/O block (docs/concept/19-input-output.md): the binding with a button that opens the binding
 * dialog, the input and output inventories and the player inventory.
 */
public class IoScreen extends AbstractContainerScreen<IoMenu> {
    private static final int BACKGROUND = 0xFFC6C6C6;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_INSIDE = 0xFF8B8B8B;
    private static final int BUTTON_WIDTH = 60;

    public IoScreen(IoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = IoMenu.HOTBAR_Y + 24;
        inventoryLabelY = IoMenu.PLAYER_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("dialog.c0de.device"), button -> {
                    // The binding dialog replaces this one, so the menu is closed on the server first
                    onClose();
                    Platform.INSTANCE.sendToServer(new OpenDevicePayload(menu.pos()));
                })
                .bounds(leftPos + imageWidth - BUTTON_WIDTH - 6, topPos + 4, BUTTON_WIDTH, 14)
                .build());
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
        graphics.drawString(font, title, 8, 7, dark, false);
        Component binding = menu.program().isEmpty()
                ? Component.translatable("dialog.c0de.io.unbound")
                : Component.literal(menu.program() + " / " + menu.function());
        graphics.drawString(font, font.plainSubstrByWidth(binding.getString(), imageWidth - 16), 8, 18, dark, false);
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.input"), 8, IoMenu.INPUT_Y - 11, dark, false);
        graphics.drawString(
                font, Component.translatable("dialog.c0de.trigger.output"), 8, IoMenu.OUTPUT_Y - 11, dark, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, dark, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

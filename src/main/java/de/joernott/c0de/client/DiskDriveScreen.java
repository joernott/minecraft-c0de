package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.Panels;
import de.joernott.c0de.disk.DiskDriveMenu;
import de.joernott.c0de.item.DiskItem;
import de.joernott.c0de.network.DiskUploadPayload;
import de.joernott.c0de.platform.Platform;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The dialog of a disk drive (task 11.11.9): the slot of the disk, Save, which writes the program of the disk into
 * the folder {@code c0de} of this Minecraft, and Load, which lists the programs of that folder and loads the chosen
 * one onto the disk.
 */
public class DiskDriveScreen extends AbstractContainerScreen<DiskDriveMenu> {
    private static final int BACKGROUND = 0xFFC6C6C6;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_INSIDE = 0xFF8B8B8B;
    private static final int PAGE_SIZE = 5;

    private Button save;
    private Button load;
    private Component message = Component.empty();
    /** The programs of the folder while Load shows them, or null. */
    private List<String> files;
    private int page;

    public DiskDriveScreen(DiskDriveMenu menu, Inventory inventory, Component title) {
        //? if >=26.1 {
        /*super(menu, inventory, title, 176, DiskDriveMenu.HOTBAR_Y + 24);
        *///?} else {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = DiskDriveMenu.HOTBAR_Y + 24;
        //?}
        inventoryLabelY = DiskDriveMenu.PLAYER_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        save = addRenderableWidget(Button.builder(Component.translatable("dialog.c0de.disk_drive.save"), button -> save())
                .bounds(leftPos + 8, topPos + DiskDriveMenu.DISK_Y - 1, 60, 20)
                .build());
        load = addRenderableWidget(Button.builder(Component.translatable("dialog.c0de.disk_drive.load"), button -> list())
                .bounds(leftPos + imageWidth - 68, topPos + DiskDriveMenu.DISK_Y - 1, 60, 20)
                .build());
        if (files != null) {
            addFileButtons();
        }
    }

    /** Writes the program of the disk into the folder; the client already has it with the item. */
    private void save() {
        DiskItem.Contents contents = DiskItem.Contents.of(menu.disk());
        try {
            LocalPrograms.write(contents.name(), contents.yaml());
            message = Component.translatable("dialog.c0de.disk_drive.saved", "c0de/" + contents.name() + ".yaml")
                    .withStyle(ChatFormatting.DARK_GREEN);
        } catch (IOException e) {
            message = Component.literal(String.valueOf(e.getMessage())).withStyle(ChatFormatting.DARK_RED);
        }
    }

    /** Shows the programs of the folder as buttons. */
    private void list() {
        try {
            files = LocalPrograms.list();
            page = 0;
            message = files.isEmpty()
                    ? Component.translatable("dialog.c0de.disk_drive.no_files")
                    : Component.translatable("dialog.c0de.disk_drive.choose");
        } catch (IOException e) {
            files = null;
            message = Component.literal(String.valueOf(e.getMessage())).withStyle(ChatFormatting.DARK_RED);
        }
        rebuildWidgets();
    }

    private void addFileButtons() {
        int x = leftPos + imageWidth + 4;
        int y = topPos;
        for (int i = page * PAGE_SIZE; i < files.size() && i < (page + 1) * PAGE_SIZE; i++) {
            String file = files.get(i);
            addRenderableWidget(Button.builder(Component.literal(file), button -> upload(file))
                    .bounds(x, y, 120, 20)
                    .build());
            y += 22;
        }
        int pages = (files.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), button -> turn(-1))
                    .bounds(x, y, 20, 20)
                    .build())
                    .active = page > 0;
            addRenderableWidget(Button.builder(Component.literal(">"), button -> turn(1))
                    .bounds(x + 24, y, 20, 20)
                    .build())
                    .active = page < pages - 1;
        }
    }

    private void turn(int pages) {
        page += pages;
        rebuildWidgets();
    }

    /** Sends the program to the server in parts; the server writes it onto the disk. */
    private void upload(String file) {
        try {
            String yaml = LocalPrograms.read(file);
            int length = DiskUploadPayload.CHUNK_LENGTH;
            for (int start = 0; start == 0 || start < yaml.length(); start += length) {
                int end = Math.min(yaml.length(), start + length);
                Platform.INSTANCE.sendToServer(
                        new DiskUploadPayload(start == 0, end == yaml.length(), yaml.substring(start, end)));
            }
            files = null;
            message = Component.translatable("dialog.c0de.disk_drive.loading", file);
        } catch (IOException e) {
            message = Component.literal(String.valueOf(e.getMessage())).withStyle(ChatFormatting.DARK_RED);
        }
        rebuildWidgets();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean disk = menu.disk().getItem() instanceof DiskItem;
        save.active = disk && !DiskItem.Contents.of(menu.disk()).isEmpty();
        load.active = disk;
    }

    //? if >=26.1 {
    /*@Override
    public void extractBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
    *///?} else {
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    //?}
        Panels.draw(graphics, leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFF555555, BACKGROUND);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, SLOT_DARK);
            graphics.fill(x + 1, y + 1, x + 18, y + 18, SLOT_LIGHT);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INSIDE);
        }
    }

    //? if >=26.1 {
    /*@Override
    protected void extractLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    *///?} else {
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    //?}
        int dark = 0xFF404040;
        graphics.drawString(font, title, 8, 7, dark, false);
        DiskItem.Contents contents = DiskItem.Contents.of(menu.disk());
        Component disk = menu.disk().isEmpty()
                ? Component.translatable("dialog.c0de.disk_drive.no_disk")
                : contents.isEmpty()
                        ? Component.translatable("item.c0de.c0de_disk.empty")
                        : Component.translatable("item.c0de.c0de_disk.program", contents.name());
        graphics.drawString(font, disk, (imageWidth - font.width(disk)) / 2, DiskDriveMenu.DISK_Y + 22, dark, false);
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(message, imageWidth - 16);
        if (!lines.isEmpty()) {
            graphics.drawString(font, lines.get(0), 8, DiskDriveMenu.DISK_Y + 34, dark, false);
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, dark, false);
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        super.render(graphics, mouseX, mouseY, partialTick);
        //? if <26.1
        renderTooltip(graphics, mouseX, mouseY);
    }
}

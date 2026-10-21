package de.joernott.c0de.client.dialog;

import de.joernott.c0de.network.DiskPayload;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * The dialogs of the c0de disk: {@code disk_overwrite} asks whether an exported program may replace its file,
 * {@code disk_import} lists the program files to import, a page of buttons at a time.
 */
public class DiskScreen extends Screen {
    private static final int WIDTH = 240;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PAGE_SIZE = 8;

    private final OpenDialogPayload payload;
    private final boolean importing;
    private final List<String> files = new ArrayList<>();
    private int page;

    public DiskScreen(OpenDialogPayload payload) {
        super(Component.translatable(
                payload.dialog().equals("disk_import") ? "dialog.c0de.disk.import" : "dialog.c0de.disk.overwrite"));
        this.payload = payload;
        this.importing = payload.dialog().equals("disk_import");
        payload.data().getList("files", Tag.TAG_STRING).forEach(file -> files.add(file.getAsString()));
    }

    @Override
    protected void init() {
        int left = (width - WIDTH) / 2;
        int y = height / 2 - (PAGE_SIZE / 2 + 1) * (BUTTON_HEIGHT + 2);
        if (importing) {
            for (int i = page * PAGE_SIZE; i < files.size() && i < (page + 1) * PAGE_SIZE; i++) {
                String file = files.get(i);
                addRenderableWidget(Button.builder(Component.literal(file), button -> send("import", file))
                        .bounds(left, y, WIDTH, BUTTON_HEIGHT)
                        .build());
                y += BUTTON_HEIGHT + 2;
            }
            y = height / 2 + (PAGE_SIZE / 2) * (BUTTON_HEIGHT + 2) + 4;
            int pages = Math.max(1, (files.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            if (pages > 1) {
                addRenderableWidget(Button.builder(Component.literal("<"), button -> turn(-1))
                        .bounds(left, y, 20, BUTTON_HEIGHT)
                        .build())
                        .active = page > 0;
                addRenderableWidget(Button.builder(Component.literal(">"), button -> turn(1))
                        .bounds(left + 24, y, 20, BUTTON_HEIGHT)
                        .build())
                        .active = page < pages - 1;
            }
        } else {
            y = height / 2;
            addRenderableWidget(Button.builder(
                            Component.translatable("dialog.c0de.disk.confirm"), button -> send("overwrite", ""))
                    .bounds(left, y, WIDTH / 2 - 2, BUTTON_HEIGHT)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + WIDTH / 2 + 2, y, WIDTH / 2 - 2, BUTTON_HEIGHT)
                .build());
    }

    private void turn(int pages) {
        page += pages;
        rebuildWidgets();
    }

    private void send(String action, String program) {
        Platform.INSTANCE.sendToServer(new DiskPayload(action, payload.pos(), program));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int top = height / 2 - (PAGE_SIZE / 2 + 2) * (BUTTON_HEIGHT + 2);
        graphics.drawCenteredString(font, title, width / 2, top, 0xFFFFFFFF);
        Component text = importing
                ? (files.isEmpty() ? Component.translatable("dialog.c0de.disk.empty") : null)
                : Component.translatable("dialog.c0de.disk.exists", payload.data().getString("program"));
        if (text != null) {
            graphics.drawCenteredString(font, text, width / 2, height / 2 - 20, 0xFFD0D0D0);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

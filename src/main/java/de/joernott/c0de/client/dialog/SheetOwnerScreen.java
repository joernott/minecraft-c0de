package de.joernott.c0de.client.dialog;

import de.joernott.c0de.network.GiveSheetPayload;
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
 * The dialog of the wrench on the corner of a sheet (task 11.11.7): the owner of the sheet and the players online,
 * a page of buttons at a time; clicking a player gives the sheet to them.
 */
public class SheetOwnerScreen extends Screen {
    private static final int WIDTH = 240;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PAGE_SIZE = 8;

    private final OpenDialogPayload payload;
    private final List<String> players = new ArrayList<>();
    private int page;

    public SheetOwnerScreen(OpenDialogPayload payload) {
        super(Component.translatable("dialog.c0de.sheet_owner", payload.data().getInt("sheet")));
        this.payload = payload;
        payload.data().getList("players", Tag.TAG_STRING).forEach(player -> players.add(player.getAsString()));
    }

    @Override
    protected void init() {
        int left = (width - WIDTH) / 2;
        int y = height / 2 - (PAGE_SIZE / 2 + 1) * (BUTTON_HEIGHT + 2);
        for (int i = page * PAGE_SIZE; i < players.size() && i < (page + 1) * PAGE_SIZE; i++) {
            String player = players.get(i);
            addRenderableWidget(Button.builder(Component.literal(player), button -> {
                        Platform.INSTANCE.sendToServer(new GiveSheetPayload(payload.data().getInt("sheet"), player));
                        onClose();
                    })
                    .bounds(left, y, WIDTH, BUTTON_HEIGHT)
                    .build());
            y += BUTTON_HEIGHT + 2;
        }
        y = height / 2 + (PAGE_SIZE / 2) * (BUTTON_HEIGHT + 2) + 4;
        int pages = Math.max(1, (players.size() + PAGE_SIZE - 1) / PAGE_SIZE);
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
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + WIDTH / 2 + 2, y, WIDTH / 2 - 2, BUTTON_HEIGHT)
                .build());
    }

    private void turn(int pages) {
        page += pages;
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int top = height / 2 - (PAGE_SIZE / 2 + 3) * (BUTTON_HEIGHT + 2);
        graphics.drawCenteredString(font, title, width / 2, top, 0xFFFFFFFF);
        String owner = payload.data().getString("owner");
        graphics.drawCenteredString(
                font,
                Component.translatable("dialog.c0de.sheet_owner.current", owner.isEmpty() ? "-" : owner),
                width / 2,
                top + 12,
                0xFFD0D0D0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

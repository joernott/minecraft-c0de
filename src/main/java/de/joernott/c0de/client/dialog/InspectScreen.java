package de.joernott.c0de.client.dialog;

import de.joernott.c0de.network.OpenDialogPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The read-only dialog of the debugging tool: the properties of a block or the properties and stack of a cursor,
 * one item per line. Long lists can be scrolled with the mouse wheel; Escape or a click outside closes it.
 */
public class InspectScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int MARGIN = 8;
    private static final int MAX_VISIBLE_LINES = 20;

    private final List<Component> items = new ArrayList<>();
    private List<FormattedCharSequence> lines = List.of();
    private int scroll;
    private int left;
    private int top;
    private int panelHeight;

    public InspectScreen(OpenDialogPayload payload) {
        super(Component.literal(payload.data().getString("title")));
        payload.data().getList("lines", Tag.TAG_STRING).forEach(line -> items.add(Component.literal(line.getAsString())));
    }

    @Override
    protected void init() {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        items.forEach(item -> wrapped.addAll(font.split(item, WIDTH - 2 * MARGIN)));
        lines = wrapped;
        panelHeight = 2 * MARGIN + 14 + Math.min(lines.size(), MAX_VISIBLE_LINES) * font.lineHeight;
        left = (width - WIDTH) / 2;
        top = (height - panelHeight) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left - 1, top - 1, left + WIDTH + 1, top + panelHeight + 1, 0xFF808080);
        graphics.fill(left, top, left + WIDTH, top + panelHeight, 0xF0202020);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, title, left + MARGIN, top + MARGIN, 0xFFFFFFFF);
        int y = top + MARGIN + 14;
        for (int i = scroll; i < lines.size() && i < scroll + MAX_VISIBLE_LINES; i++) {
            graphics.drawString(font, lines.get(i), left + MARGIN, y, 0xFFD0D0D0);
            y += font.lineHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maximum = Math.max(0, lines.size() - MAX_VISIBLE_LINES);
        scroll = Math.max(0, Math.min(maximum, scroll - (int) Math.signum(scrollY)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < left || mouseX >= left + WIDTH || mouseY < top || mouseY >= top + panelHeight) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package de.joernott.c0de.client.dialog;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * A scrollable list of choices, filtered by the text of an input field: only the choices that contain the text
 * are shown. Clicking a choice passes it on, e.g. into the input field. The mouse wheel scrolls the list.
 */
public class ChoiceList extends AbstractWidget {
    /** The height of a row in pixels. */
    public static final int ROW = 11;

    private static final int BACKGROUND = 0xF0101010;
    private static final int BORDER = 0xFF606060;
    private static final int HOVER = 0xFF3A3A3A;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int SCROLLBAR = 0xFF909090;
    private static final int SCROLLBAR_WIDTH = 3;

    private final Font font;
    private final List<String> choices;
    private final Consumer<String> picked;
    private List<String> shown;
    private int scroll;

    public ChoiceList(Font font, int x, int y, int width, int rows, List<String> choices, Consumer<String> picked) {
        super(x, y, width, rows * ROW + 2, Component.empty());
        this.font = font;
        this.choices = List.copyOf(choices);
        this.picked = picked;
        this.shown = this.choices;
    }

    /** Shows the choices that contain the text; all choices if the text is one of them or nothing matches. */
    public void filter(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> matching = choices.stream()
                .filter(choice -> choice.toLowerCase(Locale.ROOT).contains(lower))
                .toList();
        shown = matching.isEmpty() || (matching.size() == 1 && matching.get(0).equals(text)) ? choices : matching;
        scroll = 0;
    }

    private int rows() {
        return (height - 2) / ROW;
    }

    private int maxScroll() {
        return Math.max(0, shown.size() - rows());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + width, y + height, BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND);
        int rows = rows();
        for (int i = 0; i < rows && scroll + i < shown.size(); i++) {
            int top = y + 1 + i * ROW;
            if (isHovered() && mouseY >= top && mouseY < top + ROW) {
                graphics.fill(x + 1, top, x + width - 1 - SCROLLBAR_WIDTH, top + ROW, HOVER);
            }
            String choice = font.plainSubstrByWidth(shown.get(scroll + i), width - 6 - SCROLLBAR_WIDTH);
            graphics.drawString(font, choice, x + 3, top + 2, TEXT, false);
        }
        if (maxScroll() > 0) {
            int track = height - 2;
            int thumb = Math.max(6, track * rows / shown.size());
            int thumbTop = y + 1 + (track - thumb) * scroll / maxScroll();
            graphics.fill(x + width - 1 - SCROLLBAR_WIDTH, thumbTop, x + width - 1, thumbTop + thumb, SCROLLBAR);
        }
    }

    //? if >=1.21.9 {
    /*@Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        pick(event.x(), event.y());
    }
    *///?} else {
    @Override
    public void onClick(double mouseX, double mouseY) {
        pick(mouseX, mouseY);
    }
    //?}

    /** Picks the clicked choice. */
    private void pick(double mouseX, double mouseY) {
        int index = scroll + (int) ((mouseY - getY() - 1) / ROW);
        if (index >= 0 && index < shown.size() && mouseX < getX() + width - 1 - SCROLLBAR_WIDTH) {
            picked.accept(shown.get(index));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(scrollY)));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(String.join(", ", shown)));
    }
}

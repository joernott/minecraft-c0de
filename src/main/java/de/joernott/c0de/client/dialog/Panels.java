package de.joernott.c0de.client.dialog;

import net.minecraft.client.gui.GuiGraphics;

/** The panels of the dialogs: a filled area with a border of one pixel and slightly rounded corners. */
public final class Panels {
    /** How far each of the first rows of a corner is moved inwards, from the outermost row. */
    private static final int[] CORNER = {3, 1, 1};

    private Panels() {}

    /**
     * Draws a panel; the border lies around the area.
     *
     * @param left the x coordinate of the area inside the border
     * @param top the y coordinate of the area inside the border
     */
    public static void draw(GuiGraphics graphics, int left, int top, int width, int height, int border, int fill) {
        rounded(graphics, left - 1, top - 1, width + 2, height + 2, border);
        rounded(graphics, left, top, width, height, fill);
    }

    private static void rounded(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        for (int row = 0; row < height; row++) {
            int fromTop = Math.min(row, height - 1 - row);
            int inset = fromTop < CORNER.length ? CORNER[fromTop] : 0;
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }
}

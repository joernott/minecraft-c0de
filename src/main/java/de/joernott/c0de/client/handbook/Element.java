package de.joernott.c0de.client.handbook;

import de.joernott.c0de.client.GuiDraw;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * A part of a handbook page (docs/concept/21-handbooks.md): a line of text, a title, a line of a list of entries,
 * an image, a recipe or an item. Pages stack their elements from top to bottom.
 */
public interface Element {
    /** The colour of the text of the pages. */
    int TEXT = 0xFF000000;
    /** The colour of titles. */
    int TITLE = 0xFF404040;

    int height();

    /** Draws the element at x, y; elements under the mouse show their tooltip through the screen. */
    void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY);

    /** True if the element moves to the next page with the element after it, e.g. the name above a recipe. */
    default boolean keepWithNext() {
        return false;
    }

    /** Handles a click at mouseX, mouseY on the element drawn at x, y. */
    default boolean click(HandbookScreen screen, int x, int y, double mouseX, double mouseY) {
        return false;
    }

    /** Empty space. */
    record Space(int height) implements Element {
        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {}
    }

    /** A line of text, indented by {@code indent}, optionally with a bullet or number in front of it. */
    record Line(
            FormattedCharSequence text, int indent, @Nullable FormattedCharSequence prefix, int prefixX, boolean heading)
            implements Element {
        @Override
        public int height() {
            return 9;
        }

        @Override
        public boolean keepWithNext() {
            return heading;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            Font font = screen.font();
            if (prefix != null) {
                graphics.drawString(font, prefix, x + prefixX, y, TEXT, false);
            }
            graphics.drawString(font, text, x + indent, y, TEXT, false);
            if (mouseY >= y && mouseY < y + 9) {
                Style style = styleAt(font, text, mouseX - x - indent);
                if (style != null && style.getInsertion() != null) {
                    screen.linkTooltip(style.getInsertion());
                }
            }
        }

        @Override
        public boolean click(HandbookScreen screen, int x, int y, double mouseX, double mouseY) {
            Style style = styleAt(screen.font(), text, (int) mouseX - x - indent);
            return style != null && style.getInsertion() != null && screen.followLink(style.getInsertion());
        }
    }

    /** The title of a page: centered, on as many lines as it needs, with a line below it. */
    record Title(List<FormattedCharSequence> lines) implements Element {
        /** The title wrapped to the width of a page. */
        static Title of(Font font, int width, Component text) {
            List<FormattedCharSequence> lines = font.split(text, width);
            return new Title(lines.isEmpty() ? List.of(FormattedCharSequence.EMPTY) : List.copyOf(lines));
        }

        @Override
        public int height() {
            return 9 * lines.size() + 9;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            Font font = screen.font();
            for (int i = 0; i < lines.size(); i++) {
                FormattedCharSequence line = lines.get(i);
                graphics.drawString(font, line, x + (screen.pageWidth() - font.width(line)) / 2, y + 2 + 9 * i,
                        TITLE, false);
            }
            int rule = y + 4 + 9 * lines.size();
            graphics.fill(x + 4, rule, x + screen.pageWidth() - 4, rule + 1, 0xFFB8A27A);
        }
    }

    /**
     * A line of the index of categories or entries: icon and title, which wraps to more lines if it is long; a
     * click opens it.
     */
    record Link(String icon, Component title, Runnable open, int lines, int textWidth) implements Element {
        /** A line for a page of the given width; the title gets the width without the icon. */
        static Link of(Font font, int width, String icon, Component title, Runnable open) {
            int textWidth = width - 20;
            return new Link(icon, title, open, Math.max(1, font.split(title, textWidth).size()), textWidth);
        }

        @Override
        public int height() {
            return Math.max(18, 9 * lines + 2);
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            Font font = screen.font();
            int height = height();
            boolean hovered =
                    mouseX >= x && mouseX < x + screen.pageWidth() && mouseY >= y && mouseY < y + height;
            GuiDraw.item(graphics, font, stack(icon), x, y + (height - 16) / 2);
            Component text = hovered ? title.copy().withStyle(style -> style.withUnderlined(true)) : title;
            List<FormattedCharSequence> wrapped = font.split(text, textWidth);
            int top = y + (height - 9 * wrapped.size()) / 2 + 1;
            for (int i = 0; i < wrapped.size(); i++) {
                graphics.drawString(font, wrapped.get(i), x + 19, top + 9 * i, TEXT, false);
            }
        }

        @Override
        public boolean click(HandbookScreen screen, int x, int y, double mouseX, double mouseY) {
            open.run();
            return true;
        }
    }

    /** An image, as wide as the page or as its own width. */
    record Image(ResourceLocation texture, int textureWidth, int textureHeight, boolean fit, int pageWidth)
            implements Element {
        private int width() {
            return fit || textureWidth > pageWidth ? pageWidth : textureWidth;
        }

        @Override
        public int height() {
            return Math.max(1, textureHeight * width() / Math.max(1, textureWidth)) + 2;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            int width = width();
            int height = height() - 2;
            int left = x + (pageWidth - width) / 2;
            GuiDraw.blitScaled(graphics, texture, left, y, width, height, textureWidth, textureHeight);
        }
    }

    /** The crafting grid of a recipe with its result; slots with several items cycle through them. */
    record Recipe(String id, HandbookContent.@Nullable Recipe recipe) implements Element {
        @Override
        public int height() {
            return recipe == null ? 11 : 58;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            Font font = screen.font();
            if (recipe == null) {
                graphics.drawString(font, id, x, y, 0xFFAA0000, false);
                return;
            }
            int left = x + (screen.pageWidth() - 104) / 2;
            GuiDraw.blit(graphics, screen.texture(), left, y, 54, 186, 104, 54, 512, 256);
            long second = System.currentTimeMillis() / 1000;
            for (int slot = 0; slot < 9 && slot < recipe.slots().size(); slot++) {
                List<String> items = recipe.slots().get(slot);
                if (items.isEmpty()) {
                    continue;
                }
                ItemStack stack = stack(items.get((int) (second % items.size())));
                slot(graphics, screen, stack, left + 2 + 17 * (slot % 3), y + 2 + 17 * (slot / 3), mouseX, mouseY);
            }
            ItemStack result = stack(recipe.result());
            result.setCount(Math.max(1, recipe.count()));
            slot(graphics, screen, result, left + 83, y + 19, mouseX, mouseY);
        }

        private static void slot(
                GuiGraphics graphics, HandbookScreen screen, ItemStack stack, int x, int y, int mouseX, int mouseY) {
            GuiDraw.item(graphics, screen.font(), stack, x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                screen.itemTooltip(stack);
            }
        }
    }

    /** An item: its icon and name. */
    record ItemRow(String id) implements Element {
        @Override
        public int height() {
            return 18;
        }

        @Override
        public boolean keepWithNext() {
            return true;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            ItemStack stack = stack(id);
            Font font = screen.font();
            GuiDraw.item(graphics, font, stack, x, y + 1);
            FormattedCharSequence name = font.split(stack.getHoverName(), screen.pageWidth() - 20).get(0);
            graphics.drawString(font, name, x + 19, y + 5, TEXT, false);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 17) {
                screen.itemTooltip(stack);
            }
        }
    }

    /** A horizontal line. */
    record Rule() implements Element {
        @Override
        public int height() {
            return 7;
        }

        @Override
        public void draw(GuiGraphics graphics, HandbookScreen screen, int x, int y, int mouseX, int mouseY) {
            graphics.fill(x + 10, y + 3, x + screen.pageWidth() - 10, y + 4, 0xFF808080);
        }
    }

    /** The item of an ID, or a barrier if there is no such item. */
    static ItemStack stack(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        net.minecraft.world.item.Item item = location == null
                ? Items.BARRIER
                : BuiltInRegistries.ITEM.getOptional(location).orElse(Items.BARRIER);
        return new ItemStack(item);
    }

    /** The style of the character of a line at the given distance from its start, or null outside the line. */
    static @Nullable Style styleAt(Font font, FormattedCharSequence line, int offset) {
        if (offset < 0) {
            return null;
        }
        Style[] found = {null};
        int[] width = {0};
        line.accept((index, style, codePoint) -> {
            width[0] += font.width(FormattedCharSequence.codepoint(codePoint, style));
            if (width[0] > offset) {
                found[0] = style;
                return false;
            }
            return true;
        });
        return found[0];
    }
}

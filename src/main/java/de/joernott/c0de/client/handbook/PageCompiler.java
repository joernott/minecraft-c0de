package de.joernott.c0de.client.handbook;

import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;

/**
 * Lays out Markdown on the pages of a handbook (docs/concept/21-handbooks.md): text is wrapped to the width of a
 * page, lists and quotes are indented, a blank line leaves a small gap, and elements that don't fit on a page
 * start the next one. The result is a list of pages, each a list of elements from top to bottom.
 */
public final class PageCompiler implements MarkdownCompiler<List<List<Element>>> {
    /** The gap between paragraphs. */
    private static final int GAP = 4;
    private static final int LIST_INDENT = 8;
    private static final int QUOTE_INDENT = 6;

    private final Font font;
    private final int width;
    private final int height;
    private final Map<String, HandbookContent.Recipe> recipes;
    private final ResourceManager resources;

    private final List<List<Element>> pages = new ArrayList<>();
    private List<Element> page = new ArrayList<>();
    private int used;

    private final Deque<Style> styles = new ArrayDeque<>();
    private final List<Component> runs = new ArrayList<>();
    private final Deque<String> listPrefixes = new ArrayDeque<>();
    private boolean listItemStarted;
    private int quoteDepth;
    private boolean gapPending;

    public PageCompiler(
            Font font, int width, int height, Map<String, HandbookContent.Recipe> recipes, ResourceManager resources) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.recipes = recipes;
        this.resources = resources;
        styles.push(Style.EMPTY);
    }

    /** Adds an element before the Markdown, e.g. the title of an entry. */
    public void add(Element element) {
        flush();
        place(element);
    }

    @Override
    public void visitText(String text) {
        // Two or more line breaks end a paragraph; one is a line break within it
        String[] paragraphs = text.split("\n{2,}", -1);
        for (int i = 0; i < paragraphs.length; i++) {
            if (i > 0) {
                flush();
                gapPending = true;
            }
            if (!paragraphs[i].isEmpty()) {
                runs.add(Component.literal(paragraphs[i]).setStyle(styles.peek()));
            }
        }
    }

    @Override
    public void visitStyle(UnaryOperator<Style> style) {
        styles.push(style.apply(styles.peek()));
    }

    @Override
    public void visitStyleEnd() {
        styles.pop();
    }

    @Override
    public void visitBlockQuote() {
        flush();
        quoteDepth++;
        styles.push(styles.peek().withColor(ChatFormatting.DARK_GRAY).withItalic(true));
    }

    @Override
    public void visitBlockQuoteEnd() {
        flush();
        styles.pop();
        quoteDepth--;
        gapPending = true;
    }

    @Override
    public void visitHorizontalRule() {
        add(new Element.Rule());
    }

    @Override
    public void visitImage(ResourceLocation image, String description, boolean fit) {
        int[] size = pngSize(image);
        add(size == null ? text(Component.literal("[" + description + "]").withStyle(ChatFormatting.RED))
                : new Element.Image(image, size[0], size[1], fit, width));
    }

    @Override
    public void visitListItem(OptionalInt ordinal) {
        flush();
        listPrefixes.push(ordinal.isPresent() ? ordinal.getAsInt() + "." : "•");
        listItemStarted = true;
    }

    @Override
    public void visitListItemEnd() {
        flush();
        listPrefixes.pop();
        if (listPrefixes.isEmpty()) {
            gapPending = true;
        }
    }

    /**
     * A program example: every line as written, a c0de program in the colours of its keywords, other code dark
     * blue; long lines continue indented.
     */
    void visitCode(String language, List<String> lines) {
        flush();
        gap();
        int space = font.width(" ");
        for (String line : lines) {
            String stripped = line.stripLeading();
            int indent = Math.min(width / 2, (line.length() - stripped.length()) * space);
            String text = stripped.isEmpty() ? " " : stripped;
            MutableComponent code = language.equals("c0de")
                    ? C0deHighlighter.line(text)
                    : Component.literal(text).withStyle(ChatFormatting.DARK_BLUE);
            List<FormattedCharSequence> wrapped = font.split(code, width - indent - LIST_INDENT);
            for (int i = 0; i < wrapped.size(); i++) {
                place(new Element.Line(wrapped.get(i), indent + (i > 0 ? LIST_INDENT : 0), null, 0));
            }
        }
        gapPending = true;
    }

    void visitRecipe(String id) {
        flush();
        gap();
        place(new Element.Recipe(id, recipes.get(id)));
        gapPending = true;
    }

    void visitItem(String id) {
        flush();
        place(new Element.ItemRow(id));
    }

    void visitPageBreak() {
        flush();
        if (!page.isEmpty()) {
            newPage();
        }
        gapPending = false;
    }

    /**
     * The pages so far. The Markdown processor calls this after every piece of Markdown, and more elements can
     * follow, so the pages are copied and the current page stays open.
     */
    @Override
    public List<List<Element>> compile() {
        flush();
        List<List<Element>> result = new ArrayList<>(pages);
        if (!page.isEmpty() || result.isEmpty()) {
            result.add(List.copyOf(page));
        }
        return result;
    }

    @Override
    public String name() {
        return "c0de handbook pages";
    }

    /** The lines of a text, wrapped to the width of the page. */
    private Element text(Component text) {
        List<FormattedCharSequence> lines = font.split(text, width);
        return new Element.Line(lines.isEmpty() ? FormattedCharSequence.EMPTY : lines.get(0), 0, null, 0);
    }

    /** Places the text collected so far as lines, indented for lists and quotes. */
    private void flush() {
        MutableComponent text = Component.empty();
        boolean empty = true;
        for (Component run : runs) {
            text.append(run);
            empty = false;
        }
        runs.clear();
        String plain = text.getString();
        if (empty || plain.isBlank()) {
            return;
        }
        gap();
        int indent = quoteDepth * QUOTE_INDENT + listPrefixes.size() * LIST_INDENT;
        FormattedCharSequence prefix = null;
        int prefixX = 0;
        if (listItemStarted && !listPrefixes.isEmpty()) {
            String marker = listPrefixes.peek();
            prefix = FormattedCharSequence.forward(marker, Style.EMPTY);
            prefixX = indent - font.width(marker) - 2;
            if (marker.endsWith(".")) {
                indent = Math.max(indent, font.width(marker) + 2 + (listPrefixes.size() - 1) * LIST_INDENT);
                prefixX = indent - font.width(marker) - 2;
            }
        }
        listItemStarted = false;
        List<FormattedCharSequence> lines = font.split(trim(text), width - indent);
        for (int i = 0; i < lines.size(); i++) {
            place(new Element.Line(lines.get(i), indent, i == 0 ? prefix : null, prefixX));
        }
    }

    /** The text without the line breaks at its start and end. */
    private static Component trim(MutableComponent text) {
        String plain = text.getString();
        int start = 0;
        while (start < plain.length() && plain.charAt(start) == '\n') {
            start++;
        }
        int end = plain.length();
        while (end > start && (plain.charAt(end - 1) == '\n' || plain.charAt(end - 1) == ' ')) {
            end--;
        }
        if (start == 0 && end == plain.length()) {
            return text;
        }
        // Rebuild the text from its styled parts, cut to start..end
        MutableComponent result = Component.empty();
        int[] position = {0};
        int from = start;
        int to = end;
        text.visit((style, part) -> {
            int partStart = position[0];
            int partEnd = partStart + part.length();
            position[0] = partEnd;
            int cutStart = Math.max(from, partStart);
            int cutEnd = Math.min(to, partEnd);
            if (cutStart < cutEnd) {
                result.append(Component.literal(part.substring(cutStart - partStart, cutEnd - partStart))
                        .setStyle(style));
            }
            return java.util.Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    private void gap() {
        if (gapPending && !page.isEmpty()) {
            place(new Element.Space(GAP));
        }
        gapPending = false;
    }

    /** Puts an element on the current page, or on a new one if it doesn't fit. */
    private void place(Element element) {
        if (used + element.height() > height && !page.isEmpty()) {
            // An element that belongs to this one (e.g. the name of an item above its recipe) moves along
            Element previous = page.get(page.size() - 1);
            boolean along = previous.keepWithNext() && page.size() > 1;
            if (along) {
                page.remove(page.size() - 1);
            }
            newPage();
            if (along) {
                page.add(previous);
                used += previous.height();
            }
            if (element instanceof Element.Space) {
                return;
            }
        }
        page.add(element);
        used += element.height();
    }

    private void newPage() {
        pages.add(page);
        page = new ArrayList<>();
        used = 0;
    }

    /** The width and height of a PNG image, from its header, or null if it doesn't exist. */
    private int[] pngSize(ResourceLocation image) {
        var resource = resources.getResource(image);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = resource.get().open()) {
            byte[] header = stream.readNBytes(24);
            if (header.length < 24) {
                return null;
            }
            int imageWidth = ((header[16] & 0xFF) << 24) | ((header[17] & 0xFF) << 16) | ((header[18] & 0xFF) << 8)
                    | (header[19] & 0xFF);
            int imageHeight = ((header[20] & 0xFF) << 24) | ((header[21] & 0xFF) << 16) | ((header[22] & 0xFF) << 8)
                    | (header[23] & 0xFF);
            return new int[] {imageWidth, imageHeight};
        } catch (IOException e) {
            return null;
        }
    }
}

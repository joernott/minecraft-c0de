package de.joernott.c0de.client.handbook;

import com.mojang.blaze3d.platform.InputConstants;
import de.joernott.c0de.client.GuiDraw;
import de.joernott.c0de.client.handbook.md.MarkdownFeature;
import de.joernott.c0de.client.handbook.md.MarkdownProcessor;
import de.joernott.c0de.client.handbook.md.feature.BasicFormattingFeature;
import de.joernott.c0de.client.handbook.md.feature.BlockQuoteFeature;
import de.joernott.c0de.client.handbook.md.feature.ColorFeature;
import de.joernott.c0de.client.handbook.md.feature.ImageFeature;
import de.joernott.c0de.client.handbook.md.feature.KeybindFeature;
import de.joernott.c0de.client.handbook.md.feature.LinkFeature;
import de.joernott.c0de.client.handbook.md.feature.ListFeature;
import de.joernott.c0de.client.handbook.md.feature.TranslationsFeature;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * The screen of a handbook (docs/concept/21-handbooks.md): a two-page spread on the book texture of Lavender,
 * showing the landing page, a category or an entry. The arrows turn the pages, the back arrow returns to the
 * previous view, and the book remembers where it was open while the game runs.
 */
public class HandbookScreen extends Screen {
    /** The smallest book; on smaller screens the book is as large as the screen. */
    private static final int MIN_WIDTH = 267;
    private static final int MIN_HEIGHT = 180;
    /** The part of the screen the book takes, and the widest book relative to its height (lines stay readable). */
    private static final double SHARE = 0.8;
    private static final double MAX_ASPECT = 1.75;
    /** The red cover around the pages, the gap between them with the rings, the margin of the text. */
    private static final int COVER = 6;
    private static final int GAP = 10;
    private static final int PADDING = 8;
    /** The space below the text of a page for the page number and the arrows. */
    private static final int FOOTER = 14;
    private static final int COVER_EDGE = 0xFF4A0B0E;
    private static final int COVER_RED = 0xFFA8282D;
    private static final int GAP_DARK = 0xFF5A1014;
    private static final int PAPER = 0xFFF2E9D0;
    private static final int PAPER_EDGE = 0xFFDCCDA8;
    private static final int MAX_HISTORY = 50;

    /** What the book shows: the landing page, a category or an entry, and the spread of its pages. */
    private record View(Kind kind, String id, int spread) {}

    private enum Kind {
        LANDING,
        CATEGORY,
        ENTRY
    }

    /** Where each book was open, with the views to go back to, while the game runs. */
    private static final Map<String, Deque<View>> OPEN = new HashMap<>();

    private final HandbookContent.Book book;
    private final Deque<View> history;
    private View view;
    private List<List<Element>> pages = List.of();
    private int left;
    private int top;
    private int bookWidth;
    private int bookHeight;
    private int paperWidth;
    private int pageWidth;
    private int pageHeight;
    private int leftPage;
    private int rightPage;
    private int pageTop;
    private int footer;
    private ItemStack itemTooltip = ItemStack.EMPTY;
    private List<FormattedCharSequence> textTooltip = List.of();

    private HandbookScreen(HandbookContent.Book book) {
        super(Component.translatable(book.name()));
        this.book = book;
        this.history = OPEN.computeIfAbsent(book.id(), id -> new ArrayDeque<>());
        View remembered = history.pollLast();
        this.view = remembered != null ? remembered : new View(Kind.LANDING, "", 0);
    }

    /** Opens a handbook, e.g. {@code guide}; logs a warning if its content is missing. */
    public static void open(String bookId) {
        Minecraft minecraft = Minecraft.getInstance();
        String language = minecraft.getLanguageManager().getSelected();
        HandbookContent.load(minecraft.getResourceManager(), bookId, language).ifPresentOrElse(book -> {
            de.joernott.c0de.client.ClientCompat.setScreen(new HandbookScreen(book));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }, () -> de.joernott.c0de.C0de.LOGGER.warn("The handbook {} has no content", bookId));
    }

    public Font font() {
        return font;
    }

    public ResourceLocation texture() {
        return book.texture();
    }

    /** The width of the text of a page, which grows with the screen. */
    public int pageWidth() {
        return pageWidth;
    }

    @Override
    protected void init() {
        // About 80 % of the screen, not wider than MAX_ASPECT times the height, at least the size of a small book
        bookHeight = Math.min(height, Math.max(MIN_HEIGHT, (int) (height * SHARE)));
        bookWidth = Math.min(width, Math.max(MIN_WIDTH, Math.min((int) (width * SHARE), (int) (bookHeight * MAX_ASPECT))));
        left = (width - bookWidth) / 2;
        top = (height - bookHeight) / 2;
        paperWidth = (bookWidth - 2 * COVER - GAP) / 2;
        pageWidth = paperWidth - 2 * PADDING;
        leftPage = left + COVER + PADDING;
        rightPage = left + COVER + paperWidth + GAP + PADDING;
        pageTop = top + COVER + PADDING;
        pageHeight = bookHeight - 2 * COVER - 2 * PADDING - FOOTER;
        footer = pageTop + pageHeight + 4;
        // Back from a confirmation of a link, removed() has stored the current view
        if (view.equals(history.peekLast())) {
            history.pollLast();
        }
        show(view);
    }

    /** Lays out the pages of a view. */
    private void show(View next) {
        View target = next;
        pages = layout(target);
        if (pages == null) {
            target = new View(Kind.LANDING, "", 0);
            pages = layout(target);
        }
        int spreads = (pages.size() + 1) / 2;
        view = new View(target.kind(), target.id(), Math.max(0, Math.min(target.spread(), spreads - 1)));
    }

    /** The pages of a view, or null if its category or entry doesn't exist (any more). */
    private List<List<Element>> layout(View target) {
        PageCompiler compiler = compiler();
        switch (target.kind()) {
            case LANDING -> {
                compiler.add(Element.Title.of(font, pageWidth, title));
                markdown(compiler, Component.translatable(book.landing()).getString());
                compiler.visitPageBreak();
                for (HandbookContent.Category category : book.categories()) {
                    compiler.add(Element.Link.of(font, pageWidth, category.icon(), Component.literal(category.title()),
                            () -> navigate(new View(Kind.CATEGORY, category.id(), 0))));
                }
            }
            case CATEGORY -> {
                HandbookContent.Category category = book.category(target.id()).orElse(null);
                if (category == null) {
                    return null;
                }
                compiler.add(Element.Title.of(font, pageWidth, Component.literal(category.title())));
                markdown(compiler, category.markdown());
                compiler.visitPageBreak();
                for (HandbookContent.Entry entry : category.entries()) {
                    compiler.add(Element.Link.of(font, pageWidth, entry.icon(), Component.literal(entry.title()),
                            () -> navigate(new View(Kind.ENTRY, entry.id(), 0))));
                }
            }
            case ENTRY -> {
                HandbookContent.Entry entry = book.entries().get(target.id());
                if (entry == null) {
                    return null;
                }
                compiler.add(Element.Title.of(font, pageWidth, Component.literal(entry.title())));
                markdown(compiler, entry.markdown());
            }
        }
        return compiler.compile();
    }

    private PageCompiler compiler() {
        return new PageCompiler(font, pageWidth, pageHeight, book.recipes(),
                Minecraft.getInstance().getResourceManager());
    }

    /** Lays out Markdown with the features of lavender-md and c0de. */
    private static void markdown(PageCompiler compiler, String markdown) {
        if (markdown.isBlank()) {
            return;
        }
        List<MarkdownFeature> features = new ArrayList<>(List.of(
                new BasicFormattingFeature(),
                new ColorFeature(),
                new LinkFeature(),
                new ListFeature(),
                new BlockQuoteFeature(),
                new ImageFeature(),
                new KeybindFeature(),
                new TranslationsFeature()));
        features.addAll(HandbookFeatures.all());
        try {
            MarkdownProcessor<List<List<Element>>> processor = new MarkdownProcessor<>(() -> compiler, features);
            processor.process(markdown);
        } catch (RuntimeException e) {
            de.joernott.c0de.C0de.LOGGER.warn("Handbook page can't be shown: {}", e.toString());
            compiler.visitText(markdown);
        }
    }

    /** Shows a category, e.g. {@code getting_started}, as if it was chosen on the landing page. */
    public void showCategory(String id) {
        navigate(new View(Kind.CATEGORY, id, 0));
    }

    /** Shows an entry, e.g. {@code editing_tools/wrench}, as if it was chosen in its category. */
    public void showEntry(String id) {
        navigate(new View(Kind.ENTRY, id, 0));
    }

    /** The number of pages of the current view. */
    public int pageCount() {
        return pages.size();
    }

    /** True if a page of the current view has links to categories or entries. */
    public boolean hasLinks(int page) {
        return page < pages.size() && pages.get(page).stream().anyMatch(element -> element instanceof Element.Link);
    }

    /** Opens another view and remembers the current one for the back arrow. */
    private void navigate(View next) {
        history.addLast(view);
        while (history.size() > MAX_HISTORY) {
            history.pollFirst();
        }
        show(next);
        pageSound();
    }

    private void back() {
        View previous = history.pollLast();
        if (previous == null && view.kind() != Kind.LANDING) {
            previous = view.kind() == Kind.ENTRY
                    ? new View(Kind.CATEGORY, book.entries().get(view.id()).category(), 0)
                    : new View(Kind.LANDING, "", 0);
        }
        if (previous != null) {
            show(previous);
            pageSound();
        }
    }

    private void turn(int spreads) {
        int last = Math.max(0, (pages.size() + 1) / 2 - 1);
        int spread = Math.max(0, Math.min(last, view.spread() + spreads));
        if (spread != view.spread()) {
            view = new View(view.kind(), view.id(), spread);
            pageSound();
        }
    }

    private void pageSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    /** Follows a link: {@code ^entry} opens an entry of the book, other links open in the browser. */
    boolean followLink(String insertion) {
        if (!insertion.startsWith(LinkFeature.LINK)) {
            return false;
        }
        String link = insertion.substring(LinkFeature.LINK.length());
        if (link.startsWith("^")) {
            book.entry(link.substring(1)).ifPresent(entry -> navigate(new View(Kind.ENTRY, entry.id(), 0)));
            return true;
        }
        try {
            URI uri = URI.create(link);
            if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
                ConfirmLinkScreen.confirmLinkNow(this, uri);
            }
        } catch (IllegalArgumentException e) {
            de.joernott.c0de.C0de.LOGGER.warn("Handbook link isn't valid: {}", link);
        }
        return true;
    }

    /** Shows the target of a link under the mouse. */
    void linkTooltip(String insertion) {
        if (insertion.startsWith(LinkFeature.LINK)) {
            String link = insertion.substring(LinkFeature.LINK.length());
            if (link.startsWith("^")) {
                link = book.entry(link.substring(1)).map(HandbookContent.Entry::title).orElse(link);
            }
            textTooltip = List.of(Component.literal(link).getVisualOrderText());
        }
    }

    void itemTooltip(ItemStack stack) {
        itemTooltip = stack;
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        super.render(graphics, mouseX, mouseY, partialTick);
        itemTooltip = ItemStack.EMPTY;
        textTooltip = List.of();
        drawBinder(graphics);
        drawPage(graphics, 2 * view.spread(), leftPage, mouseX, mouseY);
        drawPage(graphics, 2 * view.spread() + 1, rightPage, mouseX, mouseY);
        if (view.spread() > 0) {
            button(graphics, 0, previousX(), footer, mouseX, mouseY);
        }
        if (view.spread() < (pages.size() + 1) / 2 - 1) {
            button(graphics, 36, nextX(), footer, mouseX, mouseY);
        }
        if (view.kind() != Kind.LANDING) {
            button(graphics, 18, backX(), footer, mouseX, mouseY);
        }
        if (!itemTooltip.isEmpty()) {
            GuiDraw.tooltip(graphics, font, itemTooltip, mouseX, mouseY);
        } else if (!textTooltip.isEmpty()) {
            GuiDraw.tooltip(graphics, font, textTooltip, mouseX, mouseY);
        }
    }

    /**
     * The open binder: a red cover, two pages with a small gap between them, and four metal rings across the gap.
     */
    private void drawBinder(GuiGraphics graphics) {
        graphics.fill(left, top, left + bookWidth, top + bookHeight, COVER_EDGE);
        graphics.fill(left + 1, top + 1, left + bookWidth - 1, top + bookHeight - 1, COVER_RED);
        int paperTop = top + COVER;
        int paperBottom = top + bookHeight - COVER;
        int leftPaper = left + COVER;
        int rightPaper = leftPaper + paperWidth + GAP;
        // The gap between the pages shows the darker inside of the cover
        graphics.fill(leftPaper + paperWidth, paperTop, rightPaper, paperBottom, GAP_DARK);
        for (int paper : new int[] {leftPaper, rightPaper}) {
            graphics.fill(paper, paperTop, paper + paperWidth, paperBottom, PAPER_EDGE);
            graphics.fill(paper + 1, paperTop + 1, paper + paperWidth - 1, paperBottom - 1, PAPER);
        }
        // Four rings, evenly spaced, reaching a little into both pages through their holes
        int ringLeft = leftPaper + paperWidth - 6;
        int ringRight = rightPaper + 6;
        for (int ring = 0; ring < 4; ring++) {
            int y = paperTop + (paperBottom - paperTop) * (2 * ring + 1) / 8 - 2;
            graphics.fill(ringLeft - 1, y - 1, ringLeft + 2, y + 5, 0xFF3A3A3A);
            graphics.fill(ringRight - 2, y - 1, ringRight + 1, y + 5, 0xFF3A3A3A);
            graphics.fill(ringLeft, y, ringRight, y + 4, 0xFF6E6E6E);
            graphics.fill(ringLeft, y, ringRight, y + 1, 0xFFE6E6E6);
            graphics.fill(ringLeft, y + 1, ringRight, y + 3, 0xFFB4B4B4);
        }
    }

    private int previousX() {
        return leftPage;
    }

    private int backX() {
        return leftPage + pageWidth - 18;
    }

    private int nextX() {
        return rightPage + pageWidth - 18;
    }

    private void drawPage(GuiGraphics graphics, int index, int x, int mouseX, int mouseY) {
        if (index >= pages.size()) {
            return;
        }
        int y = pageTop;
        for (Element element : pages.get(index)) {
            element.draw(graphics, this, x, y, mouseX, mouseY);
            y += element.height();
        }
        String number = Integer.toString(index + 1);
        graphics.drawString(font, number, x + (pageWidth - font.width(number)) / 2, footer + 1, 0xFF808080, false);
    }

    /** An arrow of the book texture: u is its column; the lower row is the arrow under the mouse. */
    private void button(GuiGraphics graphics, int u, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 10;
        GuiDraw.blit(graphics, book.texture(), x, y, u, hovered ? 190 : 180, 18, 10, 512, 256);
    }

    private boolean click(double mouseX, double mouseY) {
        if (view.spread() > 0 && over(mouseX, mouseY, previousX(), footer)) {
            turn(-1);
            return true;
        }
        if (view.spread() < (pages.size() + 1) / 2 - 1 && over(mouseX, mouseY, nextX(), footer)) {
            turn(1);
            return true;
        }
        if (view.kind() != Kind.LANDING && over(mouseX, mouseY, backX(), footer)) {
            back();
            return true;
        }
        return clickPage(2 * view.spread(), leftPage, mouseX, mouseY)
                || clickPage(2 * view.spread() + 1, rightPage, mouseX, mouseY);
    }

    private static boolean over(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 10;
    }

    private boolean clickPage(int index, int x, double mouseX, double mouseY) {
        if (index >= pages.size() || mouseX < x || mouseX >= x + pageWidth) {
            return false;
        }
        int y = pageTop;
        for (Element element : pages.get(index)) {
            if (mouseY >= y && mouseY < y + element.height()) {
                return element.click(this, x, y, mouseX, mouseY);
            }
            y += element.height();
        }
        return false;
    }

    private boolean key(int key) {
        switch (key) {
            case InputConstants.KEY_LEFT, InputConstants.KEY_PAGEUP -> turn(-1);
            case InputConstants.KEY_RIGHT, InputConstants.KEY_PAGEDOWN -> turn(1);
            case InputConstants.KEY_BACKSPACE -> back();
            default -> {
                return false;
            }
        }
        return true;
    }

    //? if >=1.21.9 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        return (event.button() == 0 && click(event.x(), event.y())) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        return key(event.key()) || super.keyPressed(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (button == 0 && click(mouseX, mouseY)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return key(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        turn(scrollY < 0 ? 1 : -1);
        return true;
    }

    @Override
    public void removed() {
        // The view to open the next time, after the views to go back to
        history.addLast(view);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

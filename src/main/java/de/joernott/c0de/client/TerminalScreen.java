package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.Panels;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.io.ScreenNbt;
import de.joernott.c0de.io.Terminals;
import de.joernott.c0de.network.TerminalActionPayload;
import de.joernott.c0de.network.TerminalPayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The terminal of a laptop or tablet (docs/concept/19-input-output.md): the binding at the top, the screen with
 * its history and a scroll bar, and the input line, which is only available while an INPUT waits. Keys typed
 * while no INPUT waits go to INKEY.
 */
public class TerminalScreen extends Screen {
    private static final int BORDER = 6;
    private static final int SCROLL_BAR = 6;
    private static final int TOP = 26;
    private static final int SCREEN_COLOR = 0xFF141414;

    private final UUID id;
    private final CompoundTag programs;
    private final TextScreen screen = new TextScreen(Terminals.COLUMNS, Terminals.ROWS, Terminals.HISTORY);
    private String program;
    private String function;
    private String prompt;
    /** How many rows the view is scrolled up from the newest row. */
    private int scroll;
    private boolean draggingScrollBar;
    private int cellWidth;
    private int cellHeight;
    private int left;
    private int top;
    private Button programButton;
    private Button functionButton;
    private Button save;
    private EditBox input;

    private TerminalScreen(TerminalPayload payload) {
        super(Component.translatable("dialog.c0de.terminal"));
        id = payload.id();
        programs = payload.data().getCompound("programs");
        update(payload.data());
    }

    /** Opens a terminal or updates the open one. */
    public static void receive(TerminalPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new TerminalScreen(payload));
        } else if (minecraft.screen instanceof TerminalScreen terminal && terminal.id.equals(payload.id())) {
            terminal.update(payload.data());
        }
    }

    private void update(CompoundTag data) {
        ScreenNbt.apply(screen, data.getCompound("screen"));
        program = data.getString("program");
        function = data.getString("function");
        prompt = data.getString("prompt");
        if (programButton != null) {
            updateWidgets();
        }
    }

    @Override
    protected void init() {
        cellWidth = Math.max(1, font.width(Component.literal("M").withStyle(DisplayRenderer.STYLE)));
        cellHeight = font.lineHeight + 1;
        int screenWidth = Terminals.COLUMNS * cellWidth;
        int screenHeight = Terminals.ROWS * cellHeight;
        int panelWidth = screenWidth + SCROLL_BAR + 3 * BORDER;
        int panelHeight = TOP + screenHeight + 40;
        left = (width - panelWidth) / 2 + BORDER;
        top = Math.max(BORDER, (height - panelHeight) / 2) + TOP;
        int buttonWidth = (screenWidth - 24) / 2;
        programButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    program = next(programNames(), program);
                    List<String> functions = functionNames(program);
                    function = functions.isEmpty() ? "" : functions.get(0);
                    updateWidgets();
                })
                .bounds(left, top - TOP + 2, buttonWidth, 18)
                .build());
        functionButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    function = next(functionNames(program), function);
                    updateWidgets();
                })
                .bounds(left + buttonWidth + 2, top - TOP + 2, buttonWidth, 18)
                .build());
        save = addRenderableWidget(Button.builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), button -> {
                    Platform.INSTANCE.sendToServer(new TerminalActionPayload("bind", program, function));
                    onClose();
                })
                .bounds(left + screenWidth + SCROLL_BAR + BORDER - 20, top - TOP + 2, 20, 18)
                .build());
        input = addRenderableWidget(new EditBox(
                font, left, top + screenHeight + 18, screenWidth + SCROLL_BAR + BORDER, 16, Component.empty()));
        input.setMaxLength(1024);
        updateWidgets();
    }

    private void updateWidgets() {
        programButton.setMessage(Component.literal(program.isEmpty() ? "–" : program));
        functionButton.setMessage(Component.literal(function.isEmpty() ? "–" : function));
        functionButton.active = !program.isEmpty();
        save.active = program.isEmpty() || !function.isEmpty();
        boolean waiting = !prompt.isEmpty();
        input.setEditable(waiting);
        input.active = waiting;
        if (waiting && getFocused() != input) {
            setFocused(input);
        } else if (!waiting) {
            input.setValue("");
            setFocused(null);
        }
    }

    private List<String> programNames() {
        List<String> names = new ArrayList<>(programs.getAllKeys());
        names.sort(String::compareTo);
        names.add(0, "");
        return names;
    }

    private List<String> functionNames(String name) {
        List<String> names = new ArrayList<>();
        programs.getList(name, Tag.TAG_STRING).forEach(f -> names.add(f.getAsString()));
        return names;
    }

    private static String next(List<String> values, String current) {
        int index = values.indexOf(current);
        return values.isEmpty() ? "" : values.get((index + 1) % values.size());
    }

    // ------------------------------------------------------------------ keys

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (!prompt.isEmpty() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            Platform.INSTANCE.sendToServer(new TerminalActionPayload("line", input.getValue(), ""));
            input.setValue("");
            scroll = 0;
            return true;
        }
        if (prompt.isEmpty() && getFocused() == null) {
            String key = switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> "\r";
                case GLFW.GLFW_KEY_BACKSPACE -> "\b";
                case GLFW.GLFW_KEY_TAB -> "\t";
                default -> null;
            };
            if (key != null) {
                sendKey(key);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (prompt.isEmpty() && getFocused() == null) {
            sendKey(String.valueOf(character));
            return true;
        }
        return super.charTyped(character, modifiers);
    }

    private void sendKey(String key) {
        Platform.INSTANCE.sendToServer(new TerminalActionPayload("key", key, ""));
    }

    // ------------------------------------------------------------------ scrolling

    private List<TextScreen.Row> allRows() {
        List<TextScreen.Row> rows = new ArrayList<>(screen.history());
        rows.addAll(screen.rows());
        return rows;
    }

    private int maxScroll() {
        return screen.history().size();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll + (int) Math.signum(scrollY) * 3));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (onScrollBar(mouseX, mouseY)) {
            draggingScrollBar = true;
            scrollTo(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollBar) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int scrollBarX() {
        return left + Terminals.COLUMNS * cellWidth + BORDER / 2;
    }

    private boolean onScrollBar(double mouseX, double mouseY) {
        return mouseX >= scrollBarX()
                && mouseX < scrollBarX() + SCROLL_BAR
                && mouseY >= top
                && mouseY < top + Terminals.ROWS * cellHeight;
    }

    private void scrollTo(double mouseY) {
        double fraction = (mouseY - top) / (Terminals.ROWS * cellHeight);
        int fromTop = (int) Math.round(fraction * maxScroll());
        scroll = Math.max(0, Math.min(maxScroll(), maxScroll() - fromTop));
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int screenWidth = Terminals.COLUMNS * cellWidth;
        int screenHeight = Terminals.ROWS * cellHeight;
        graphics.fill(left - 2, top - 2, left + screenWidth + 2, top + screenHeight + 2, SCREEN_COLOR);
        List<TextScreen.Row> rows = allRows();
        int first = rows.size() - Terminals.ROWS - scroll;
        for (int r = 0; r < Terminals.ROWS; r++) {
            int index = first + r;
            if (index >= 0 && index < rows.size()) {
                drawRow(graphics, rows.get(index), top + r * cellHeight);
            }
        }
        // The scroll bar: the visible part of the history and the screen
        int barX = scrollBarX();
        graphics.fill(barX, top, barX + SCROLL_BAR, top + screenHeight, 0xFF2A2A2A);
        int total = rows.size();
        int thumbHeight = Math.max(8, screenHeight * Terminals.ROWS / Math.max(1, total));
        int thumbTop = top + (int) ((long) (screenHeight - thumbHeight) * Math.max(0, first) / Math.max(1, total - Terminals.ROWS));
        graphics.fill(barX, thumbTop, barX + SCROLL_BAR, thumbTop + thumbHeight, 0xFFAAAAAA);
        Component label = prompt.isEmpty()
                ? Component.translatable(program.isEmpty() ? "dialog.c0de.io.unbound" : "dialog.c0de.terminal.keys")
                : Component.translatable("dialog.c0de.terminal.input", prompt);
        graphics.drawString(font, label, left, top + screenHeight + 6, 0xFFFFFFFF, false);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int screenWidth = Terminals.COLUMNS * cellWidth;
        int screenHeight = Terminals.ROWS * cellHeight;
        Panels.draw(
                graphics,
                left - BORDER,
                top - TOP,
                screenWidth + SCROLL_BAR + 3 * BORDER,
                TOP + screenHeight + 40,
                0xFF555555,
                0xFF3C3C3C);
    }

    private void drawRow(GuiGraphics graphics, TextScreen.Row row, int y) {
        int start = 0;
        int width = row.characters().length;
        while (start < width) {
            int foreground = row.foregrounds()[start];
            int background = row.backgrounds()[start];
            int end = start + 1;
            while (end < width && row.foregrounds()[end] == foreground && row.backgrounds()[end] == background) {
                end++;
            }
            int x = left + start * cellWidth;
            if (background != 0) {
                graphics.fill(x, y, left + end * cellWidth, y + cellHeight, DisplayRenderer.color(background));
            }
            for (int i = start; i < end; i++) {
                int character = row.characters()[i];
                if (character != ' ') {
                    graphics.drawString(
                            font,
                            Component.literal(Character.toString(character)).withStyle(DisplayRenderer.STYLE),
                            left + i * cellWidth,
                            y + 1,
                            DisplayRenderer.color(foreground),
                            false);
                }
            }
            start = end;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        // The ID: a terminal opened again before this one was closed stays open
        Platform.INSTANCE.sendToServer(new TerminalActionPayload("close", id.toString(), ""));
        super.removed();
    }
}

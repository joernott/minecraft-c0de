package de.joernott.c0de.client;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.client.dialog.Panels;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.menu.TriggerMenu;
import de.joernott.c0de.network.TriggerConfigPayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The dialog of a trigger block (docs/concept/13-running-programs.md): the input and output inventories on the
 * left; the program, function, interval (timer block only), state of the process and the camouflage slot on the
 * right. Clicking the program or function cycles through the programs of the c0de dimension and their
 * functions.
 */
public class TriggerScreen extends AbstractContainerScreen<TriggerMenu> {
    private static final int PANEL_X = 180;
    private static final int PANEL_WIDTH = 140;
    private static final int BACKGROUND = 0xFFC6C6C6;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_INSIDE = 0xFF8B8B8B;
    /** The box of the messages: below the camouflage slot, above the save button. */
    private static final int MESSAGES_Y = TriggerMenu.CAMOUFLAGE_Y + 24;
    private static final int MESSAGES_WIDTH = PANEL_WIDTH - 6;
    private static final int SCROLL_BAR = 4;
    private static final int LINE_HEIGHT = 10;

    private final CompoundTag programs;
    private final boolean timer;
    private String program;
    private String function;
    private Button programButton;
    private Button functionButton;
    private EditBox interval;
    private TriggerBlockEntity.Run run;
    private Button runButton;
    private Button save;
    /** The first message line shown. */
    private int scroll;

    public TriggerScreen(TriggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        CompoundTag data = menu.data();
        programs = Nbt.compound(data, "programs");
        timer = TriggerBlock.Kind.TIMER.name().equals(Nbt.string(data, "kind"));
        program = Nbt.string(data, "program");
        function = Nbt.string(data, "function");
        run = TriggerBlockEntity.Run.of(Nbt.string(data, "run"));
        imageWidth = PANEL_X + PANEL_WIDTH;
        imageHeight = TriggerMenu.HOTBAR_Y + 24;
        inventoryLabelY = TriggerMenu.PLAYER_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + PANEL_X + 2;
        programButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    program = next(programNames(), program);
                    List<String> functions = functionNames(program);
                    function = functions.isEmpty() ? "" : functions.get(0);
                    updateLabels();
                })
                .bounds(x, topPos + 18, PANEL_WIDTH - 6, 20)
                .build());
        functionButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    function = next(functionNames(program), function);
                    updateLabels();
                })
                .bounds(x, topPos + 52, PANEL_WIDTH - 6, 20)
                .build());
        if (timer) {
            interval = addRenderableWidget(
                    new EditBox(font, x, topPos + 86, PANEL_WIDTH - 6, 18, Component.empty()));
            interval.setValue(Integer.toString(Nbt.integer(menu.data(), "interval")));
            interval.setFilter(text -> text.matches("\\d{0,5}"));
        } else {
            // Who may start the block (task 11.11.8); timer blocks start themselves
            runButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                        TriggerBlockEntity.Run[] values = TriggerBlockEntity.Run.values();
                        run = values[(run.ordinal() + 1) % values.length];
                        updateLabels();
                    })
                    .bounds(x, topPos + 86, PANEL_WIDTH - 6, 20)
                    .build());
        }
        save = addRenderableWidget(Button.builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), button -> {
                    Platform.INSTANCE.sendToServer(
                            new TriggerConfigPayload(menu.pos(), program, function, intervalValue(), run.name()));
                    onClose();
                })
                // In the lower right corner
                .bounds(leftPos + imageWidth - 26, topPos + imageHeight - 26, 20, 20)
                .build());
        updateLabels();
    }

    private int intervalValue() {
        if (interval == null || interval.getValue().isEmpty()) {
            return Nbt.integer(menu.data(), "interval");
        }
        return Integer.parseInt(interval.getValue());
    }

    private List<String> programNames() {
        List<String> names = new ArrayList<>(Nbt.keys(programs));
        names.sort(String::compareTo);
        names.add(0, "");
        return names;
    }

    private List<String> functionNames(String name) {
        List<String> names = new ArrayList<>();
        Nbt.list(programs, name, Tag.TAG_STRING).forEach(f -> names.add(Nbt.text(f)));
        return names;
    }

    private static String next(List<String> values, String current) {
        if (values.isEmpty()) {
            return "";
        }
        int index = values.indexOf(current);
        return values.get((index + 1) % values.size());
    }

    private void updateLabels() {
        programButton.setMessage(Component.literal(program.isEmpty() ? "–" : program));
        functionButton.setMessage(Component.literal(function.isEmpty() ? "–" : function));
        if (runButton != null) {
            runButton.setMessage(Component.translatable("dialog.c0de.trigger.run." + run.name().toLowerCase(java.util.Locale.ROOT)));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The configuration is locked while a process is running and for players who are not the owner
        boolean locked = menu.pid() >= 0 || !menu.editable();
        programButton.active = !locked;
        functionButton.active = !locked && !program.isEmpty();
        if (interval != null) {
            interval.setEditable(!locked);
        }
        if (runButton != null) {
            runButton.active = !locked;
        }
        int value = interval == null || interval.getValue().isEmpty() ? 1 : Integer.parseInt(interval.getValue());
        save.active = !locked
                && !function.isEmpty()
                && value >= TriggerBlockEntity.MIN_INTERVAL
                && value <= TriggerBlockEntity.MAX_INTERVAL;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        Panels.draw(graphics, leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFF555555, BACKGROUND);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, SLOT_DARK);
            graphics.fill(x + 1, y + 1, x + 18, y + 18, SLOT_LIGHT);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INSIDE);
        }
        if (!menu.editable()) {
            // The camouflage slot is greyed out for players who may not change it
            int x = leftPos + TriggerMenu.CAMOUFLAGE_X;
            int y = topPos + TriggerMenu.CAMOUFLAGE_Y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x99C6C6C6);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int dark = 0xFF404040;
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.input"), 8, 6, dark, false);
        graphics.drawString(
                font, Component.translatable("dialog.c0de.trigger.output"), 8, TriggerMenu.OUTPUT_Y - 11, dark, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, dark, false);
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.program"), PANEL_X + 2, 8, dark, false);
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.function"), PANEL_X + 2, 42, dark, false);
        if (timer) {
            graphics.drawString(font, Component.translatable("dialog.c0de.trigger.interval"), PANEL_X + 2, 76, dark, false);
        } else {
            graphics.drawString(font, Component.translatable("dialog.c0de.trigger.run"), PANEL_X + 2, 76, dark, false);
        }
        graphics.drawString(
                font,
                Component.translatable("dialog.c0de.trigger.camouflage"),
                TriggerMenu.CAMOUFLAGE_X + 22,
                TriggerMenu.CAMOUFLAGE_Y + 5,
                dark,
                false);
    }

    // ------------------------------------------------------------------ messages

    /** The state of the block: PID or exit code, the error of the last process, the status and the owner. */
    private List<Component> messages() {
        List<Component> state = new ArrayList<>();
        if (menu.pid() >= 0) {
            state.add(Component.translatable("dialog.c0de.trigger.pid", menu.pid()));
        } else if (menu.exitCode() >= 0) {
            state.add(Component.translatable("dialog.c0de.trigger.exit_code", menu.exitCode()));
            CompoundTag error = Nbt.compound(menu.data(), "error");
            if (!error.isEmpty() && menu.exitCode() == Nbt.integer(menu.data(), "exitCode")) {
                state.add(Component.literal(Nbt.string(error, "code") + " " + Nbt.string(error, "message")));
                state.add(Component.translatable(
                        "dialog.c0de.trigger.error_at", Nbt.string(error, "context"), Nbt.string(error, "line")));
            }
        }
        String status = Nbt.string(menu.data(), "status");
        if (!status.isEmpty() && menu.pid() < 0) {
            state.add(Component.literal(status).withStyle(ChatFormatting.DARK_RED));
        }
        String owner = Nbt.string(menu.data(), "owner");
        if (!owner.isEmpty()) {
            state.add(Component.translatable("dialog.c0de.trigger.owner", owner));
        }
        return state;
    }

    /** The lines of the messages, wrapped to the width of the message box. */
    private List<FormattedCharSequence> messageLines() {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component message : messages()) {
            lines.addAll(font.split(message, MESSAGES_WIDTH - SCROLL_BAR - 4));
        }
        return lines;
    }

    private int messagesLeft() {
        return leftPos + PANEL_X + 2;
    }

    private int messagesTop() {
        return topPos + MESSAGES_Y;
    }

    private int messagesHeight() {
        return imageHeight - MESSAGES_Y - 30;
    }

    private int maxScroll(int lines) {
        return Math.max(0, lines - messagesHeight() / LINE_HEIGHT);
    }

    /** The messages in a box with a scroll bar, so that long errors don't run over the other elements. */
    private void renderMessages(GuiGraphics graphics) {
        int left = messagesLeft();
        int top = messagesTop();
        int height = messagesHeight();
        graphics.fill(left - 1, top - 1, left + MESSAGES_WIDTH + 1, top + height + 1, SLOT_DARK);
        graphics.fill(left, top, left + MESSAGES_WIDTH, top + height, 0xFFDADADA);
        List<FormattedCharSequence> lines = messageLines();
        scroll = Math.min(scroll, maxScroll(lines.size()));
        graphics.enableScissor(left, top, left + MESSAGES_WIDTH - SCROLL_BAR, top + height);
        for (int i = scroll; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), left + 2, top + 2 + (i - scroll) * LINE_HEIGHT, 0xFF404040, false);
        }
        graphics.disableScissor();
        int visible = height / LINE_HEIGHT;
        if (lines.size() > visible) {
            int barX = left + MESSAGES_WIDTH - SCROLL_BAR;
            graphics.fill(barX, top, barX + SCROLL_BAR, top + height, SLOT_INSIDE);
            int thumb = Math.max(6, height * visible / lines.size());
            int thumbTop = top + (height - thumb) * scroll / Math.max(1, maxScroll(lines.size()));
            graphics.fill(barX, thumbTop, barX + SCROLL_BAR, thumbTop + thumb, SLOT_DARK);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= messagesLeft()
                && mouseX < messagesLeft() + MESSAGES_WIDTH
                && mouseY >= messagesTop()
                && mouseY < messagesTop() + messagesHeight()) {
            scroll = Math.max(0, Math.min(maxScroll(messageLines().size()), scroll - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderMessages(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

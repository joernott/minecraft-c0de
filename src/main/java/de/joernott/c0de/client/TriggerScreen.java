package de.joernott.c0de.client;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
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

    private final CompoundTag programs;
    private final boolean timer;
    private String program;
    private String function;
    private Button programButton;
    private Button functionButton;
    private EditBox interval;
    private Button save;

    public TriggerScreen(TriggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        CompoundTag data = menu.data();
        programs = data.getCompound("programs");
        timer = TriggerBlock.Kind.TIMER.name().equals(data.getString("kind"));
        program = data.getString("program");
        function = data.getString("function");
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
            interval.setValue(Integer.toString(menu.data().getInt("interval")));
            interval.setFilter(text -> text.matches("\\d{0,5}"));
        }
        save = addRenderableWidget(Button.builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), button -> {
                    Platform.INSTANCE.sendToServer(
                            new TriggerConfigPayload(menu.pos(), program, function, intervalValue()));
                    onClose();
                })
                .bounds(leftPos + imageWidth - 26, topPos + 110, 20, 20)
                .build());
        updateLabels();
    }

    private int intervalValue() {
        if (interval == null || interval.getValue().isEmpty()) {
            return menu.data().getInt("interval");
        }
        return Integer.parseInt(interval.getValue());
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
        if (values.isEmpty()) {
            return "";
        }
        int index = values.indexOf(current);
        return values.get((index + 1) % values.size());
    }

    private void updateLabels() {
        programButton.setMessage(Component.literal(program.isEmpty() ? "–" : program));
        functionButton.setMessage(Component.literal(function.isEmpty() ? "–" : function));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The configuration is locked while a process is running
        boolean running = menu.pid() >= 0;
        programButton.active = !running;
        functionButton.active = !running && !program.isEmpty();
        if (interval != null) {
            interval.setEditable(!running);
        }
        int value = interval == null || interval.getValue().isEmpty() ? 1 : Integer.parseInt(interval.getValue());
        save.active = !running
                && !function.isEmpty()
                && value >= TriggerBlockEntity.MIN_INTERVAL
                && value <= TriggerBlockEntity.MAX_INTERVAL;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, BACKGROUND);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, SLOT_DARK);
            graphics.fill(x + 1, y + 1, x + 18, y + 18, SLOT_LIGHT);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INSIDE);
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
        }
        graphics.drawString(
                font,
                Component.translatable("dialog.c0de.trigger.camouflage"),
                PANEL_X + 2,
                TriggerMenu.CAMOUFLAGE_Y - 11,
                dark,
                false);
        List<Component> state = new ArrayList<>();
        if (menu.pid() >= 0) {
            state.add(Component.translatable("dialog.c0de.trigger.pid", menu.pid()));
        } else if (menu.exitCode() >= 0) {
            state.add(Component.translatable("dialog.c0de.trigger.exit_code", menu.exitCode()));
            CompoundTag error = menu.data().getCompound("error");
            if (!error.isEmpty() && menu.exitCode() == menu.data().getInt("exitCode")) {
                state.add(Component.literal(error.getString("code") + " " + error.getString("message")));
                state.add(Component.translatable(
                        "dialog.c0de.trigger.error_at", error.getString("context"), error.getString("line")));
            }
        }
        String status = menu.data().getString("status");
        if (!status.isEmpty() && menu.pid() < 0) {
            state.add(Component.literal(status).withStyle(ChatFormatting.DARK_RED));
        }
        String owner = menu.data().getString("owner");
        if (!owner.isEmpty()) {
            state.add(Component.translatable("dialog.c0de.trigger.owner", owner));
        }
        int y = 136;
        for (Component line : state) {
            for (var part : font.split(line, PANEL_WIDTH - 6)) {
                graphics.drawString(font, part, PANEL_X + 2, y, dark, false);
                y += 10;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

package de.joernott.c0de.client;

import de.joernott.c0de.compat.Nbt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * The binding of a device in its dialog, laid out like the right panel of the trigger dialog: a button that cycles
 * through the programs of the c0de dimension, one that cycles through the functions of the program, and ✔, which
 * saves the binding. An empty program unbinds the device.
 */
public final class BindingPanel {
    public static final int WIDTH = 140;
    public static final int SAVE_Y = 76;

    private final CompoundTag programs;
    private String program;
    private String function;
    private Button programButton;
    private Button functionButton;
    private Button save;

    /** @param data the data of the dialog: {@code programs}, {@code program} and {@code function} */
    public BindingPanel(CompoundTag data) {
        programs = Nbt.compound(data, "programs");
        program = Nbt.string(data, "program");
        function = Nbt.string(data, "function");
    }

    public String program() {
        return program;
    }

    public String function() {
        return program.isEmpty() ? "" : function;
    }

    /**
     * Adds the buttons to a dialog.
     *
     * @param x the left edge of the panel on the screen
     * @param y the top edge of the panel on the screen
     * @param saved called by ✔
     */
    public void init(Function<Button, Button> add, int x, int y, Runnable saved) {
        programButton = add.apply(Button.builder(Component.empty(), button -> {
                    program = next(programNames(), program);
                    List<String> functions = functionNames(program);
                    function = functions.isEmpty() ? "" : functions.get(0);
                    update();
                })
                .bounds(x, y + 18, WIDTH - 6, 20)
                .build());
        functionButton = add.apply(Button.builder(Component.empty(), button -> {
                    function = next(functionNames(program), function);
                    update();
                })
                .bounds(x, y + 52, WIDTH - 6, 20)
                .build());
        save = add.apply(Button.builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), button -> saved.run())
                .bounds(x + WIDTH - 26, y + SAVE_Y + 4, 20, 20)
                .build());
        update();
    }

    /** Draws the captions, relative to the dialog (as in {@code renderLabels}). */
    public void renderLabels(GuiGraphics graphics, Font font, int x, int y, int color) {
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.program"), x, y + 8, color, false);
        graphics.drawString(font, Component.translatable("dialog.c0de.trigger.function"), x, y + 42, color, false);
    }

    private void update() {
        programButton.setMessage(Component.literal(program.isEmpty() ? "–" : program));
        functionButton.setMessage(Component.literal(function.isEmpty() ? "–" : function));
        functionButton.active = !program.isEmpty();
        save.active = program.isEmpty() || !function.isEmpty();
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
        int index = values.indexOf(current);
        return values.isEmpty() ? "" : values.get((index + 1) % values.size());
    }
}

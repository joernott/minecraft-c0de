package de.joernott.c0de.client.dialog;

import de.joernott.c0de.network.OpenDialogPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * The binding of a device (display, I/O block, laptop): the program and its function, each with a list of the
 * choices filtered by the entered text (docs/concept/19-input-output.md). An empty program unbinds the device.
 */
public class DeviceScreen extends PropertiesScreen {
    private static final int CHOICE_ROWS = 5;

    private final CompoundTag programs;
    private EditBox program;
    private EditBox function;
    private ChoiceList programChoices;
    private ChoiceList functionChoices;

    public DeviceScreen(OpenDialogPayload payload) {
        super(payload);
        programs = payload.data().getCompound("programs");
    }

    @Override
    protected int contentHeight() {
        return 2 * (ROW + CHOICE_ROWS * ChoiceList.ROW + 14);
    }

    private List<String> functions(String name) {
        List<String> functions = new ArrayList<>();
        programs.getList(name, Tag.TAG_STRING).forEach(function -> functions.add(function.getAsString()));
        return functions;
    }

    @Override
    protected void addFields() {
        int y = contentTop() + 10;
        program = addRenderableWidget(new EditBox(font, left + MARGIN, y, FIELD_WIDTH, 18, Component.empty()));
        program.setValue(payload.data().getString("program"));
        y += 20;
        programChoices = addRenderableWidget(new ChoiceList(
                font, left + MARGIN, y, FIELD_WIDTH, CHOICE_ROWS, new ArrayList<>(programs.getAllKeys()), program::setValue));
        y += CHOICE_ROWS * ChoiceList.ROW + 14;
        function = addRenderableWidget(new EditBox(font, left + MARGIN, y, FIELD_WIDTH, 18, Component.empty()));
        function.setValue(payload.data().getString("function"));
        y += 20;
        rebuildFunctions();
        program.setResponder(text -> {
            programChoices.filter(text);
            rebuildFunctions();
            update();
        });
        function.setResponder(text -> {
            functionChoices.filter(text);
            update();
        });
        programChoices.filter(program.getValue());
        setInitialFocus(program);
    }

    /** The function list shows the functions of the entered program. */
    private void rebuildFunctions() {
        if (functionChoices != null) {
            removeWidget(functionChoices);
        }
        int y = function.getY() + 20;
        functionChoices = addRenderableWidget(new ChoiceList(
                font, left + MARGIN, y, FIELD_WIDTH, CHOICE_ROWS, functions(program.getValue()), function::setValue));
        functionChoices.filter(function.getValue());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        caption(graphics, Component.translatable("dialog.c0de.device.program"), program.getY());
        caption(graphics, Component.translatable("dialog.c0de.device.function"), function.getY());
    }

    @Override
    protected Optional<String> problem() {
        String name = program.getValue();
        if (name.isEmpty()) {
            return Optional.empty();
        }
        if (!programs.contains(name)) {
            return Optional.of("There is no program " + name);
        }
        return functions(name).contains(function.getValue())
                ? Optional.empty()
                : Optional.of("Choose a function of " + name);
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("program", program.getValue());
        properties.putString("function", function.getValue());
        return properties;
    }
}

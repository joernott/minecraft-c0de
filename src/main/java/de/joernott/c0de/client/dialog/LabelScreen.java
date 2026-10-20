package de.joernott.c0de.client.dialog;

import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * The dialog of a LABEL. Its layout depends on the position of the label: an input field for the name, a list
 * of choices (libraries, functions, …) that fill the field when clicked, and an editor for the index of
 * references.
 */
public class LabelScreen extends PropertiesScreen {
    private static final int CHOICE_ROWS = 6;
    private static final int CHOICE_HEIGHT = 16;

    private final LabelMode mode;
    private final List<String> options = new ArrayList<>();
    private final List<Button> choices = new ArrayList<>();
    private EditBox name;
    private EditBox index;

    public LabelScreen(OpenDialogPayload payload) {
        super(payload);
        LabelMode parsed;
        try {
            parsed = LabelMode.valueOf(payload.data().getString("mode"));
        } catch (IllegalArgumentException e) {
            parsed = LabelMode.REFERENCE;
        }
        mode = parsed;
        payload.data().getList("options", Tag.TAG_STRING).forEach(option -> options.add(option.getAsString()));
    }

    private boolean hasChoices() {
        return !options.isEmpty() && !readOnly;
    }

    @Override
    protected int contentHeight() {
        return ROW + (hasChoices() ? CHOICE_ROWS * CHOICE_HEIGHT + 4 : 0) + (mode.allowsIndex() ? ROW + 4 : 0) + 8;
    }

    @Override
    protected void addFields() {
        int y = contentTop() + 10;
        name = addRenderableWidget(new EditBox(font, left + MARGIN, y, FIELD_WIDTH, 18, Component.empty()));
        name.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
        name.setValue(payload.data().getString("name"));
        name.setEditable(!readOnly);
        name.setResponder(text -> {
            updateChoices();
            update();
        });
        setInitialFocus(name);
        y += ROW;
        if (hasChoices()) {
            for (int i = 0; i < CHOICE_ROWS; i++) {
                Button choice = addRenderableWidget(Button.builder(Component.empty(), button -> {
                            name.setValue(button.getMessage().getString());
                        })
                        .bounds(left + MARGIN, y + i * CHOICE_HEIGHT, FIELD_WIDTH, CHOICE_HEIGHT)
                        .build());
                choices.add(choice);
            }
            y += CHOICE_ROWS * CHOICE_HEIGHT + 4;
            updateChoices();
        }
        if (mode.allowsIndex()) {
            index = addRenderableWidget(new EditBox(font, left + MARGIN, y + 10, FIELD_WIDTH, 18, Component.empty()));
            index.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
            index.setValue(payload.data().getString("index"));
            index.setEditable(!readOnly);
            index.setResponder(text -> update());
        }
    }

    /** Shows the choices that contain the entered text. */
    private void updateChoices() {
        String filter = name.getValue().toLowerCase(Locale.ROOT);
        List<String> matching = options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).contains(filter))
                .toList();
        if (matching.isEmpty() || (matching.size() == 1 && matching.get(0).equals(name.getValue()))) {
            // An exact match needs no list; show all choices instead
            matching = options;
        }
        for (int i = 0; i < choices.size(); i++) {
            Button choice = choices.get(i);
            choice.visible = i < matching.size();
            choice.setMessage(Component.literal(i < matching.size() ? matching.get(i) : ""));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        caption(graphics, Component.translatable("dialog.c0de.label." + mode.name().toLowerCase(Locale.ROOT)), name.getY());
        if (index != null) {
            caption(graphics, Component.translatable("dialog.c0de.label.index"), index.getY());
        }
    }

    @Override
    protected Optional<String> problem() {
        return PropertyRules.label(mode, name.getValue(), index == null ? "" : index.getValue());
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("name", name.getValue());
        properties.putString("index", index == null ? "" : index.getValue());
        return properties;
    }
}

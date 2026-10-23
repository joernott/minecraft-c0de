package de.joernott.c0de.client.dialog;

import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * The dialog of a LABEL. Its layout depends on the position of the label: an input field for the name, a
 * scrollable list of choices (libraries, functions, …) filtered by the name, which fill the field when clicked,
 * and an editor for the index of references.
 */
public class LabelScreen extends PropertiesScreen {
    private static final int CHOICE_ROWS = 10;

    private final LabelMode mode;
    private final List<String> options = new ArrayList<>();
    private ChoiceList choices;
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
        return ROW + (hasChoices() ? CHOICE_ROWS * ChoiceList.ROW + 6 : 0) + (mode.allowsIndex() ? ROW + 4 : 0) + 8;
    }

    @Override
    protected void addFields() {
        int y = contentTop() + 10;
        name = addRenderableWidget(new EditBox(font, left + MARGIN, y, FIELD_WIDTH, 18, Component.empty()));
        name.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
        name.setValue(payload.data().getString("name"));
        name.setEditable(!readOnly);
        name.setResponder(text -> {
            if (choices != null) {
                choices.filter(text);
            }
            update();
        });
        setInitialFocus(name);
        y += ROW;
        if (hasChoices()) {
            choices = addRenderableWidget(
                    new ChoiceList(font, left + MARGIN, y, FIELD_WIDTH, CHOICE_ROWS, options, name::setValue));
            choices.filter(name.getValue());
            y += CHOICE_ROWS * ChoiceList.ROW + 6;
        }
        if (mode.allowsIndex()) {
            index = addRenderableWidget(new EditBox(font, left + MARGIN, y + 10, FIELD_WIDTH, 18, Component.empty()));
            index.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
            index.setValue(payload.data().getString("index"));
            index.setEditable(!readOnly);
            index.setResponder(text -> update());
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

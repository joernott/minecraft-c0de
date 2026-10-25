package de.joernott.c0de.client.dialog;

import de.joernott.c0de.client.ClientCompat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/** The dialog of a LITERAL: a choice of the type and an input field for the value. */
public class LiteralScreen extends PropertiesScreen {
    private CycleButton<String> type;
    private EditBox value;

    public LiteralScreen(OpenDialogPayload payload) {
        super(payload);
    }

    @Override
    protected int contentHeight() {
        return 2 * ROW + 8;
    }

    @Override
    protected void addFields() {
        String initialType = Nbt.string(payload.data(), "type");
        type = addRenderableWidget(ClientCompat.cycleButton(
                        PropertyRules.LITERAL_TYPES,
                        PropertyRules.LITERAL_TYPES.contains(initialType) ? initialType : "INTEGER")
                .create(
                        left + MARGIN,
                        contentTop(),
                        FIELD_WIDTH,
                        20,
                        Component.translatable("dialog.c0de.literal.type"),
                        (button, chosen) -> update()));
        type.active = !readOnly;
        value = addRenderableWidget(
                new EditBox(font, left + MARGIN, contentTop() + ROW + 10, FIELD_WIDTH, 18, Component.empty()));
        value.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
        value.setValue(Nbt.string(payload.data(), "value"));
        value.setEditable(!readOnly);
        value.setResponder(text -> update());
        setInitialFocus(value);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        caption(graphics, Component.translatable("dialog.c0de.literal.value"), value.getY());
    }

    @Override
    protected Optional<String> problem() {
        return PropertyRules.literal(type.getValue(), value.getValue(), ZoneId.systemDefault());
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("type", type.getValue());
        properties.putString("value", value.getValue());
        return properties;
    }
}

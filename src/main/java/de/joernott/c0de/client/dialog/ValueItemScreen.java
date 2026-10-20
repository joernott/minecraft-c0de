package de.joernott.c0de.client.dialog;

import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SaveItemPayload;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The dialog of a variable or constant item: its name, its type (the subtype or struct name can be added) and
 * its value, entered like the value of a LITERAL for simple types and STRING and as JSON otherwise.
 */
public class ValueItemScreen extends PropertiesScreen {
    private EditBox name;
    private EditBox type;
    private EditBox value;

    public ValueItemScreen(OpenDialogPayload payload) {
        super(payload);
    }

    @Override
    public Component getTitle() {
        return Component.translatable(
                payload.data().getBoolean("constant") ? "item.c0de.constant_item" : "item.c0de.variable_item");
    }

    @Override
    protected int contentHeight() {
        return 3 * (ROW + 10) + 8;
    }

    private EditBox field(int row, String key, int maxLength) {
        EditBox box = addRenderableWidget(new EditBox(
                font, left + MARGIN, contentTop() + 10 + row * (ROW + 10), FIELD_WIDTH, 18, Component.empty()));
        box.setMaxLength(maxLength);
        box.setValue(payload.data().getString(key));
        box.setEditable(!readOnly);
        box.setResponder(text -> update());
        return box;
    }

    @Override
    protected void addFields() {
        name = field(0, "name", PropertyRules.MAX_TEXT_LENGTH);
        type = field(1, "type", 200);
        value = field(2, "value", 10 * PropertyRules.MAX_TEXT_LENGTH);
        setInitialFocus(name);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        caption(graphics, Component.translatable("dialog.c0de.item.name"), name.getY());
        caption(graphics, Component.translatable("dialog.c0de.item.type"), type.getY());
        caption(graphics, Component.translatable("dialog.c0de.item.value"), value.getY());
    }

    @Override
    protected Optional<String> problem() {
        return ItemRules.check(name.getValue(), type.getValue(), value.getValue(), ZoneId.systemDefault())
                .problem();
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("name", name.getValue());
        properties.putString("type", type.getValue());
        properties.putString("value", value.getValue());
        return properties;
    }

    @Override
    protected CustomPacketPayload savePayload() {
        return new SaveItemPayload(properties());
    }
}

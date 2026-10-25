package de.joernott.c0de.client.dialog;

import de.joernott.c0de.client.ClientCompat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * The dialog of a TAG: a choice of the language and an input field for the name, which is a path in VARIABLE
 * and CONSTANT lines and a key in struct declarations.
 */
public class TagScreen extends PropertiesScreen {
    private final boolean path;
    private CycleButton<String> language;
    private EditBox name;

    public TagScreen(OpenDialogPayload payload) {
        super(payload);
        path = Nbt.bool(payload.data(), "path");
    }

    @Override
    protected int contentHeight() {
        return 2 * ROW + 8;
    }

    @Override
    protected void addFields() {
        String initial = Nbt.string(payload.data(), "language");
        language = addRenderableWidget(ClientCompat.cycleButton(
                        PropertyRules.TAG_LANGUAGES, PropertyRules.TAG_LANGUAGES.contains(initial) ? initial : "json")
                .create(
                        left + MARGIN,
                        contentTop(),
                        FIELD_WIDTH,
                        20,
                        Component.translatable("dialog.c0de.tag.language"),
                        (button, chosen) -> update()));
        language.active = !readOnly;
        name = addRenderableWidget(
                new EditBox(font, left + MARGIN, contentTop() + ROW + 10, FIELD_WIDTH, 18, Component.empty()));
        name.setMaxLength(PropertyRules.MAX_TEXT_LENGTH);
        name.setValue(Nbt.string(payload.data(), "name"));
        name.setEditable(!readOnly);
        name.setResponder(text -> update());
        setInitialFocus(name);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        caption(graphics, Component.translatable(path ? "dialog.c0de.tag.path" : "dialog.c0de.tag.key"), name.getY());
    }

    @Override
    protected Optional<String> problem() {
        return PropertyRules.tag(language.getValue(), name.getValue(), path);
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("language", language.getValue());
        properties.putString("name", name.getValue());
        return properties;
    }
}

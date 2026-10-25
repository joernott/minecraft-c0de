package de.joernott.c0de.client.dialog;

import de.joernott.c0de.client.ClientCompat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import java.util.Optional;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/** The dialog of a COMMENT: a multi-line input field for its text. */
public class CommentScreen extends PropertiesScreen {
    private static final int TEXT_HEIGHT = 96;

    private MultiLineEditBox text;

    public CommentScreen(OpenDialogPayload payload) {
        super(payload);
    }

    @Override
    protected int contentHeight() {
        return TEXT_HEIGHT;
    }

    @Override
    protected void addFields() {
        text = addRenderableWidget(ClientCompat.multiLineEditBox(
                font,
                left + MARGIN,
                contentTop(),
                FIELD_WIDTH,
                TEXT_HEIGHT,
                Component.translatable("dialog.c0de.comment.placeholder"),
                Component.empty()));
        text.setCharacterLimit(PropertyRules.MAX_TEXT_LENGTH);
        text.setValue(Nbt.string(payload.data(), "text"));
        text.setValueListener(value -> update());
        text.active = !readOnly;
        if (!readOnly) {
            setInitialFocus(text);
        }
    }

    @Override
    protected boolean savesOnEnter() {
        return false;
    }

    @Override
    protected Optional<String> problem() {
        return PropertyRules.comment(text.getValue());
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("text", text.getValue());
        return properties;
    }
}

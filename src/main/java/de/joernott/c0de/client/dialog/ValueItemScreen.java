package de.joernott.c0de.client.dialog;

import de.joernott.c0de.C0de;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SaveItemPayload;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The dialog of a variable or constant item: its name, its type (the subtype or struct name can be added) and
 * its value, entered like the value of a LITERAL for simple types and STRING and as JSON otherwise. While the type
 * field has the focus, a drop-down list below it offers the types, filtered by the entered text. The trash can
 * button empties all fields; an item saved with empty fields is uninitialized.
 */
public class ValueItemScreen extends PropertiesScreen {
    private EditBox name;
    private EditBox type;
    private EditBox value;
    private Button trash;
    private ChoiceList types;
    private static final int TYPE_ROWS = 6;

    private static final ResourceLocation TRASH = C0de.id("textures/gui/trash.png");

    public ValueItemScreen(OpenDialogPayload payload) {
        super(payload);
    }

    /** True for a literal item: it has no name. */
    private boolean literal() {
        return Nbt.bool(payload.data(), "literal");
    }

    @Override
    public Component getTitle() {
        if (literal()) {
            return Component.translatable("item.c0de.literal_item");
        }
        return Component.translatable(
                Nbt.bool(payload.data(), "constant") ? "item.c0de.constant_item" : "item.c0de.variable_item");
    }

    @Override
    protected int contentHeight() {
        return 3 * (ROW + 10) + 8;
    }

    private EditBox field(int row, String key, int maxLength) {
        EditBox box = addRenderableWidget(new EditBox(
                font, left + MARGIN, contentTop() + 10 + row * (ROW + 10), FIELD_WIDTH, 18, Component.empty()));
        box.setMaxLength(maxLength);
        box.setValue(Nbt.string(payload.data(), key));
        box.setEditable(!readOnly);
        box.setResponder(text -> update());
        return box;
    }

    @Override
    protected void addFields() {
        name = field(0, "name", PropertyRules.MAX_TEXT_LENGTH);
        type = field(1, "type", 200);
        value = field(2, "value", 10 * PropertyRules.MAX_TEXT_LENGTH);
        name.visible = !literal();
        setInitialFocus(literal() ? type : name);
        List<String> choices = new ArrayList<>();
        Nbt.list(payload.data(), "types", Tag.TAG_STRING).forEach(type -> choices.add(Nbt.text(type)));
        // The drop-down list is drawn over the value field, so it is drawn and clicked separately
        types = new ChoiceList(font, type.getX(), type.getY() + 19, FIELD_WIDTH, TYPE_ROWS, choices, choice -> {
            type.setValue(choice);
            setFocused(value);
        });
        types.visible = false;
        type.setResponder(text -> {
            types.filter(text);
            update();
        });
        types.filter(type.getValue());
        if (!readOnly) {
            trash = addRenderableWidget(Button.builder(Component.empty(), button -> clear())
                    .bounds(left + WIDTH - MARGIN - 44, top + panelHeight() - MARGIN - 20, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("dialog.c0de.item.clear")))
                    .build());
        }
    }

    /** Empties all fields, which makes the item uninitialized when it is saved. */
    private void clear() {
        name.setValue("");
        type.setValue("");
        value.setValue("");
        setFocused(name);
    }

    private boolean showsTypes() {
        return types != null && !readOnly && type.isFocused() && !Nbt.list(payload.data(), "types", Tag.TAG_STRING).isEmpty();
    }

    //? if >=1.21.9 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (showsTypes() && types.isMouseOver(event.x(), event.y())) {
            return types.mouseClicked(event, doubleClick);
        }
        return super.mouseClicked(event, doubleClick);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showsTypes() && types.isMouseOver(mouseX, mouseY)) {
            return types.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //?}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showsTypes() && types.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        super.render(graphics, mouseX, mouseY, partialTick);
        types.visible = showsTypes();
        if (!readOnly && !Nbt.list(payload.data(), "types", Tag.TAG_STRING).isEmpty()) {
            // A small triangle at the end of the type field shows that it has a dropdown list
            int right = type.getX() + type.getWidth() - 4;
            int top = type.getY() + 7;
            for (int row = 0; row < 3; row++) {
                graphics.fill(right - 5 + row, top + row, right - row, top + row + 1, 0xFFA0A0A0);
            }
        }
        if (types.visible) {
            //? if >=1.21.6 {
            /*graphics.nextStratum();
            types.render(graphics, mouseX, mouseY, partialTick);
            *///?} else {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            types.render(graphics, mouseX, mouseY, partialTick);
            graphics.pose().popPose();
            //?}
        }
        if (!literal()) {
            caption(graphics, Component.translatable("dialog.c0de.item.name"), name.getY());
        }
        caption(graphics, Component.translatable("dialog.c0de.item.type"), type.getY());
        caption(graphics, Component.translatable("dialog.c0de.item.value"), value.getY());
        if (trash != null) {
            graphics.blit(TRASH, trash.getX() + 2, trash.getY() + 2, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected Optional<String> problem() {
        if (literal()) {
            if (type.getValue().isBlank()) {
                return value.getValue().isEmpty() ? Optional.empty() : Optional.of("A literal needs a type");
            }
            // A literal has no name; the rules of variable items are checked with a placeholder name
            return ItemRules.check("literal", type.getValue(), value.getValue(), ZoneId.systemDefault())
                    .problem();
        }
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

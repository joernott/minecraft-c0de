package de.joernott.c0de.client.dialog;

import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.network.OpenDialogPayload;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * The dialog of an OPERATOR: a button for every symbol. Clicking a symbol saves it; the current symbol is
 * shown in yellow.
 */
public class OperatorScreen extends PropertiesScreen {
    private static final int COLUMNS = 7;
    private static final int SIZE = 30;

    private Operator chosen;

    public OperatorScreen(OpenDialogPayload payload) {
        super(payload);
    }

    private static int rows() {
        return (OperatorBlockEntity.CHOICES.size() + COLUMNS - 1) / COLUMNS;
    }

    @Override
    protected int contentHeight() {
        return rows() * (SIZE + 2);
    }

    @Override
    protected boolean hasOkButton() {
        return false;
    }

    @Override
    protected void addFields() {
        String current = Nbt.string(payload.data(), "operator");
        int gridLeft = left + (WIDTH - COLUMNS * (SIZE + 2)) / 2;
        for (int i = 0; i < OperatorBlockEntity.CHOICES.size(); i++) {
            Operator operator = OperatorBlockEntity.CHOICES.get(i);
            Component symbol = operator.name().equals(current)
                    ? Component.literal(operator.symbol()).withStyle(ChatFormatting.YELLOW)
                    : Component.literal(operator.symbol());
            Button button = addRenderableWidget(Button.builder(symbol, pressed -> {
                        chosen = operator;
                        save();
                    })
                    .bounds(gridLeft + (i % COLUMNS) * (SIZE + 2), contentTop() + (i / COLUMNS) * (SIZE + 2), SIZE, SIZE)
                    .tooltip(Tooltip.create(Component.translatable(
                            "dialog.c0de.operator." + operator.name().toLowerCase(java.util.Locale.ROOT))))
                    .build());
            button.active = !readOnly;
        }
    }

    @Override
    protected Optional<String> problem() {
        return chosen == null ? Optional.of("") : Optional.empty();
    }

    @Override
    protected CompoundTag properties() {
        CompoundTag properties = new CompoundTag();
        properties.putString("operator", chosen.name());
        return properties;
    }
}

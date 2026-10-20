package de.joernott.c0de.client.dialog;

import de.joernott.c0de.C0de;
import de.joernott.c0de.network.OpenDialogPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Opens the dialogs the server asks for. */
public final class DialogScreens {
    private DialogScreens() {}

    public static void open(OpenDialogPayload payload) {
        Screen screen =
                switch (payload.dialog()) {
                    case "label" -> new LabelScreen(payload);
                    case "literal" -> new LiteralScreen(payload);
                    case "operator" -> new OperatorScreen(payload);
                    case "comment" -> new CommentScreen(payload);
                    case "tag" -> new TagScreen(payload);
                    case "value_item" -> new ValueItemScreen(payload);
                    default -> null;
                };
        if (screen == null) {
            C0de.LOGGER.warn("Unknown dialog {}", payload.dialog());
            return;
        }
        Minecraft.getInstance().setScreen(screen);
    }
}

package de.joernott.c0de.client;

import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The password dialog of the persistent vault (docs/concept/20-lib-security.md): CREATE_VAULT asks for a password
 * and its repetition, OPEN_VAULT for the password, and after a wrong one the dialog stays open for another try.
 * The fields show a * for every character. The password never leaves the client.
 */
public class PasswordScreen extends Screen {
    private static final int WIDTH = 220;

    private static PasswordScreen current;

    private final boolean create;
    private final Consumer<char[]> onPassword;
    private final Runnable onCancel;
    private final MaskedText password = new MaskedText();
    private final MaskedText repeat = new MaskedText();
    private Button ok;
    private Component message = Component.empty();
    private boolean working;
    private boolean answered;

    private PasswordScreen(boolean create, Consumer<char[]> onPassword, Runnable onCancel) {
        super(Component.translatable(create ? "dialog.c0de.vault.create" : "dialog.c0de.vault.open"));
        this.create = create;
        this.onPassword = onPassword;
        this.onCancel = onCancel;
    }

    /**
     * Asks the player for a password.
     *
     * @param create true for a new vault: the password must be repeated
     * @param onPassword gets the password; for opening, {@link #done()} or {@link #wrongPassword()} follows
     * @param onCancel runs when the player closes the dialog
     */
    public static void ask(boolean create, Consumer<char[]> onPassword, Runnable onCancel) {
        if (current != null) {
            current.cancel();
        }
        current = new PasswordScreen(create, password -> {
            onPassword.accept(password);
            if (create) {
                done();
            }
        }, onCancel);
        ClientCompat.setScreen(current);
    }

    /** The password opened the vault: the dialog closes. */
    public static void done() {
        if (current != null) {
            current.answered = true;
            current.onClose();
            current = null;
        }
    }

    /** The password doesn't open the vault: the dialog says so and waits for another try. */
    public static void wrongPassword() {
        if (current != null) {
            current.working = false;
            current.password.clear();
            current.message = Component.translatable("dialog.c0de.vault.wrong").withStyle(ChatFormatting.RED);
            current.updateOk();
        }
    }

    @Override
    protected void init() {
        int left = (width - WIDTH) / 2;
        int y = height / 2 - 40;
        password.box = addRenderableWidget(new EditBox(font, left, y, WIDTH, 18, Component.empty()));
        password.attach(this::updateOk);
        if (create) {
            repeat.box = addRenderableWidget(new EditBox(font, left, y + 34, WIDTH, 18, Component.empty()));
            repeat.attach(this::updateOk);
        }
        int buttons = y + (create ? 68 : 34);
        ok = addRenderableWidget(Button.builder(Component.translatable("gui.ok"), button -> submit())
                .bounds(left, buttons, WIDTH / 2 - 2, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + WIDTH / 2 + 2, buttons, WIDTH / 2 - 2, 20)
                .build());
        setInitialFocus(password.box);
        updateOk();
    }

    private void updateOk() {
        if (ok != null) {
            ok.active = !working
                    && !password.text.isEmpty()
                    && (!create || password.text.equals(repeat.text));
        }
    }

    private void submit() {
        if (!ok.active) {
            return;
        }
        working = true;
        message = Component.translatable("dialog.c0de.vault.working");
        updateOk();
        char[] chars = password.text.toCharArray();
        onPassword.accept(chars);
        Arrays.fill(chars, '\0');
    }

    private void cancel() {
        if (!answered) {
            answered = true;
            onCancel.run();
        }
    }

    @Override
    public void onClose() {
        cancel();
        if (current == this) {
            current = null;
        }
        password.clear();
        repeat.clear();
        super.onClose();
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (width - WIDTH) / 2;
        int y = height / 2 - 40;
        graphics.drawCenteredString(font, title, width / 2, y - 26, 0xFFFFFFFF);
        graphics.drawString(font, Component.translatable("dialog.c0de.vault.password"), left, y - 11, 0xFFD0D0D0);
        if (create) {
            graphics.drawString(font, Component.translatable("dialog.c0de.vault.repeat"), left, y + 23, 0xFFD0D0D0);
        }
        graphics.drawCenteredString(font, message, width / 2, y + (create ? 94 : 60), 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** A field that keeps its text and shows a * for every character. */
    private static final class MaskedText {
        EditBox box;
        String text = "";
        private boolean masking;

        void attach(Runnable changed) {
            box.setMaxLength(256);
            box.setResponder(shown -> {
                if (masking) {
                    return;
                }
                String visible = "*".repeat(text.length());
                if (shown.length() >= visible.length() && shown.startsWith(visible)) {
                    text += shown.substring(visible.length());
                } else {
                    text = text.substring(0, Math.min(text.length(), shown.length()));
                }
                masking = true;
                box.setValue("*".repeat(text.length()));
                masking = false;
                changed.run();
            });
        }

        void clear() {
            text = "";
            if (box != null) {
                masking = true;
                box.setValue("");
                masking = false;
            }
        }
    }
}

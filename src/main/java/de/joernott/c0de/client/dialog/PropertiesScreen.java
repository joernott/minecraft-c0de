package de.joernott.c0de.client.dialog;

import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SavePropertiesPayload;
import de.joernott.c0de.platform.Platform;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The base of the dialogs of blocks with properties: a panel with the input fields of the block and an OK
 * button (a green check mark) that is only enabled for valid input. Escape or a click outside of the panel
 * closes the dialog without changes. In read-only mode, the fields can't be edited and there is no OK button.
 */
public abstract class PropertiesScreen extends Screen {
    protected static final int WIDTH = 240;
    protected static final int MARGIN = 8;
    protected static final int FIELD_WIDTH = WIDTH - 2 * MARGIN;
    protected static final int ROW = 24;

    private static final int PANEL_COLOR = 0xF0202020;
    private static final int BORDER_COLOR = 0xFF808080;
    private static final int PROBLEM_COLOR = 0xFFFF5555;

    protected final OpenDialogPayload payload;
    protected final boolean readOnly;
    protected int left;
    protected int top;
    private Button ok;

    protected PropertiesScreen(OpenDialogPayload payload) {
        super(Component.translatable("dialog.c0de." + payload.dialog()));
        this.payload = payload;
        this.readOnly = payload.readOnly();
    }

    /** The height of the panel without the title and the row of the OK button. */
    protected abstract int contentHeight();

    /** Adds the input fields; the content area starts at {@link #contentTop()}. */
    protected abstract void addFields();

    /** The problem of the current input, or empty if it is valid. */
    protected abstract Optional<String> problem();

    /** The properties that are sent to the server. */
    protected abstract CompoundTag properties();

    /** False for dialogs that save directly when an entry is chosen. */
    protected boolean hasOkButton() {
        return true;
    }

    protected int panelHeight() {
        return contentHeight() + 2 * ROW + MARGIN;
    }

    protected int contentTop() {
        return top + ROW;
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - panelHeight()) / 2;
        addFields();
        if (!readOnly && hasOkButton()) {
            ok = addRenderableWidget(Button.builder(
                            Component.literal("✔").withStyle(ChatFormatting.GREEN), button -> save())
                    .bounds(left + WIDTH - MARGIN - 20, top + panelHeight() - MARGIN - 20, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("dialog.c0de.ok")))
                    .build());
        }
        update();
    }

    /** Enables the OK button if the input is valid. Called whenever the input changes. */
    protected void update() {
        if (ok != null) {
            ok.active = problem().isEmpty();
        }
    }

    /** Sends the properties to the server and closes the dialog. */
    protected void save() {
        if (!readOnly && problem().isEmpty()) {
            Platform.INSTANCE.sendToServer(new SavePropertiesPayload(payload.pos(), properties()));
            onClose();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left - 1, top - 1, left + WIDTH + 1, top + panelHeight() + 1, BORDER_COLOR);
        graphics.fill(left, top, left + WIDTH, top + panelHeight(), PANEL_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Component title = readOnly
                ? getTitle().copy().append(Component.translatable("dialog.c0de.read_only"))
                : getTitle();
        graphics.drawString(font, title, left + MARGIN, top + MARGIN, 0xFFFFFFFF);
        if (!readOnly) {
            problem().ifPresent(problem -> graphics.drawWordWrap(
                    font,
                    Component.literal(problem),
                    left + MARGIN,
                    top + panelHeight() - MARGIN - 18,
                    WIDTH - 2 * MARGIN - 28,
                    PROBLEM_COLOR));
        }
    }

    /** Draws a caption above a field. */
    protected void caption(GuiGraphics graphics, Component text, int y) {
        graphics.drawString(font, text, left + MARGIN, y - 10, 0xFFA0A0A0);
    }

    /** True if Enter saves the input. Multi-line fields need Enter for line breaks. */
    protected boolean savesOnEnter() {
        return hasOkButton();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && savesOnEnter() && !readOnly) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < left || mouseX >= left + WIDTH || mouseY < top || mouseY >= top + panelHeight()) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

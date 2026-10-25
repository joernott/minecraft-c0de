package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.DialogScreens;
import de.joernott.c0de.network.ClientReceivers;
import de.joernott.c0de.network.OpenDialogPayload;
import net.minecraft.world.item.ItemStack;

/** Loader-independent client initialization. The client entry points call {@link #init()}. */
public final class C0deClient {
    private C0deClient() {}

    /** The yellow of the text marker, which is the feather of Minecraft. */
    private static final int MARKER_COLOR = 0xFFFFD83A;

    public static void init() {
        ClientReceivers.register(OpenDialogPayload.TYPE, DialogScreens::open);
        ClientReceivers.register(de.joernott.c0de.network.TerminalPayload.TYPE, TerminalScreen::receive);
        ClientReceivers.register(de.joernott.c0de.network.SheetOwnersPayload.TYPE, ClientSheets::receive);
    }

    /**
     * A left click with a tablet in the main hand opens its terminal instead of attacking. Called by the client
     * entry points before an attack.
     *
     * @return true if the click was used
     */
    public static boolean attack() {
        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof de.joernott.c0de.item.TabletItem)) {
            return false;
        }
        de.joernott.c0de.platform.Platform.INSTANCE.sendToServer(de.joernott.c0de.network.OpenTabletPayload.INSTANCE);
        return true;
    }

    /** The item colors of the mod: the text marker is a yellow feather. */
    public static int itemColor(ItemStack stack, int tintIndex) {
        return tintIndex == 0 ? MARKER_COLOR : 0xFFFFFFFF;
    }
}

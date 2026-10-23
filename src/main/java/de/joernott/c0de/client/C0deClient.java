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
    }

    /** The item colors of the mod: the text marker is a yellow feather. */
    public static int itemColor(ItemStack stack, int tintIndex) {
        return tintIndex == 0 ? MARKER_COLOR : 0xFFFFFFFF;
    }
}

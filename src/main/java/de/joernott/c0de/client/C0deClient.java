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
        ClientReceivers.register(de.joernott.c0de.network.VaultRequestPayload.TYPE, VaultClient::receive);
        de.joernott.c0de.item.HandbookItem.opener = de.joernott.c0de.client.handbook.HandbookScreen::open;
        // The books of other mods, for the creative tab (the books of c0de have binders of their own)
        de.joernott.c0de.item.HandbookItem.knownBooks = () -> de.joernott.c0de.client.handbook.HandbookContent
                .books(net.minecraft.client.Minecraft.getInstance().getResourceManager())
                .stream()
                .filter(book -> !book.id().startsWith(de.joernott.c0de.C0de.MOD_ID + ":"))
                .map(book -> new de.joernott.c0de.item.HandbookItem.Known(book.id(), book.color()))
                .toList();
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

    /**
     * The item colors of the mod (before 1.21.4; since then the item definitions have tints): the text marker is a
     * yellow feather, and the label of the generic binder has the colour of its book.
     */
    public static int itemColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof de.joernott.c0de.item.HandbookItem) {
            return tintIndex == 1 ? 0xFF000000 | de.joernott.c0de.item.HandbookItem.color(stack) : 0xFFFFFFFF;
        }
        return tintIndex == 0 ? MARKER_COLOR : 0xFFFFFFFF;
    }
}

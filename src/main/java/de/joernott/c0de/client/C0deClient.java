package de.joernott.c0de.client;

import de.joernott.c0de.client.dialog.DialogScreens;
import de.joernott.c0de.network.ClientReceivers;
import de.joernott.c0de.network.OpenDialogPayload;

/** Loader-independent client initialization. The client entry points call {@link #init()}. */
public final class C0deClient {
    private C0deClient() {}

    public static void init() {
        ClientReceivers.register(OpenDialogPayload.TYPE, DialogScreens::open);
    }
}

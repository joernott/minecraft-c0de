package de.joernott.c0de.client;

import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.network.SheetOwnersPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The owners of the sheets as the server sent them, for the sheet information on the floor (task 11.11.7). */
public final class ClientSheets {
    private static final Map<Integer, String> OWNERS = new ConcurrentHashMap<>();

    private ClientSheets() {}

    /** Replaces the owners with those of the payload. */
    public static void receive(SheetOwnersPayload payload) {
        OWNERS.clear();
        for (String key : Nbt.keys(payload.owners())) {
            OWNERS.put(Integer.parseInt(key), Nbt.string(payload.owners(), key));
        }
    }

    /** The name of the owner of a sheet, or null if it has none. */
    public static String owner(int sheet) {
        return OWNERS.get(sheet);
    }
}
